package com.example.pressuretracker

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
/**
 * ++
 */
class SugarLevelFragmentTab : Fragment() {
    lateinit var sharedPref: SharedPreferences;
    lateinit var adapter: SugarLevelRvAdapter;
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.activity_history,container,false)
        val rv = root.findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(context);
        adapter = SugarLevelRvAdapter(requireContext(), MainActivity.sugarLevelInputs)
        rv.adapter=adapter
        return root
    }

}
