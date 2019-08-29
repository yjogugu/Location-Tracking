package com.example.locationtracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.util.Log.d
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.locationtracking.RealService.serviceIntent
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_frand_list.*
import kotlinx.android.synthetic.main.activity_frofile.*
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import kotlinx.android.synthetic.main.content_main2.*
import kotlinx.android.synthetic.main.nav_header_main2.*
import kotlinx.android.synthetic.main.nav_header_main2.view.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.*

class Main2Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var gpsTracker: GpsTracker? = null
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    internal var REQUIRED_PERMISSIONS =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    val re: Retrofit = Retrofit.Builder()
        .client(OkHttpClient()).
            baseUrl(Sever_Service.url().server_url).build()
    val apiservice : Sever_Service = re.create(Sever_Service::class.java)
    var Main_user_name :String=""
    var Main_user_email:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val user_email2=intent.getStringExtra("login_email")

        val sharedPreferences = getSharedPreferences("sFile", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("email",user_email2)

        editor.apply()

        val sf = getSharedPreferences("sFile", Context.MODE_PRIVATE)


        val user_email1 =sf.getString("email","")


        Log.e("11111",user_email1)

        main2_user_email.text=user_email1

        val member_check = apiservice.member("member",user_email1)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting()
        } else {

            checkRunTimePermission()
        }


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val header:View = navView.getHeaderView(0)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

            gpsTracker = GpsTracker(this@Main2Activity)

            val latitude = gpsTracker!!.getLatitude()
            val longitude = gpsTracker!!.getLongitude()

            //val address = getCurrentAddress(latitude, longitude)
            //textview_address.setText(address)

            //Toast.makeText(this@Main2Activity, "현재위치 \n위도 $latitude\n경도 $longitude", Toast.LENGTH_LONG).show()

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        member_check.enqueue(object :Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("실패","실패")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val info = response.body()!!.string()
                val json_contact = JSONObject(info)
                val jsonarray_info: JSONArray = json_contact.getJSONArray("result")
                val size:Int = jsonarray_info.length()
                for (i in 0.. size-1) {
                    val json_objectdetail: JSONObject = jsonarray_info.getJSONObject(i)
                    Main_user_name =json_objectdetail.getString("id")
                    Main_user_email =json_objectdetail.getString("email")
                    header.title_name.text=Main_user_name //네비게이션바 헤드 타이틀 이름 부분
                    main2_user_name.text=Main_user_name
                    main2_user_email.text=Main_user_email
                        Handler().postDelayed({
                            Picasso.with(this@Main2Activity)
                                .load("URL/"+Main_user_email+".jpg")
                                .centerCrop()
                                .resize(400, 400)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .placeholder(R.drawable.baseline_account_box_black_18dp)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                .into(nav_header_img)
                        }, 500)

                    Log.e("dqwwdq",Main_user_email +"1"+main2_user_email.text.toString())

                    if (RealService.serviceIntent == null) {
                        RealService.serviceIntent = Intent(this@Main2Activity, RealService::class.java)
                        serviceIntent.putExtra("login_email",Main_user_email)
                        serviceIntent.putExtra("latitude",latitude)
                        serviceIntent.putExtra("longitude",longitude)
                        serviceIntent.putExtra("name",Main_user_name)
                        startService(RealService.serviceIntent)
                    } else {
                        RealService.serviceIntent = RealService.serviceIntent//getInstance().getApplication();
                        //Toast.makeText(applicationContext, "already", Toast.LENGTH_LONG).show()
                    }
                }
            }

        })

        button_lo.setOnClickListener {
            val index = apiservice.indext("local_situation","friend_list"
                ,user_email1,edittext_search.text.toString())


            index.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val ue = response.body()!!.string()
                    Log.e("ddd",ue)
                    if(edittext_search.text.toString().replace(" ","").equals("")){
                        Toast.makeText(this@Main2Activity,"검색창에 이름을 넣어주세요",Toast.LENGTH_SHORT).show()
                    }
                    else if(ue=="1"){
                        val builder = AlertDialog.Builder(ContextThemeWrapper(this@Main2Activity, R.style.Theme_AppCompat_Light_Dialog))
                        builder.setTitle("이름 확인")
                        builder.setMessage("해당 이름에 친구는 없습니다")
                        builder.setNegativeButton("확인") {dialog, id ->
                        }
                        builder.show()
                    }
                    else{
                        val jsonObject = JSONObject(ue) // json 사용방법
                        val jsonArray = jsonObject.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val subjsonObject = jsonArray.getJSONObject(i)
                            var latitude = subjsonObject.getDouble("latitude")
                            var longitude = subjsonObject.getDouble("longitude")
                            var user_name = subjsonObject.getString("name")
                            var friend_email = subjsonObject.getString("email")

                            intent = Intent(this@Main2Activity,Main3Activity::class.java)

                            intent.putExtra("name",user_name)
                            intent.putExtra("latitude",latitude)
                            intent.putExtra("longitude",longitude)
                            startActivity(intent)

                        }

                    }


                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("실패", "실패")

                }
            })

        }


    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("sFile", Context.MODE_PRIVATE)

        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
        val editor = sharedPreferences.edit()

        val email = main2_user_email.text.toString()

        editor.putString("email",email)
        //최종 커밋
        editor.apply()
        if (RealService.serviceIntent != null) {
            stopService(RealService.serviceIntent)
            RealService.serviceIntent = null
        }
    }



    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }*/


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { //네비게이션바
        val email =intent.getStringExtra("login_email")
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                intent =  Intent(this,FriendList::class.java)
                intent.putExtra("login_email",main2_user_email.text.toString())
                startActivity(intent)
                // Handle the camera action
            }


            R.id.nav_profile->{
                intent = Intent(this,Frofile::class.java)
                intent.putExtra("login_email",main2_user_email.text.toString())
                intent.putExtra("user_name",main2_user_name.text.toString())
                startActivity(intent)
                finish()
            }

        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    override fun onDestroy() { //백그라운드
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("sFile", Context.MODE_PRIVATE)

        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
        val editor = sharedPreferences.edit()
        val name = main2_user_name.text.toString() // 사용자가 입력한 저장할 데이터
        val email = main2_user_email.text.toString()
        editor.putString("name", name) // key, value를 이용하여 저장하는 형태
        editor.putString("email",email)
        //최종 커밋
        editor.apply()
        if (RealService.serviceIntent != null) {
            stopService(RealService.serviceIntent)
            RealService.serviceIntent = null
        }
    }

    internal fun checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@Main2Activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@Main2Activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@Main2Activity, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this@Main2Activity, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@Main2Activity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@Main2Activity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }

        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            GPS_ENABLE_REQUEST_CODE ->

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun showDialogForLocationServiceSetting() {

        val builder = AlertDialog.Builder(this@Main2Activity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?")
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, id -> dialog.cancel() }
        builder.create().show()
    }


}

