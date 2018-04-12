package com.codbking.calendar.exaple;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.jyd.patrolsys.utils.DesUtil;
import com.jyd.patrolsys.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
        private EditText userName, userPassword;
        private CheckBox rem_pw;
        private Button btn_login;
        private String userNameValue, passwordValue;
        private SharedPreferences sp;
        private SharedPreferences.Editor editor;
        private String reqString;
        private String result;
        private Handler handler;
        private JSONObject resultObject;
        private JSONObject idJsonObject;
        private String rspData;
        private String rspData2;
        private String rspData3;
        private String rspData4;
        private String rspData5;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        init();

        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
                        resultObject=com.alibaba.fastjson.JSONObject.parseObject(result);
                        rspData=resultObject.getString("code");
                        if(rspData.equals("40000")){
                            idJsonObject=resultObject.getJSONObject("resobj");
                            rspData2 =idJsonObject.getString("emp_sta_id");
                            rspData3 =idJsonObject.getString("emp_name");
                            rspData4 =idJsonObject.getString("emp_id");
                            rspData5 =idJsonObject.getString("emp_phone");
                            if (rem_pw.isChecked()) {
                                // 记住用户名，密码
                                // 由于SharedPreferences是一个接口，而且在这个接口里没有提供写入数据和读取数据的能力。
                                // 但它是通过其Editor接口中的一些方法来操作SharedPreference的,
                                editor.putString("USER_NAME", userNameValue);
                                editor.putString("PASSWORD", passwordValue);
                                editor.commit();
                            }else{
                                //账户密码正确, 未勾选记住用户名和密码功能，清除 SharedPreferences中的数据
                                editor.remove("userName");
                                editor.remove("userPassword");
                                editor.commit();
                            }
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            Bundle bundle=new Bundle();
                            bundle.putString("emp_sta_id",rspData2);
                            bundle.putString("emp_name",rspData3);
                            bundle.putString("emp_id",rspData4);
                            bundle.putString("emp_zh",userNameValue);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }else{

                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }


    /**
     * 初始化过程
     *
     * @return
     */
    private void init() {
        // 获得实例对象
        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor=sp.edit();
        userName = (EditText) findViewById(R.id.et_zh);
        userPassword = (EditText) findViewById(R.id.et_mima);
        rem_pw = (CheckBox) findViewById(R.id.cb_mima);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        rem_pw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(rem_pw.isChecked()){
                    sp.edit().putBoolean("ISCHECK",true).commit();
                }else{
                    sp.edit().putBoolean("ISCHECK",false).commit();
                }
            }
        });
        //记住密码初始
        if (sp.getBoolean("ISCHECK", true)){
            rem_pw.setChecked(true);
            userName.setText(sp.getString("USER_NAME", ""));
            userPassword.setText(sp.getString("PASSWORD", ""));
        }
    }

    /**
     * 登录按钮事件监听
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                userNameValue = userName.getText().toString();
                passwordValue = userPassword.getText().toString();
                UUID uuid = UUID.randomUUID();
                try {
                    reqString = DesUtil.encryptData(userNameValue+"||||"+passwordValue+"||||"+uuid+"");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL postUrl = new URL("http://192.168.1.122:8080/WService/Employee/login.do?"+"key="+reqString);

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
                                result=DesUtil.decrypt(result);
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
                break;
            default:
                break;
        }
    }

}
