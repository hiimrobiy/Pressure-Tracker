package com.example.pressuretracker

/**
 * ++
 */
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class LoginTabFragment  : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val TAG="LoginTabFragment"
        val root = inflater.inflate(R.layout.login_tab_fragment,container,false)
        val loginButton = root.findViewById<Button>(R.id.btnLoginButton)
        val sharedPref = this.requireActivity().getSharedPreferences("JWT", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val tvUsername = root.findViewById<TextInputLayout>(R.id.tvLoginUsername)
        val tvPassword = root.findViewById<TextInputLayout>(R.id.tvLoginPassword)
        val progress = root.findViewById<CircularProgressIndicator>(R.id.progress)
        loginButton.setOnClickListener{
            loginButton.visibility=View.GONE
            progress.visibility=View.VISIBLE
            if(tvUsername!=null && tvPassword!=null){
                tvUsername.error=null
                tvPassword.error=null
                val username = tvUsername.editText?.text.toString()
                val password = tvPassword.editText?.text.toString()
                if(username.isEmpty()){
                    tvUsername.error="Field can't be empty"
                    loginButton.visibility=View.VISIBLE
                    progress.visibility=View.GONE
                    return@setOnClickListener
                }
                if(password.isEmpty()){
                    tvPassword.error="Field can't be empty"
                    loginButton.visibility=View.VISIBLE
                    progress.visibility=View.GONE
                    return@setOnClickListener
                }
                ServerConnector.serviceApp.login(User(username,password,null)).enqueue(object : Callback<Tokens> {
                    override fun onResponse(call: Call<Tokens>, response: Response<Tokens>) {
                        if(response.code()==400){
                            tvUsername.error="Username doesn't match password"
                            tvPassword.error="Username doesn't match password"
                            loginButton.visibility=View.VISIBLE
                            progress.visibility=View.GONE
                            return
                        }
                        if(response.code()==200 ){
                            val token=response.body()
                            if(token!=null) {
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
                        loginButton.visibility=View.VISIBLE
                        progress.visibility=View.GONE
                        Log.i(TAG,t.toString());
                    }
                })
            }
        }
        return  root
    }
}