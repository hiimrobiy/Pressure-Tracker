package com.example.pressuretracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderRvAdapter(val context : Context, val reminders : MutableList<Reminder>): RecyclerView.Adapter<ReminderRvAdapter.ReminderViewHolder>()  {
    class ReminderViewHolder (itemView : View,context: Context):RecyclerView.ViewHolder(itemView){
        var time = itemView.findViewById<TextView>(R.id.tvTimeForMedicine)
        var days = itemView.findViewById<TextView>(R.id.tvDays)
        var medicineName = itemView.findViewById<TextView>(R.id.tvMedicine)
        lateinit var currReminder:Reminder
        var deleteButton = itemView.findViewById<Button>(R.id.btnDelete)
        init{
            deleteButton.setOnClickListener{
                context as ReminderDialogFragment.ReminderDialogListener
                context.onDeleteReminder(currReminder)
            }
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderRvAdapter.ReminderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false)
        return ReminderViewHolder(view,context);
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
    var reminder=reminders[position]
        val hours =if(reminder.hours<10) "0"+reminder.hours else reminder.hours;
        val minutes =if(reminder.minutes<10) "0"+reminder.minutes else reminder.minutes;
        holder.time.text="${hours} : ${minutes}"
        var daysArr=reminder.days.split(",");
        var days:String="";
        for( s in daysArr){
            if(!s.equals("-")){
                days+=s+",";
            }
        }
        if(days.length>1){
            days=days.substring(0,days.length-1)
        }
    holder.days.text=days
    holder.medicineName.text=reminder.medicinename
    holder.currReminder=reminder
    }

    override fun getItemCount()=reminders.size
}