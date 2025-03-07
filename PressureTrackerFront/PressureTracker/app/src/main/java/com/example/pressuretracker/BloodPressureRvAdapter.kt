package com.example.pressuretracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import java.time.ZoneId

/**
 * ++
 */
class BloodPressureRvAdapter(val context: Context, val bloodPressureInputs: List<BloodPressureInput>) : RecyclerView.Adapter<BloodPressureRvAdapter.ViewHolder>() {

    class ViewHolder(val itemView: View,context: Context): RecyclerView.ViewHolder(itemView){
        val tvBpmValue:TextView
        val pressure:TextView
        val day:TextView
        val month:TextView
        val year:TextView
        val time:TextView
        val separator:TextView
        var currentBloodPressureInput:BloodPressureInput?=null
        var deleteButton = itemView.findViewById<Button>(R.id.btnDeleteInput)
        var sharedPref:SharedPreferences;

        init{
            tvBpmValue=itemView.findViewById(R.id.tvBpm)
            pressure=itemView.findViewById(R.id.tvPressure)
            day=itemView.findViewById(R.id.tvDay)
            month=itemView.findViewById(R.id.tvMonth)
            year=itemView.findViewById(R.id.tvYear)
            time=itemView.findViewById(R.id.tvTime)
            separator=itemView.findViewById(R.id.separator)
            sharedPref= context.getSharedPreferences("JWT", AppCompatActivity.MODE_PRIVATE)
            itemView.setOnClickListener{
                if(currentBloodPressureInput!=null) {
                    val intent = Intent(context, BloodPressureDescription::class.java)
                    intent.putExtra("currentInput",currentBloodPressureInput)
                    startActivity(context, intent, null)
                }
            }
            deleteButton.setOnClickListener{
               if(context is HistoryActivity) {
                   context as HistoryActivity
                   context.onDeleteInput(currentBloodPressureInput)
               }
            }


        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(bloodPressureInput : BloodPressureInput){
            pressure.text="${bloodPressureInput.systolic}/${bloodPressureInput.diastolic} mmHg"
            tvBpmValue.text="${bloodPressureInput.heartRate} bpm"
            val date =bloodPressureInput.date
            if(date!=null){
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                day.text=localDate.dayOfWeek.name.substring(0,3)
                month.text=localDate.month.toString().substring(0,3)+" "+localDate.dayOfMonth
                year.text=localDate.year.toString()
                time.text=localDate.hour.toString()+":"+localDate.minute
            }
            if(bloodPressureInput.systolic in 60..90 && bloodPressureInput.diastolic in 40..60)
                separator.setBackgroundResource(R.color.low_blue)
            else if(bloodPressureInput.systolic in 90..120 && bloodPressureInput.diastolic in 60..80)
                separator.setBackgroundResource(R.color.good)
            else if(bloodPressureInput.systolic in 120..145 && bloodPressureInput.diastolic in 80..90)
                separator.setBackgroundResource(R.color.alright)
            else
                separator.setBackgroundResource(R.color.bad)
            currentBloodPressureInput=bloodPressureInput
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.blood_pressure_item_rv, parent, false)
        return ViewHolder(view,context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val input=bloodPressureInputs[position]
        holder.bind(input)
    }

    override fun getItemCount() =bloodPressureInputs.size


}
