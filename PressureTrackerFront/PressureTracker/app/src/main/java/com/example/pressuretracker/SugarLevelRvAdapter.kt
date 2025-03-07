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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.time.ZoneId
import androidx.core.content.ContextCompat.startActivity

/**
 * ++
 */

class SugarLevelRvAdapter(val context: Context, val sugarLevelInputs: List<SugarLevelInput>) : RecyclerView.Adapter<SugarLevelRvAdapter.ViewHolder>() {

    class ViewHolder(val itemView: View, context: Context): RecyclerView.ViewHolder(itemView){
        val level:TextView;
        val day: TextView
        val month: TextView
        val year: TextView
        val time: TextView
        val separator: TextView
        var currentSugarLevelInput:SugarLevelInput?=null
        var deleteButton = itemView.findViewById<Button>(R.id.btnDeleteInput)
        var sharedPref: SharedPreferences;

        init{
            level= itemView.findViewById(R.id.tvSugarLevel)
            day=itemView.findViewById(R.id.tvDay)
            month=itemView.findViewById(R.id.tvMonth)
            year=itemView.findViewById(R.id.tvYear)
            time=itemView.findViewById(R.id.tvTime)
            separator=itemView.findViewById(R.id.separator)
            sharedPref= context.getSharedPreferences("JWT", AppCompatActivity.MODE_PRIVATE)
            itemView.setOnClickListener{
                if(currentSugarLevelInput!=null) {
                    val intent = Intent(context, SugarLevelDescription::class.java)
                    intent.putExtra("currentInput",currentSugarLevelInput)
                    startActivity(context, intent, null)
                }
            }
            deleteButton.setOnClickListener{
                context as HistoryActivity
                context.onDeleteInput(currentSugarLevelInput)
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(sugarLevelInput : SugarLevelInput){
            level.text=String.format("%.1f",sugarLevelInput.level)
            val date =sugarLevelInput.date
            if(date!=null){
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                day.text=localDate.dayOfWeek.name.substring(0,3)
                month.text=localDate.month.toString().substring(0,3)+" "+localDate.dayOfMonth
                year.text=localDate.year.toString()
                time.text=localDate.hour.toString()+":"+localDate.minute
            }
            if(level!=null && !level.text.isNullOrEmpty()){
                var levelValue=level.text.toString().replace(',','.').toDouble()
                if(levelValue<6.3)
                    separator.setBackgroundResource(R.color.good)
                else if(levelValue in 6.3..12.0)
                    separator.setBackgroundResource(R.color.alright)
                else
                    separator.setBackgroundResource(R.color.bad)
            }

            currentSugarLevelInput=sugarLevelInput
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.blood_sugar_item_rv, parent, false)
        return ViewHolder(view,context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val input=sugarLevelInputs[position]
        holder.bind(input)
    }

    override fun getItemCount() =sugarLevelInputs.size


}
