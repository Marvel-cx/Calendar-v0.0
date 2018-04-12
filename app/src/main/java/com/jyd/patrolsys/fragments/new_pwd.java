package com.jyd.patrolsys.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codbking.calendar.exaple.R;
import com.jyd.patrolsys.utils.DesUtil;
import com.jyd.patrolsys.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by caixiaoqi on 2018/4/9.
 */

public class new_pwd extends Fragment implements View.OnClickListener{
    private View view;
    private EditText old_pwd,new_pwd,re_pwd;
    private Button btn_sure;
    private String emp_id;
    private String emp_zh;
    private String reqString;
    private String result;
    private Handler handler;
    private TextView new_pwd_mention;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (view == null) {
            view = inflater.inflate(R.layout.tab_newpwd, container, false);
        }
        init();
        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
                        new_pwd_mention.setText(result);
                        break;
                    default:
                        break;
                }
            }
        };
        return view;
    }

    private void init() {
        old_pwd= (EditText) view.findViewById(R.id.old_pwd);
        new_pwd=(EditText) view.findViewById(R.id.new_pwd);
        re_pwd=(EditText) view.findViewById(R.id.re_pwd);
        new_pwd_mention=(TextView) view.findViewById(R.id.new_pwd_mention);
        btn_sure=(Button) view.findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(this);
        emp_id=getArguments().getString("emp_ID");
        emp_zh=getArguments().getString("emp_ZH");
    }

    public static new_pwd newInstance(String emp_id,String emp_zh) {
        new_pwd new_password =new new_pwd();
        Bundle args = new Bundle();
        args.putString("emp_ID",emp_id);
        args.putString("emp_ZH",emp_zh);
        new_password.setArguments(args);
        return new_password;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        ((ViewGroup) view.getParent()).removeView(view);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sure:
                String old_password=old_pwd.getText().toString();
                String new_password=new_pwd.getText().toString();
                String sure_password=re_pwd.getText().toString();
                try {
                    reqString= DesUtil.encryptData(emp_id+"||||"+emp_zh+"||||"+old_password+"||||"+new_password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL postUrl = new URL("http://192.168.1.122:8080/WService/Employee/updatePWD.do?"+"key="+reqString);

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
                break;
            default:
                break;
        }
    }
}
