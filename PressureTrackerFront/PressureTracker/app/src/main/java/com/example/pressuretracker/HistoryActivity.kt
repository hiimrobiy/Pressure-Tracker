package com.example.pressuretracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ++
 */
class HistoryActivity : AppCompatActivity() {
    val  TAG="HistoryActivity"
    lateinit var tabLayout :TabLayout;
    lateinit var  viewPager : ViewPager;
    lateinit  var sharedPref:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)
        supportActionBar?.hide()
        sharedPref= getSharedPreferences("JWT", MODE_PRIVATE)
        createTabLayout();
    }
    private fun createTabLayout(){
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("Blood Pressure"))
        tabLayout.addTab(tabLayout.newTab().setText("Blood Sugar"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL;
        val historyAdapter = HistoryTabAdapter(supportFragmentManager, this@HistoryActivity, tabLayout.tabCount)
        viewPager.adapter = historyAdapter;
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { viewPager.currentItem = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }

    fun onDeleteInput(input: Any?) {
        if (input != null ) {
            val page = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem())
            if (page is BloodPressureFragmentTab) {
                input as BloodPressureInput
                val sharedPref = getSharedPreferences("JWT", AppCompatActivity.MODE_PRIVATE)
                val accessToken = sharedPref.getString("accessToken", null);
                ServerConnector.serviceApp.deleteInput("Bearer $accessToken", input.id.toString()).enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.body() != null && response.code()==200) {
                                MainActivity.bloodPressureInputs.remove(input)
                                page.adapter.notifyDataSetChanged();
                                MainActivity.changedInputs=true;
                                Toast.makeText(applicationContext, "Input deleted", Toast.LENGTH_SHORT).show()
                            }
                            else if(response.code()==401 || response.code()==403){
                                val refreshToken = sharedPref.getString("refreshToken", null);
                                val editor =sharedPref.edit();
                                ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                                    override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                                        val token=response.body()
                                        if( response.code()==201 && token!=null) {
                                            editor.apply {
                                                putString("accessToken", token.accessToken)
                                                putString("refreshToken",token.refreshToken)
                                                apply()
                                                Log.i(TAG, "new tokens added "+token.toString())
                                            }
                                            onDeleteInput(input)
                                        }
                                        else
                                            logOut()
                                    }
                                    override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i(TAG,t.toString()); }
                                })
                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) { Log.i(TAG, t.cause.toString()) }
                    })
            }
            else if (page is SugarLevelFragmentTab) {
                input as SugarLevelInput
                val sharedPref = getSharedPreferences("JWT", AppCompatActivity.MODE_PRIVATE)
                val accessToken = sharedPref.getString("accessToken", null);
                ServerConnector.serviceApp.deleteInput("Bearer $accessToken", input.id.toString()).enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.body() != null && response.code()==200) {
                                MainActivity.sugarLevelInputs.remove(input)
                                page.adapter.notifyDataSetChanged();
                                MainActivity.changedInputs=true;
                                Toast.makeText(applicationContext, "Input deleted", Toast.LENGTH_SHORT).show()
                            }
                            else if(response.code()==401 || response.code()==403){
                                val refreshToken = sharedPref.getString("refreshToken", null);
                                val editor =sharedPref.edit();
                                ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                                    override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                                        val token=response.body()
                                        if( response.code()==201 && token!=null) {
                                            editor.apply {
                                                putString("accessToken", token.accessToken)
                                                putString("refreshToken",token.refreshToken)
                                                apply()
                                                Log.i(TAG, "new tokens added "+token.toString())
                                            }
                                            onDeleteInput(input)
                                        }
                                        else
                                            logOut()
                                    }
                                    override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i(TAG,t.toString()); }
                                })
                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) { Log.i(TAG, t.cause.toString()) }
                    })
            }
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
        finish();
    }
}