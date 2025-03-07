package com.example.pressuretracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ++
 */
class RegisterTabFragment :Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.register_tab_fragment,container,false);
        val registerButton = root.findViewById<Button>(R.id.tvRegisterButton)
        val sharedPref = this.requireActivity().getSharedPreferences("JWT", Context.MODE_PRIVATE);
        val editor = sharedPref.edit()
        val tvUsername = root.findViewById<TextInputLayout>(R.id.tvRegisterUsername)
        val tvPassword = root.findViewById<TextInputLayout>(R.id.tvRegisterPassword)
        val tvEmail = root.findViewById<TextInputLayout>(R.id.tvRegisterEmail)
        registerButton.setOnClickListener{
            if(tvUsername!=null && tvPassword!=null){
                tvUsername.error=null;
                tvPassword.error=null;
                tvEmail.error=null;
                val username = tvUsername.editText?.text.toString();
                val password = tvPassword.editText?.text.toString();
                val email = tvPassword.editText?.text.toString();
                if(username.isEmpty()){
                    tvUsername.error="Field can't be empty"
                    return@setOnClickListener;
                }
                if(password.isEmpty()){
                    tvPassword.error="Field can't be empty"
                    return@setOnClickListener;
                }
                if(email.isEmpty()){
                    tvPassword.error="Field can't be empty"
                    return@setOnClickListener;
                }
                ServerConnector.serviceApp.register(User(username,password,email)).enqueue(object : Callback<Tokens> {
                    override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                        if(response.code()==400){
                            tvUsername.error="Username or email already exits"
                            return
                        }
                        if(response.code()==200 ){
                            val token=response.body();
                            if(token!=null && token.accessToken!=null && token.refreshToken!=null) {
                                editor.apply {
                                    putString("accessToken", token.accessToken)
                                    putString("refreshToken",token.refreshToken)
                                    apply()
                                }
                                startActivity(Intent(activity,MainActivity::class.java))
                                activity?.finish()
                            }
                        }
                    }
                    override fun onFailure(call: Call<Tokens>, t: Throwable) {
                        Log.i("RegisterTabFragment","fails");
                    }
                })
            }
        }
        return  root;
    }
}