package com.example.locationtracking

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_join.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.regex.Pattern
import android.view.WindowManager



class Sing_up : AppCompatActivity() {
    var pw_check :Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        var check: String ="1"
        val re: Retrofit = Retrofit.Builder()
            .client(OkHttpClient()).
                baseUrl(Sever_Service.url().server_url).build()
        val apiservice : Sever_Service = re.create(Sever_Service::class.java)

        user_password_check.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (user_password.text.toString() == user_password_check.text.toString()) {
                    setImage.setImageResource(R.drawable.baseline_panorama_fish_eye_black_18dp)
                    pw_check = true
                } else if (user_password_check.text.toString().length == 0) {
                    setImage.setImageBitmap(null)
                    pw_check = false
                } else {
                    pw_check = false
                    setImage.setImageResource(R.drawable.baseline_close_black_18dp)
                }
            }

        })

        user_email_check.setOnClickListener {
            val email_check = apiservice.email_check("member", user_email.text.toString())

            email_check.enqueue(object : Callback<ResponseBody> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val info = response.body()!!.string()
                    check = info
                    if(user_email.text.toString().replace(" ","").equals(""))
                    {
                        Toast.makeText(this@Sing_up, "빈칸확인", Toast.LENGTH_SHORT).show()
                    }
                    else if(info == "0"){
                        check = info
                        Toast.makeText(this@Sing_up, "사용가능 이메일입니다", Toast.LENGTH_SHORT).show()
                    }
                    else if(info == "1"){
                        check = info
                        Toast.makeText(this@Sing_up, "중복된 이메일입니다", Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("실패", "실패")

                }
            })
        }

        sing_up_button.setOnClickListener {
            val user_name = user_name.text.toString()
            val user_password = user_password.text.toString()
            val user_password_check = user_password_check.text.toString()
            val user_email = user_email.text.toString()

            val postCommentStr = apiservice.postRequst("member",
                user_name,user_password,user_email)

            if(user_name.replace(" ","").equals("")||user_password.replace(" ","").equals("")
                ||user_password_check.replace(" ","").equals("")||user_email.replace(" ","").equals(""))
            {
                Toast.makeText(this,"빈칸확인",Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            } else if (!checkEmail(user_email))  {
                Toast.makeText(this, "이메일 형식을 맞춰주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(check=="1"){
                Toast.makeText(this, "중복체크 해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                postCommentStr.enqueue(object : Callback<ResponseBody> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Log.e("성공", "성공")
                        Toast.makeText(this@Sing_up,"회원가입 완료",Toast.LENGTH_SHORT).show()
                        intent = Intent(this@Sing_up,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("실패", "실패")

                    }
                })

            }
        }
    }

     val EMAIL_ADDRESS_PATTERN = Pattern.compile(
         "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
         "\\@" +
         "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
         "(" +
         "\\." +
         "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
         ")+"
    )

    private fun checkEmail(email:String):Boolean {
    return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }


}
