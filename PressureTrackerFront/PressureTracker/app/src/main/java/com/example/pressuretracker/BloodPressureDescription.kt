package com.example.pressuretracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * --
 */
class BloodPressureDescription : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)
        supportActionBar?.hide()
        val input = intent.getSerializableExtra("currentInput") as BloodPressureInput;
        val description = input.description
        val feeling = findViewById<TextView>(R.id.feeling_value)
        val activity = findViewById<TextView>(R.id.activity_value)
        val meals = findViewById<TextView>(R.id.meals_value)
        val pain = findViewById<TextView>(R.id.pain_value)
        val stress = findViewById<TextView>(R.id.stress_value)
        val pressure = findViewById<TextView>(R.id.pressureValue)
        val bpm = findViewById<TextView>(R.id.bpm_value)
        if(description!=null) {
            feeling.text = if(description.feelings.isNullOrEmpty()) "No data" else description.feelings
            activity.text = if(description.activityDescription.isNullOrEmpty()) "No data" else  description.activityDescription
            pain.text = if(description.painDescription.isNullOrEmpty()) "No data" else description.painDescription
            meals.text = if(description.meals.isNullOrEmpty()) "No data" else description.meals
            stress.text = if(description.stressLevel==null) "No data" else description.stressLevel.toString()
        }

        pressure.text="${input.systolic}/${input.diastolic} mmHg"
        val systolic = input.systolic;
        val diastolic = input.diastolic;
        val result = findViewById<TextView>(R.id.normal_value)
        if(systolic in 40..80 && diastolic in 40..60){
            result.text="Low"
            result.setTextColor(ContextCompat.getColor(this, R.color.low_blue));
        }
        else if(systolic in 90..120 && diastolic in 60..80){
                result.text="Normal"
                result.setTextColor(ContextCompat.getColor(this, R.color.good));
        }
        else if(systolic in 120..130 && diastolic in 80..90){
            result.text="Hypertension 1"
            result.setTextColor(ContextCompat.getColor(this, R.color.hyper1));
        }
        else if(systolic >140 && diastolic >90){
            result.text="Hypertension 2"
            result.setTextColor(ContextCompat.getColor(this, R.color.hyper2));
        }
        else{
            result.text="Abnormal"
            result.setTextColor(ContextCompat.getColor(this, R.color.hyper1));
        }
        bpm.text=input.heartRate.toString()


    }
}