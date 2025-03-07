package com.example.pressuretracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * ++
 */
class SugarLevelDescription : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blood_sugar_description)
        supportActionBar?.hide()
        val input = intent.getSerializableExtra("currentInput") as SugarLevelInput;
        val description = input.description
        val feeling = findViewById<TextView>(R.id.feeling_value)
        val activity = findViewById<TextView>(R.id.activity_value)
        val meals = findViewById<TextView>(R.id.meals_value)
        val pain = findViewById<TextView>(R.id.pain_value)
        val stress = findViewById<TextView>(R.id.stress_value)
        val glucoseValue = findViewById<TextView>(R.id.glucoseValue)
        if(description!=null) {
            feeling.text = if(description.feelings.isNullOrEmpty()) "No data" else description.feelings
            activity.text = if(description.activityDescription.isNullOrEmpty()) "No data" else  description.activityDescription
            pain.text = if(description.painDescription.isNullOrEmpty()) "No data" else description.painDescription
            meals.text = if(description.meals.isNullOrEmpty()) "No data" else description.meals
            stress.text = if(description.stressLevel==null) "No data" else description.stressLevel.toString()
        }
        glucoseValue.text=String.format("%.1f",input.level)
        val result = findViewById<TextView>(R.id.normal_value)
        if(input.level<6.3){
            result.text="Excellent"
            result.setTextColor(ContextCompat.getColor(this, R.color.good));
        }
       else if(input.level in 6.3..12.0){
            result.text="Good"
            result.setTextColor(ContextCompat.getColor(this, R.color.alright));
        }
        else{
            result.text="High"
            result.setTextColor(ContextCompat.getColor(this, R.color.bad));
        }


    }
}