package com.example.pressuretracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ++
 */
class LoginActivity : AppCompatActivity() {
    val TAG="LoginActivity"
    lateinit var tabLayout :TabLayout;
    lateinit var  viewPager :ViewPager;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PressureTracker)
        setContentView(R.layout.login_activity)
        supportActionBar?.hide();
        val sharedPref = getSharedPreferences("JWT", Context.MODE_PRIVATE);
        val editor =sharedPref.edit();
        val accessToken = sharedPref.getString("accessToken",null);
        val refreshToken = sharedPref.getString("refreshToken",null);
        if(accessToken!=null || refreshToken!=null){
            ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                    if(response.code()!=201) createLogin()
                    val token=response.body()
                    if(token!=null) {
                        editor.apply {
                            putString("accessToken", token.accessToken)
                            putString("refreshToken",token.refreshToken)
                            apply()
                        }
                        startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                        this@LoginActivity.finish()
                    }
                }
                override fun onFailure(call: Call<Tokens>, t: Throwable) {
                    Log.i(TAG,t.toString());
                    createLogin() }
            })
        }
        else
            createLogin()

    }
    private fun createLogin(){
        findViewById<LinearProgressIndicator>(R.id.loginProgress).visibility=View.GONE;
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("Login"))
        tabLayout.addTab(tabLayout.newTab().setText("Register"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL;
        val loginAdapter = LoginTabAdapter(supportFragmentManager, this@LoginActivity, tabLayout.tabCount)
        viewPager.adapter = loginAdapter;
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { viewPager.currentItem = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}