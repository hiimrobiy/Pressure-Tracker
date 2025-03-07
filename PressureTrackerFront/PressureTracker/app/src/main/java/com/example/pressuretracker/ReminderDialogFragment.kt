package com.example.pressuretracker

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout


/**
 * ++
 */
class ReminderDialogFragment : DialogFragment() {
    private lateinit var listener: ReminderDialogListener

    interface ReminderDialogListener {
        fun onDialogPositiveClick(reminder: Reminder)
        fun onDialogNegativeClick(reminder: Reminder)
        fun onDeleteReminder(reminder: Reminder)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder .setView(layoutInflater.inflate(R.layout.reminder_dialog_layout, null))
                    .setPositiveButton("Set", null)
                    .setNegativeButton("Cancel") { _, _ -> }

            val alertDialog = builder.create()

            alertDialog.setOnShowListener {
                val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                btnPositive.setOnClickListener {
                    val medicineNameInput =
                        alertDialog.findViewById<TextInputLayout>(R.id.etMedicineName)
                    val timeForMedicineInput =
                        alertDialog.findViewById<TextInputLayout>(R.id.etMedicineTime)
                    val daysChecked = arrayOf(
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbMonday).isChecked)     "Mon" else "-",
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbTuesday).isChecked)    "Tue" else "-",
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbWednesday).isChecked)  "Wen" else "-",
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbThursday).isChecked)   "Thu" else "-",
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbFriday).isChecked)     "Fri" else "-",
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbSaturday).isChecked)   "Sat" else "-",
                        if(alertDialog.findViewById<ToggleButton>(R.id.tbSunday).isChecked)     "Sun" else "-",

                    )
                    timeForMedicineInput.error = null
                    medicineNameInput.error = null
                    if (medicineNameInput.editText == null || medicineNameInput.editText?.text.toString().isEmpty()) {
                        medicineNameInput.error = "Error : Field mustn't be empty"
                    }
                    else if (medicineNameInput.editText?.text.toString().length > 10) {
                        medicineNameInput.error = "Error : Maximum length is 10"
                    }
                    else if (timeForMedicineInput.editText == null || timeForMedicineInput.editText?.text.toString().isEmpty()) {
                        timeForMedicineInput.error = "Error : Field mustn't be empty"
                    }
                    else if (!(timeForMedicineInput.editText?.text.toString().matches("\\d\\d:\\d\\d".toRegex()))) {
                        timeForMedicineInput.error = "Error : Field must follow format 'hh:mm' e.g: 08:40 "
                    }
                    else if (timeForMedicineInput.editText?.text.toString().split(":")[0].trim().toInt() !in 0..23) {
                        timeForMedicineInput.error = "Error : Hours have to be between 0-23 "
                    } else if (timeForMedicineInput.editText?.text.toString().split(":")[1].trim().toInt() !in 0..59) {
                        timeForMedicineInput.error = "Error : Minutes have to be between 0-59 "
                    }
                    else {
                        if (activity is ReminderDialogListener) {
                            listener = activity as ReminderDialogListener
                            val hours = timeForMedicineInput.editText?.text.toString().split(":")[0].trim().toInt()
                            val minutes = timeForMedicineInput.editText?.text.toString().split(":")[1].trim().toInt()
                            listener.onDialogPositiveClick(Reminder(0, medicineNameInput.editText?.text.toString(), hours, minutes, daysChecked.joinToString (",")))
                             alertDialog.dismiss()
                        }
                    }
                }
            }
            return alertDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}