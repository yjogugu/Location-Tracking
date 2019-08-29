package com.example.locationtracking


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Window
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*


class Main3Activity : FragmentActivity(),OnMapReadyCallback{

    var goolgmop : GoogleMap? = null
    private val TAG = "googlemap_example"
    private val GPS_ENABLE_REQUEST_CODE = 2001
    internal var needRequest = false


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        //showDialogForLocationServiceSetting()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this) // getMapAsync must be called on the main thread.

    }

    override fun onMapReady(p0: GoogleMap?) {
        goolgmop = p0
        val latitude = intent.getDoubleExtra("latitude",0.0)
        val longitude= intent.getDoubleExtra("longitude",0.0)
        val name= intent.getStringExtra("name")

        oneMarker(latitude,longitude,name)

    }




    fun oneMarker(latitude:Double , longitude:Double , name:String) {
        // 서울 여의도에 대한 위치 설정
        val seoul = LatLng(latitude, longitude)
        getCurrentAddress(seoul)

        // 구글 맵에 표시할 마커에 대한 옵션 설정  (알파는 좌표의 투명도이다.)
        val makerOptions = MarkerOptions()
        makerOptions
            .position(seoul)
            .title(name)
            .snippet(getCurrentAddress(seoul))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        // 마커를 생성한다. showInfoWindow를 쓰면 처음부터 마커에 상세정보가 뜨게한다. (안쓰면 마커눌러야뜸)
        goolgmop!!.addMarker(makerOptions) //.showInfoWindow();

        //정보창 클릭 리스너
        goolgmop!!.setOnInfoWindowClickListener(infoWindowClickListener)

        //마커 클릭 리스너
        goolgmop!!.setOnMarkerClickListener(markerClickListener)

        //카메라를 마커 위치로 옮긴다.
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        //처음 줌 레벨 설정 (해당좌표=>서울, 줌레벨(16)을 매개변수로 넣으면 된다.) (위에 코드대신 사용가능)(중첩되면 이걸 우선시하는듯)
        goolgmop!!.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 17f))

        goolgmop!!.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener {
            Toast.makeText(this@Main3Activity, "눌렀습니다!!", Toast.LENGTH_LONG)
            false
        })


    }

    fun getCurrentAddress(latlng: LatLng): String { //지오코딩 위도,경도 주소로변환

        //지오코더... GPS를 주소로 변환
        val geocoder = Geocoder(this, Locale.KOREA)

        val addresses: List<Address>?

        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )

        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"

        }


        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show()
            return "주소 미발견"

        } else {
            val address = addresses[0]
            Log.e("dd", address.getAddressLine(0))
            return address.getAddressLine(0).toString()
        }

    }

    internal var infoWindowClickListener: GoogleMap.OnInfoWindowClickListener =
        GoogleMap.OnInfoWindowClickListener { marker ->
            val markerId = marker.id
            Toast.makeText(this@Main3Activity, "정보창 클릭 Marker ID : $markerId", Toast.LENGTH_SHORT).show()
        }

    //마커 클릭 리스너
    internal var markerClickListener: GoogleMap.OnMarkerClickListener =
        GoogleMap.OnMarkerClickListener { marker ->
            val markerId = marker.id
            //선택한 타겟위치
            val location = marker.position
            Toast.makeText(
                this@Main3Activity,
                "마커 클릭 Marker ID : " + markerId + "(" + location.latitude + " " + location.longitude + ")",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {

        val builder = AlertDialog.Builder(this@Main3Activity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?")
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, id -> dialog.cancel() }
        builder.create().show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            GPS_ENABLE_REQUEST_CODE ->

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")


                        needRequest = true

                        return
                    }
                }
        }
    }


}
