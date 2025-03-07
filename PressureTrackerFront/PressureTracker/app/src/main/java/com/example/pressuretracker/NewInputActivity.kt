package com.example.pressuretracker

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputLayout
import java.lang.NumberFormatException
import java.util.*

/**
 * ++
 */
class NewInputActivity : AppCompatActivity() {

    private  var newDescription: Description ? =null
    private lateinit var innerConstraintLayout: ConstraintLayout
    private lateinit var saveButton: Button
    private lateinit var tvDescription: TextView;
    lateinit var type:String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getStringExtra("type")!!;
        setContentView(if(type=="Pressure") R.layout.blood_pressure_form else R.layout.blood_sugar_form)
        supportActionBar?.hide()
        addListenerToSaveButton()
        addListenerForReturn()
        setUpDescription()
        showDescription()
        addListenerToInnerSaveButton()
    }

    private fun setUpDescription() {
        innerConstraintLayout=findViewById(R.id.inner_constraint_layout)
        innerConstraintLayout.visibility=View.GONE
        tvDescription=findViewById(R.id.tvAddDescription)
        val items = listOf("Excellent", "Good", "Alright", "Bad")
        val adapter = ArrayAdapter(this, R.layout.list_item, items)
        (findViewById<TextInputLayout>(R.id.dropdown_menu_feelings).editText as? AutoCompleteTextView)?.setAdapter(adapter)
        addListenerToSaveButton()
        val painCheckBox=findViewById<CheckBox>(R.id.cbPain)
        val painDescription=findViewById<TextInputLayout>(R.id.tvPainDescription)
        painDescription.visibility= View.GONE
        painCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                painDescription.visibility=View.VISIBLE
            else
                painDescription.visibility= View.GONE
        }
        val activityCheckBox=findViewById<CheckBox>(R.id.cbActivities)
        val activityDescription=findViewById<TextInputLayout>(R.id.tvActivityDescription)
        activityDescription.visibility= View.GONE
        activityCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                activityDescription.visibility=View.VISIBLE
            else
                activityDescription.visibility= View.GONE
        }
        setUpSeekBar()
    }

    private fun setUpSeekBar() {
        val sb = findViewById<SeekBar>(R.id.sbStressBar)
        sb.max = 10
        sb.progress = 0
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                findViewById<TextView>(R.id.tvSeekBarProgress).text = Integer.toString(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun showDescription() {
        val addDescriptionButton=findViewById<ImageView>(R.id.btnAddDescription)
        addDescriptionButton.setOnClickListener{
            if(innerConstraintLayout.visibility==View.GONE){
                innerConstraintLayout.visibility=View.VISIBLE
                saveButton.visibility=View.GONE
                tvDescription.text="Hide description"
            }
            else {
                innerConstraintLayout.visibility = View.GONE
                saveButton.visibility = View.VISIBLE
                tvDescription.text="Add description"
            }
        }
    }

    private fun addListenerForReturn() {
        val returnButton=findViewById<ImageView>(R.id.returnButton)
        returnButton.setOnClickListener{
            setResult(Activity.RESULT_CANCELED,intent)
            finish()
        }
    }

    private fun addListenerToSaveButton() {
        saveButton=findViewById(R.id.saveButton)
        saveButton.setOnClickListener{
           try {
               if(type=="Pressure"){
                   val systolicValue = findViewById<TextInputLayout>(R.id.inputSystolic).editText?.text.toString().toInt()
                   val diastolicValue=findViewById<TextInputLayout>(R.id.inputDiastolic).editText?.text.toString().toInt()
                   val heartRateValue=findViewById<TextInputLayout>(R.id.inputHeartRate).editText?.text.toString().toInt()
                   val newInput=BloodPressureInput(0,systolicValue,diastolicValue,heartRateValue, Date(),newDescription)
                   intent.putExtra("newInput",newInput)
                   setResult(Activity.RESULT_OK,intent)
               }
               else if(type=="Sugar"){
                   val level = findViewById<TextInputLayout>(R.id.inputSugarLevels).editText?.text.toString().toInt()
                   val newInput=SugarLevelInput(0,level.toDouble(),Date(),newDescription)
                   intent.putExtra("newInput",newInput)
                   setResult(Activity.RESULT_OK,intent)
               }
               finish()
           }
           catch (ex:NumberFormatException){ Log.i("FormActivity",ex.toString()) }
        }
    }

    private fun addListenerToInnerSaveButton() {
        val descriptionSaveButton=findViewById<Button>(R.id.innerSaveButton)
        descriptionSaveButton.setOnClickListener{
            val feeling=findViewById<TextInputLayout>(R.id.dropdown_menu_feelings).editText?.text.toString()
            val eating=findViewById<TextInputLayout>(R.id.tvEating).editText?.text.toString()
            val painChecked=findViewById<CheckBox>(R.id.cbPain).isChecked
            val painDescription=if(painChecked) findViewById<TextInputLayout>(R.id.tvPainDescription).editText?.text.toString() else null
            val activityChecked=findViewById<CheckBox>(R.id.cbActivities).isChecked
            val activityDescription=if(activityChecked)  findViewById<TextInputLayout>(R.id.tvActivityDescription).editText?.text.toString() else null
            val stress=findViewById<SeekBar>(R.id.sbStressBar).progress
            val description=Description(0,feeling,eating,painDescription,activityDescription,stress)
            newDescription=description
            saveButton.callOnClick()
        }
    }
}