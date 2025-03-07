package com.example.pressuretracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.auth0.android.jwt.JWT
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * ++
 */
class MainActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var sharedPref:SharedPreferences
    companion object{
        var bloodPressureInputs:MutableList<BloodPressureInput> = mutableListOf()
        var sugarLevelInputs:MutableList<SugarLevelInput> = mutableListOf()
        var changedInputs=false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PressureTracker)
        setContentView(R.layout.activity_main)
        sharedPref = getSharedPreferences("JWT", Context.MODE_PRIVATE)
        addListenerForNewInput()
        setUpSideNavigation()
        getBloodPressureInputsFromDatabase()
    }

    private fun getBloodPressureInputsFromDatabase() {
        val accessToken = sharedPref.getString("accessToken",null)
        ServerConnector.serviceApp.getBloodPressureInputs("Bearer $accessToken").enqueue(object : Callback<List<BloodPressureInputResponse>> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<List<BloodPressureInputResponse>>, response: Response<List<BloodPressureInputResponse>>) {
                val responseInputs = response.body()
                if((response.code()==200 || response.code()==201) && responseInputs!=null){
                    bloodPressureInputs=responseInputs.map{ inputResponse->BloodPressureInput(inputResponse.inputid,
                                                                    inputResponse.systolic,
                                                                    inputResponse.diastolic,
                                                                    inputResponse.heartrate,
                                                                    inputResponse.date,
                                                                    if(inputResponse.descriptionid!=null)  Description( inputResponse.descriptionid,
                                                                                                                        inputResponse.feelings,
                                                                                                                        inputResponse.meals,
                                                                                                                        inputResponse.paindescription,inputResponse.activitydescription,inputResponse.stresslevel)
                                                                    else null)
                                }.toMutableList()
                    getSugarLeveInputsFromDatabase()
                }
                else if(response.code()==401 || response.code()==403){
                    val refreshToken = sharedPref.getString("refreshToken", null)
                    val editor =sharedPref.edit()
                    ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                        override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                            val token=response.body()
                            if( response.code()==201 && token!=null) {
                                editor.apply {
                                    putString("accessToken", token.accessToken)
                                    putString("refreshToken",token.refreshToken)
                                    apply()
                                    Log.i("MainActivity", "new tokens added "+token.toString())
                                }
                                getBloodPressureInputsFromDatabase()
                            }
                            else
                                logOut()
                        }
                        override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                    })
                }

            }
            override fun onFailure(call: Call<List<BloodPressureInputResponse>>, t: Throwable) { Log.i("MainActivity",t.toString()) }
        })
    }

    private fun getSugarLeveInputsFromDatabase() {
        val accessToken = sharedPref.getString("accessToken",null)
        ServerConnector.serviceApp.getBloodSugarInputs("Bearer $accessToken").enqueue(object : Callback<List<SugarLevelInputResponse>> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<List<SugarLevelInputResponse>>, response: Response<List<SugarLevelInputResponse>>) {
                val responseInputs = response.body()
                if((response.code()==200 || response.code()==201) && responseInputs!=null){
                    sugarLevelInputs=responseInputs.map{ inputResponse->SugarLevelInput(inputResponse.inputid,
                                                                                        inputResponse.levels.toDouble(),
                                                                                        inputResponse.date,
                                                                                        if(inputResponse.descriptionid!=null)  Description( inputResponse.descriptionid,
                                                                                            inputResponse.feelings,
                                                                                            inputResponse.meals,
                                                                                            inputResponse.paindescription,inputResponse.activitydescription,inputResponse.stresslevel)
                                                                                        else null)
                    }.toMutableList()
                    calculateAvg()
                }
                else if(response.code()==401 || response.code()==403){
                    val refreshToken = sharedPref.getString("refreshToken", null)
                    val editor =sharedPref.edit()
                    ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                        override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                            val token=response.body()
                            if( response.code()==201 && token!=null) {
                                editor.apply {
                                    putString("accessToken", token.accessToken)
                                    putString("refreshToken",token.refreshToken)
                                    apply()
                                    Log.i("MainActivity", "new tokens added "+token.toString())
                                }
                                getSugarLeveInputsFromDatabase()
                            }
                            else
                                logOut()
                        }
                        override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                    })
                }

            }
            override fun onFailure(call: Call<List<SugarLevelInputResponse>>, t: Throwable) { Log.i("MainActivity",t.toString()) } })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addListenerForNewInput() {
        val bloodPressureButton = findViewById<ImageView>(R.id.imageView2)
        bloodPressureButton.setOnClickListener { loadPressureFormActivity(); }
        val sugarLevelsButton = findViewById<ImageView>(R.id.ivBloodSugar)
        sugarLevelsButton.setOnClickListener { loadSugarFormActivity(); }
    }

    private fun setUpSideNavigation() {
        val navButton = findViewById<ImageView>(R.id.btnNav)
        navButton.setOnClickListener{
            val navDrawer = findViewById<DrawerLayout>(R.id.drawerLayout)
            if(!navDrawer.isDrawerOpen(GravityCompat.START)) navDrawer.openDrawer(GravityCompat.START)
            else navDrawer.closeDrawer(GravityCompat.END)
        }
        supportActionBar?.hide()
        val usernameTextView = findViewById<TextView>(R.id.tvUsername)
        val accessToken = sharedPref.getString("accessToken",null)
        val jwt= JWT(accessToken!!)
        val claim: String? = jwt.getClaim("username").asString()
        usernameTextView.text="Hi , ${claim}"
        val navView = findViewById<NavigationView>(R.id.navView)
        navView.setNavigationItemSelectedListener { it ->
            when (it.itemId) {
                R.id.miHistory -> loadHistoryActivity()
                R.id.miMedicineReminder -> loadMedicineReminders()
                R.id.miLogOut-> logOut()
            }
            true
        }
    }

    private fun logOut() {
        val sharedPref = getSharedPreferences("JWT", Context.MODE_PRIVATE)
        val editor =sharedPref.edit()
        editor.apply {
            putString("accessToken", null)
            putString("refreshToken",null)
            commit()
        }
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var input = data?.getSerializableExtra("newInput")
            val accessToken = sharedPref.getString("accessToken", null)
            if (input is BloodPressureInput){
                ServerConnector.serviceApp.addBloodPressureInput("Bearer $accessToken", input, "bloodpressureinput").enqueue(object : Callback<String> {
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.code() == 201 && response.body()!=null) {
                            val inputId = response.body()
                            input.id = inputId!!.toInt()
                            bloodPressureInputs.add(input)
                            calculateAvg()
                            Toast.makeText(applicationContext, "New input added", Toast.LENGTH_SHORT).show()
                        }
                        else if(response.code()==401 || response.code()==403){
                            val refreshToken = sharedPref.getString("refreshToken", null)
                            val editor =sharedPref.edit()
                            ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                                override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                                    val token=response.body()
                                    if( response.code()==201 && token!=null) {
                                        editor.apply {
                                            putString("accessToken", token.accessToken)
                                            putString("refreshToken",token.refreshToken)
                                            apply()
                                            Log.i("MainActivity", "new tokens added "+token.toString())
                                        }
                                        onActivityResult(requestCode,resultCode,data)
                                    }
                                    else
                                        logOut()
                                }
                                override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                            })
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) {Log.i("MainActivity", t.toString()) }
                })
            }
            else if(input is SugarLevelInput){
                ServerConnector.serviceApp.addSugarLevelInput("Bearer $accessToken", input, "glucoselevelinput").enqueue(object : Callback<String> {
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.code() == 201 && response.body()!=null) {
                            val inputId = response.body()
                            input.id = inputId!!.toInt()
                            sugarLevelInputs.add(input)
                            calculateAvg()
                            Toast.makeText(applicationContext, "New input added", Toast.LENGTH_SHORT).show()
                        }
                        else if(response.code()==401 || response.code()==403){
                            val refreshToken = sharedPref.getString("refreshToken", null)
                            val editor =sharedPref.edit()
                            ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                                override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                                    val token=response.body()
                                    if( response.code()==201 && token!=null) {
                                        editor.apply {
                                            putString("accessToken", token.accessToken)
                                            putString("refreshToken",token.refreshToken)
                                            apply()
                                            Log.i("MainActivity", "new tokens added "+token.toString())
                                        }
                                        onActivityResult(requestCode,resultCode,data)
                                    }
                                    else
                                        logOut()
                                }
                                override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                            })
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) { Log.i("MainActivity", t.toString()) }
                })
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun calculateAvg() {
        val currentTimeInMilliseconds = Date().time
        val sevenDaysInMilliseconds= 7*24*60*60*1000
        val avgSystolic: OptionalDouble =bloodPressureInputs.stream().filter{input->currentTimeInMilliseconds-input.date!!.time<sevenDaysInMilliseconds}.mapToDouble{ input->input.systolic.toDouble()}.average()
        val avgDiastolic: OptionalDouble =bloodPressureInputs.stream().filter{input->currentTimeInMilliseconds-input.date!!.time<sevenDaysInMilliseconds}.mapToDouble{ input->input.diastolic.toDouble()}.average()
        val avgHeartRate: OptionalDouble =bloodPressureInputs.stream().filter{input->currentTimeInMilliseconds-input.date!!.time<sevenDaysInMilliseconds}.mapToDouble{ input->input.heartRate.toDouble()}.average()
        val avgSugarLevels: OptionalDouble = sugarLevelInputs.stream().filter{input->currentTimeInMilliseconds-input.date!!.time<sevenDaysInMilliseconds}.mapToDouble{ input->input.level.toDouble()}.average()
        if(avgSystolic.isPresent) findViewById<TextView>(R.id.tvSystolicAvg).text=String.format("%.1f",avgSystolic.asDouble)
        if(avgDiastolic.isPresent) findViewById<TextView>(R.id.tvDiastolicAvg).text=String.format("%.1f",avgDiastolic.asDouble)
        if(avgHeartRate.isPresent) findViewById<TextView>(R.id.tvHeartRateAvg).text=String.format("%.1f",avgHeartRate.asDouble)
        if(avgSugarLevels.isPresent) findViewById<TextView>(R.id.tvSugarLevelsAvg).text=String.format("%.1f",avgSugarLevels.asDouble)
    }

    private fun loadHistoryActivity() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun loadPressureFormActivity() {
        val intent = Intent(this, NewInputActivity::class.java)
        intent.putExtra("type","Pressure")
        startActivityForResult(intent, 10)
    }

    private fun loadSugarFormActivity() {
        val intent = Intent(this, NewInputActivity::class.java)
        intent.putExtra("type","Sugar")
        startActivityForResult(intent, 11)
    }

    private fun loadMedicineReminders() {
         startActivity(Intent(this, RemindersActivity::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        if(changedInputs) {
            calculateAvg()
            changedInputs=false
        }
    }

}