package com.example.locationtracking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import android.os.PowerManager
import android.view.Window


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button3.setOnClickListener {
            intent = Intent(this@MainActivity,Main3Activity::class.java)
            startActivity(intent)
        }
        val re: Retrofit = Retrofit.Builder()
            .client(OkHttpClient()).
                baseUrl(Sever_Service.url().server_url).build()
        val apiservice : Sever_Service = re.create(Sever_Service::class.java)

        var check:String

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        var isWhiteListing = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
        }
        if (!isWhiteListing) {
            val intent = Intent()
            intent.action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:" + applicationContext.packageName)
            startActivity(intent)
        }

        login_button.setOnClickListener { //로그인 버튼
            val login_check = apiservice.login_check("member",email.text.toString(),password.text.toString())

            login_check.enqueue(object :Callback<ResponseBody>{
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                   Log.e("실패","실패")
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    check=response.body()!!.string()
                    if(check=="1"){
                        intent = Intent(this@MainActivity,Main2Activity::class.java)
                        intent.putExtra("login_email",email.text.toString())
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(this@MainActivity,"입력한 정보가 맞지 않습니다",Toast.LENGTH_SHORT).show()
                    }
                }

            })

        }

        sing_up.setOnClickListener {//회원가입 버튼
            intent = Intent(this,Sing_up::class.java)
            startActivity(intent)
        }

    }

}
