package com.example.locationtracking

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.activity_frofile.*
import kotlinx.android.synthetic.main.nav_header_main2.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.IOException

class Frofile : AppCompatActivity() {

    private val IMG_REQUEST = 777
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frofile)
        val email =intent.getStringExtra("login_email")
        val name = intent.getStringExtra("user_name")

        my_user_name.text=name
        Glide.with(this@Frofile)
            .load("URL"+email+".jpg")
            .clone()
            .apply(RequestOptions().centerCrop().circleCrop())
            //.override(50,40)
            .error(R.drawable.baseline_account_box_black_18dp1)
            .skipMemoryCache (true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .into(imageView)

        /*Picasso.with(this@Frofile)
            .load("URL/"+email+".jpg")
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .centerCrop()
            .resize(400, 400)
            .into(imageView)*/

        imageView.setOnClickListener {
            selectImage()
        }

        myprofile_save.setOnClickListener {
            upoadImage(email)
            intent = Intent(this,Main2Activity::class.java)
            intent.putExtra("login_email",email)
            startActivity(intent)
            finish()
        }
    }

    private fun upoadImage(em:String) {
        val image = imageToString()
        val apiInterface = ApiClient.getRetrofit().create(Sever_Service::class.java)
        //val email = intent.getStringExtra("email")
        val call = apiInterface.uploadImage("imagedb",em,image)
        call.enqueue(object : Callback<ImageClass> {
            override fun onResponse(call: Call<ImageClass>, response: Response<ImageClass>) {
                val imageClass = response.body()
                //Log.e("성공",email)
            }

            override fun onFailure(call: Call<ImageClass>, t: Throwable) {
                Log.e("실패","실패")
                return
            }
        })
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, IMG_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val path = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, path)
                Glide.with(this@Frofile).load(bitmap).apply(RequestOptions()).centerCrop().circleCrop().into(imageView)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    private fun imageToString(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imgByte = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imgByte, Base64.DEFAULT)
    }

}
