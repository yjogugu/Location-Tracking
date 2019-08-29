package com.example.locationtracking;

import android.app.Service;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RealService extends Service {
    private Thread mainThread;
    public static Intent serviceIntent = null;
    static public String email,name;
    static public Sever_Service apiservice;
    static public Retrofit re;
    static public double latitude,longitude;//위도 경도
    Call<ResponseBody> comment;
    public RealService() {


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
        GpsTracker gpsTracker;
        gpsTracker = new GpsTracker(this);

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();


        email = serviceIntent.getStringExtra("login_email");
        //latitude = serviceIntent.getDoubleExtra("latitude",0);
        //longitude = serviceIntent.getDoubleExtra("longitude",0);
        name = serviceIntent.getStringExtra("name");
        SharedPreferences sf = getSharedPreferences("sFile", Context.MODE_PRIVATE);

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(name)){
            name = sf.getString("name", "");
            email= sf.getString("email","");
        }
        //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
         //name = sf.getString("name", "");
         //email= sf.getString("email","");
        //resultText1.text=text+text2

        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final SimpleDateFormat sdf = new SimpleDateFormat("aa hh:mm");
                boolean run = true;
                while (run) {
                    try {
                        Thread.sleep(1000 * 30 * 1); // 1 minute
                        re =  new Retrofit.Builder()
                                .client(new OkHttpClient())
                                .baseUrl("URL").build();
                        apiservice = re.create(Sever_Service.class);
                        //email = intent.getStringExtra("login_email");
                        comment = apiservice.local("local_situation",latitude,longitude,email,name);
                        final Date date = new Date();
                        comment.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                sendNotification(sdf.format(date),email);
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("실패","실패");

                            }
                        });
                        //showToast(getApplication(), sdf.format(date)); 시간 토스트


                    } catch (InterruptedException e) {
                        run = false;
                        e.printStackTrace();
                    }
                }
            }
        });
        mainThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        serviceIntent = null;
        setAlarmTimer();
        Thread.currentThread().interrupt();

        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    private void sendNotification(String messageBody , String email) {
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("login_email",email);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle("위도 경도가 설정 되었습니다.")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
