package com.example.locationtracking

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_frand_list.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class FriendList : AppCompatActivity() {
    val re: Retrofit = Retrofit.Builder()
        .client(OkHttpClient()).
            baseUrl(Sever_Service.url().server_url).build()
    val apiservice : Sever_Service = re.create(Sever_Service::class.java)
    var array = arrayListOf<Friend_Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frand_list)
        val user_email = intent.getStringExtra("login_email")
        friend(user_email)

        swipe_layout.setOnRefreshListener {
            Handler().postDelayed({
                friend(user_email)
                swipe_layout.setRefreshing(false)
            }, 500)
        }
        friend_add_button.setOnClickListener {
            show(user_email)
        }
    }

    fun show(email:String) {
        val edittext = EditText(this)

        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle("친구추가")
        builder.setMessage("친구 아이디 입력")
        builder.setView(edittext)
        builder.setPositiveButton(
            "입력"
        ) { dialog, which ->
            val a = apiservice.friend_add("friend_list",email,edittext.text.toString())
            a.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("실패","dasasd")

                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    var ab = response.body()!!.string()

                    if(ab=="1"){
                        Toast.makeText(getApplicationContext(),"이미 있는 아이디" ,Toast.LENGTH_LONG).show()
                    }
                    else if(ab=="2"){
                        Toast.makeText(getApplicationContext(),"없는 아이디" ,Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"친구추가 되었습니다." ,Toast.LENGTH_LONG).show()

                    }
                    friend_list_recyclerView.adapter!!.notifyDataSetChanged()
                }

            })

        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, which -> }
        builder.show()
    }


    fun friend(email: String) {

        val b = apiservice.friend_list("member",email)
        b.enqueue(object :Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ㅇㅇㅂㅈ","실패")

            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val id = response.body()!!.string()
                val jsonObject = JSONObject(id) // json 사용방법
                val jsonArray = jsonObject.getJSONArray("result")

                if(jsonArray.isNull(0)){ //친구목록 체크
                    textView3.visibility= View.VISIBLE
                }
                else{
                    textView3.visibility= View.GONE
                }

                for (i in 0 until jsonArray.length()) {
                    val subjsonObject = jsonArray.getJSONObject(i)

                    val user_email = subjsonObject.getString("friend_name")
                    val use_image = subjsonObject.getString("friend_image")

                    array!!.add(Friend_Data(user_email,use_image))
                }

                val mAdapter = Friend_Adapter(array)
                friend_list_recyclerView.adapter = mAdapter
                val lm = LinearLayoutManager(this@FriendList)
                friend_list_recyclerView.layoutManager = lm
                friend_list_recyclerView.setHasFixedSize(true)
            }

        })

    }

    override fun onBackPressed() {
        super.onBackPressed()

    }
}
