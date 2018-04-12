package com.jyd.patrolsys.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codbking.calendar.exaple.R;
import com.jyd.patrolsys.utils.DesUtil;
import com.jyd.patrolsys.utils.HttpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

/**
 * Created by caixiaoqi on 2018/3/30.
 */

public class submit_fragment extends Fragment implements View.OnClickListener{
    private View view;
    private Unbinder unbinder;
    private static int REQ_1=1;
    private ImageView imageView;
    private String mFilePath;
    private Button submit;
    private String reqString;
    private String result;
    private Handler handler;
    private String emp_sta_id;
    private String emp_name;
    private String start_time;
    private String emp_phone;
    private String end_time;
    private String yema="1";
    private String tiaoshu="-1";
    private String task_id;
    private String task_name;
    private String emp_id;
    //private TextView submit_mention;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();
    @BindView(R.id.camera)
    ImageButton cameraBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (view == null) {
            view = inflater.inflate(R.layout.tab_submit, container, false);
        }
        unbinder = ButterKnife.bind(this,view);
        //submit_mention=(TextView) view.findViewById(R.id.submit_mention);
        imageView =(ImageView) getActivity().findViewById(R.id.iv);
        mFilePath = Environment.getExternalStorageDirectory().getPath();
        mFilePath =mFilePath+"/"+"temp.png";


        start_time="2018-04-12";
        end_time="2018-04-12";
        emp_sta_id=(String) getArguments().getSerializable("emp_sta_id");
        emp_id=(String) getArguments().getSerializable("emp_id");
        emp_name= (String) getArguments().getSerializable("emp_name");
        emp_phone=(String) getArguments().getSerializable("emp_phone");
        try {
            reqString=DesUtil.encryptData(start_time+"||||"+end_time+"||||"+emp_sta_id+"||||"+emp_name+"||||"+yema+"||||"+tiaoshu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getData();
        for(int i=0;i<dataList.size();i++){
            Map<String,Object> map =dataList.get(i);
            task_name=map.get("TASK_NAME").toString();
            task_id=map.get("MPTD_ID").toString();
        }
        submit =(Button) view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
                        //submit_mention.setText(result);
                        break;
                    default:
                        break;
                }
            }
        };
        return view;
    }

    @OnClick(R.id.camera)
    public void startCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri =Uri.fromFile(new File(mFilePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQ_1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQ_1){
                FileInputStream fis =null;
                try {
                    fis = new FileInputStream(mFilePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }finally{
                    try {
                        fis.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        //((ViewGroup) view.getParent()).removeView(view);
        unbinder.unbind();
    }

    /**
     * 事件上报
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submit:
                Map<String,Object> m = new HashMap<String, Object>();
                m.put("MPE_INFO", "测试1");
                m.put("MPE_TEL", emp_phone);
                m.put("MPE_TYPE", "治安");
                m.put("MPE_URL", "");
                m.put("MPE_ID", emp_id);
                m.put("MPTDP_ID", task_id);
                m.put("MPTDP_NAME", task_name);
                m.put("STATION_ID", emp_sta_id);
                try {
                    reqString= DesUtil.encryptData(JSON.toJSON(m).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL postUrl = new URL("http://192.168.1.122:8080/WService/Event/updateEvent.do?"+"key="+reqString);

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


    /**
     * 获取当日任务ID，任务名称
     */
    private List<Map<String,Object>> getData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL postUrl = new URL("http://192.168.1.122:8080/WService/Task/getListForPage.do?"+"key="+reqString);

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
                        JSONObject object = JSONObject.parseObject(result);
                        JSONObject resObject = object.getJSONObject("resobj");
                        JSONArray array =resObject.getJSONArray("rows");
                        dataList.clear();
                        for(int i=0;i<array.size();i++){
                            JSONObject jo=array.getJSONObject(i);
                            Map<String,Object>map=new HashMap<String,Object>();
                            map.put("TASK_NAME",jo.getString("MPTD_MPT_NAME"));
                            map.put("MPTD_ID",jo.getString("MPTD_ID"));
                            dataList.add(map);
                        }
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
        return dataList;
    }
}
