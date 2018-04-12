package com.codbking.calendar.exaple;


import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.jyd.patrolsys.fragments.new_pwd;
import com.jyd.patrolsys.fragments.patrol_task_fragment;
import com.jyd.patrolsys.fragments.personal_center_fragment;
import com.jyd.patrolsys.fragments.submit_fragment;
import com.jyd.patrolsys.utils.DesUtil;
import com.jyd.patrolsys.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private RadioGroup radioGroup_bottom;
    private TextView txt;
    private Fragment patrol_task;
    private Fragment new_password;
    private Fragment submit;
    private Fragment personal_center;
    private String emp_id;
    private String emp_name;
    private String emp_sta_id;
    private String emp_zh;
    private String emp_phone;
    private LocationManager locationManager;
    private Location location;
    private Handler handler;
    double  lat=0.0;
    double  lng=0.0;
    private String addressStr;
    private String reqString;
    private String result;
    private JSONObject resultObject;
    private String rspData;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        /**
         * 初始控件
         */
        initView();
        /**
         * GPS板块
         */
        locationManager =(LocationManager) getSystemService(LOCATION_SERVICE);

        location = getBestLocation(locationManager);

        updateView(location);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, locationListener);

        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
//                      resultObject=com.alibaba.fastjson.JSONObject.parseObject(result);
//                      rspData=resultObject.getString("code");
                        //txt.setText(result);
                        break;
                    default:
                        break;
                }
            }
        };

    }

    private void initView() {
        radioGroup_bottom = (RadioGroup) findViewById(R.id.rg_radio_navigation_bottom);
        //txt = (TextView) findViewById(R.id.txt);
        findViewById(R.id.patrol_task).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.personal_center).setOnClickListener(this);
        patrol_task = new patrol_task_fragment();
        new_password=new new_pwd();
        submit = new submit_fragment();
        personal_center = new personal_center_fragment();
        radioGroup_bottom.check(R.id.patrol_task);
        addFragment(patrol_task);
        Bundle bundle =this.getIntent().getExtras();
        emp_id=bundle.getString("emp_id");
        emp_name=bundle.getString("emp_name");
        emp_sta_id=bundle.getString("emp_sta_id");
        emp_zh=bundle.getString("emp_zh");
        emp_phone=bundle.getString("emp_phone");
        patrol_task.setArguments(bundle);
        personal_center.setArguments(bundle);
        submit.setArguments(bundle);
    }

    @SuppressLint("MissingPermission")
    private Location getBestLocation(LocationManager locationManager) {
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                return location;
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000,5, locationListener);
                return location;
            }
        }
        return location;
    }

    LocationListener locationListener=new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            updateView(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateView(null);
        }

        @Override
        public void onLocationChanged(Location location) {
            location = getBestLocation(locationManager);// 每次都去获取GPS_PROVIDER优先的location对象
            updateView(location);
        }
    };

    private void updateView(Location location){
        if (location != null){
            StringBuffer sb = new StringBuffer();
            sb.append(location.getLongitude()+","+location.getLatitude());
            addressStr = "no address \n";
            lat = location.getLatitude();
            lng = location.getLongitude();
            try {
                reqString =DesUtil.encryptData(emp_id+"||||"+lng+"||||"+lat);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /**
             * GPS位置上报服务器
             */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL postUrl = new URL("http://192.168.1.122:8080/WService/Task/clockAddress.do?"+"key="+reqString);

                        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();

                        connection.setDoOutput(true);

                        connection.setDoInput(true);

                        connection.setRequestMethod("GET");

                        connection.setUseCaches(false);

                        connection.setInstanceFollowRedirects(true);

                        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                        connection.connect();

                        if(connection.getResponseCode() == 200){
                            InputStream is = connection.getInputStream();
                            result = HttpUtils.readMyInputStream(is);
                            result= DesUtil.decrypt(result);
                            connection.disconnect();
                        }
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(0);
                    Message msg =new Message();
                    msg.obj =result;
                    handler.sendMessage(msg);
                }
            }).start();
        }else{
            //txt.setText("no address");
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.patrol_task:
                addFragment(patrol_task);
                break;
            case R.id.submit:
                addFragment(submit);
                break;
            case R.id.personal_center:
                addFragment(personal_center);
                break;
            default:
                break;
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // 使用管理器开启事务
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        // 使用事务替换Fragment容器中Fragment对象
        fragmentTransaction.replace(R.id.framelayout, fragment);
        // 提交事务，否则事务不生效
        fragmentTransaction.commit();
    }


}
