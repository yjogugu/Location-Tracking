package com.example.locationtracking

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Sever_Service {
     class url(){
        val server_url :String = "URL"
    }

    @FormUrlEncoded
    @POST("/wich/sing_up.php")
    fun postRequst(@Field("table_name")table_name:String, @Field("id")id:String, @Field("password")password:String, @Field("email")email:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/email_check.php")
    fun email_check(@Field("table_name")table_name:String,@Field("email")email:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/login_check.php")
    fun login_check(@Field("table_name")table_name:String,@Field("email")email:String , @Field("password")password:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/local_situation.php")
    fun local(@Field("table_name")table_name:String, @Field("latitude")latitude:Double,
              @Field("longitude")longitude:Double, @Field("email")email:String,@Field("name")name:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/member.php")
    fun member(@Field("table_name")table_name:String,@Field("email")email:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/uplode.php")
    fun uploadImage(@Field("table_name")table_name:String, @Field("title") title: String, @Field("image") image: String): Call<ImageClass>

    @FormUrlEncoded
    @POST("/wich/friend_list_add.php")
    fun friend_add(@Field("table_name")table_name:String, @Field("email")email:String, @Field("friend_email")friend_email:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/friend_list.php")
    fun friend_list(@Field("table_name")table_name:String,@Field("email")email:String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("/wich/index.php")
    fun indext(@Field("table_name")table_name:String,@Field("friend_table_name")friend_table_name:String,
               @Field("email")email:String, @Field("friend_name")friend_name:String) : Call<ResponseBody>

}