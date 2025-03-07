package com.example.pressuretracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.util.*

/**
 * ++
 */
class RemindersActivity : AppCompatActivity(), ReminderDialogFragment.ReminderDialogListener {

    private  var reminders: MutableList<Reminder> = mutableListOf()
    private lateinit var rvAdapter: RecyclerView.Adapter<ReminderRvAdapter.ReminderViewHolder>
    private lateinit var sharedPref:SharedPreferences;
    private var TAG="RemindersActivity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reminders)
        supportActionBar?.hide()
        sharedPref = getSharedPreferences("JWT", Context.MODE_PRIVATE)
        createNotificationChannel()
        getRemindersFromDataBase();
        setUpRecyclerView();

    }

    private fun getRemindersFromDataBase() {
        val accessToken = sharedPref.getString("accessToken",null);
        ServerConnector.serviceApp.getReminders("Bearer $accessToken").enqueue(object :Callback<MutableList<Reminder>>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<MutableList<Reminder>>, response: Response<MutableList<Reminder>>) {
                if((response.code()==200 || response.code()==201) && response.body()!=null){
                    response.body()!!.forEach {r->scheduleNotification(r)}
                    reminders.addAll(response.body()!!)
                    rvAdapter.notifyDataSetChanged();
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
                               getRemindersFromDataBase()
                            }
                            else
                                logOut()
                        }
                        override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                    })
                }
            }
            override fun onFailure(call: Call<MutableList<Reminder>>, t: Throwable) { Log.i(TAG,t.cause.toString()) }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification(reminder: Reminder) {
        var calendar =Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        val currentMinute = calendar.get(Calendar.MINUTE)
        calendar.set(Calendar.HOUR_OF_DAY,reminder.hours)
        calendar.set(Calendar.MINUTE,reminder.minutes)
        calendar.set(Calendar.SECOND,0);
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val days = reminder.days.split(",").filter {day->!day.equals("-")}
            .map{day->when (day){"Mon"->1 "Tue"->2 "Wen"->3 "Thu"->4 "Fri"->5 "Sat"->6 else -> 7}}
        for(day in days){
            val localDate = LocalDate.now()
            val dayOfWeekNum= localDate.dayOfWeek.value;
            var dayDifference=(day-dayOfWeekNum)%7
            if(dayDifference<0) dayDifference+=7;
            if(dayDifference==0){
               if(currentHour>reminder.hours || (currentHour==reminder.hours && currentMinute>reminder.minutes))
                   dayDifference+=7;
            }
            val intent = Intent(applicationContext, NotificationReceiver::class.java)
            val title = "Medicine reminder"
            val message = "You should take ${reminder.medicinename} at ${reminder.hours} : ${reminder.minutes}"
            intent.putExtra(titleExtra, title)
            intent.putExtra(messageExtra, message)
            intent.putExtra("notificationID", Random(calendar.timeInMillis).nextInt())
            val n=reminder.reminderid
            val m=day
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                (n+m)*(n+m+1)/2+m,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val oneDayInMilliseconds : Long= 24*60*60*1000;
            val startingTime = dayDifference * oneDayInMilliseconds + calendar.timeInMillis

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,startingTime,oneDayInMilliseconds,pendingIntent)
            }
    }

    private fun setUpRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvReminders)
        rvAdapter = ReminderRvAdapter(this, reminders)
        rv.adapter = rvAdapter
        rv.layoutManager = LinearLayoutManager(this)
        var addNewReminder = findViewById<FloatingActionButton>(R.id.btnAddNewReminder)
        addNewReminder.setOnClickListener { view ->
            ReminderDialogFragment().show(supportFragmentManager, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "Notification channel"
        val desc = "A Description of the Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDialogPositiveClick(reminder: Reminder) {
        val accessToken = sharedPref.getString("accessToken",null);
        ServerConnector.serviceApp.addReminder("Bearer $accessToken",reminder).enqueue(object :Callback<String>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if((response.code()==200 ||response.code()==201) && response.body()!=null) {
                    reminder.reminderid = response.body()!!.toInt()
                    reminders.add(reminder)
                    rvAdapter.notifyDataSetChanged();
                    scheduleNotification(reminder)
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
                                onDialogPositiveClick(reminder)
                            }
                            else
                                logOut()
                        }
                        override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                    })
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) { Log.i(TAG,t.cause.toString()) }
        })
    }

    override fun onDeleteReminder(reminder: Reminder) {
        val accessToken = sharedPref.getString("accessToken",null);
        ServerConnector.serviceApp.deleteReminder("Bearer $accessToken", reminder.reminderid.toString()).enqueue(object :Callback<String>{
            override fun onResponse(call: Call<String>,response: Response<String>) {
                if(response.body()!=null){
                    val days = reminder.days.split(",").filter {day->!day.equals("-")}
                        .map{day->when (day){"Mon"->1 "Tue"->2 "Wen"->3 "Thu"->4 "Fri"->5 "Sat"->6 else -> 7}}
                    for(day in days) {
                        val n=reminder.reminderid
                        val m=day
                        val intent = Intent(applicationContext, NotificationReceiver::class.java)
                        val title = "Medicine reminder"
                        val message = "You should take ${reminder.medicinename} at ${reminder.hours} : ${reminder.minutes}"
                        intent.putExtra(titleExtra, title)
                        intent.putExtra(messageExtra, message)
                        val pendingIntent = PendingIntent.getBroadcast(
                            applicationContext,
                            (n+m)*(n+m+1)/2+m,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.cancel(pendingIntent);
                    }
                    reminders.remove(reminder)
                    rvAdapter.notifyDataSetChanged()
                }
                else if(response.code()==401 || response.code()==403){
                    val refreshToken = sharedPref.getString("refreshToken", null);
                    val editor =sharedPref.edit();
                    ServerConnector.serviceApp.verifyAccessToken(Tokens(accessToken,refreshToken)).enqueue(object : Callback<Tokens> {
                        override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                            val token=response.body()
                            if(response.code()==201 && token!=null) {
                                editor.apply {
                                    putString("accessToken", token.accessToken)
                                    putString("refreshToken",token.refreshToken)
                                    apply()
                                    Log.i(TAG, "new tokens added "+token.toString())
                                }
                                onDeleteReminder(reminder)
                            }
                            else
                                logOut()
                        }
                        override fun onFailure(call: Call<Tokens>, t: Throwable) { Log.i("MainActivity",t.toString()); }
                    })
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) { Log.i(TAG,t.cause.toString()) }
        })
    }
    override fun onDialogNegativeClick(reminder: Reminder) {}
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
