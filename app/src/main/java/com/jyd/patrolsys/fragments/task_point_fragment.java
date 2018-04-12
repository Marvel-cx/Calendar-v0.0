package com.jyd.patrolsys.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codbking.calendar.exaple.NFCActivity;
import com.codbking.calendar.exaple.R;
import com.jyd.patrolsys.utils.DesUtil;
import com.jyd.patrolsys.utils.HttpUtils;

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

/**
 * Created by caixiaoqi on 2018/4/10.
 */

public class task_point_fragment extends Fragment{
    private View view;
    private TextView txt_point;
    private String reqString;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();
    private String result;
    private Handler handler;
    private SimpleAdapter simp_adapter;
    private ListView mList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (view == null) {
            view = inflater.inflate(R.layout.task_point, container, false);
        }
        String MPTD_ID = getArguments().getString("MPTD_ID");
        mList=(ListView) view.findViewById(R.id.list_point);
        try {
            reqString= DesUtil.encryptData(MPTD_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getData();

        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
                        simp_adapter =new SimpleAdapter(getActivity(),dataList,R.layout.task_point_item,new String[]{"point","should_time","actual_time"},new int[]{R.id.point,R.id.should_time,R.id.actual_time});
                        mList.setAdapter(simp_adapter);
                        break;
                    default:
                        break;
                }
            }
        };

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String,Object> map = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String MPTDP_ID= (String) map.get("MPTDP_ID");
                String point_name=(String) map.get("point");
                Toast.makeText(getActivity(),"position="+i+MPTDP_ID,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), NFCActivity.class);
                Bundle bundle =new Bundle();
                bundle.putString("MPTDP_ID",MPTDP_ID);
                bundle.putString("point_name",point_name);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return view;
    }

    private List<Map<String,Object>> getData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL postUrl = new URL("http://192.168.1.122:8080/WService/Task/getTaskDayPoint.do?"+"key="+reqString);

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
                        JSONArray array =object.getJSONArray("resobj");
                        dataList.clear();
                        for(int i=0;i<array.size();i++){
                            JSONObject jo=array.getJSONObject(i);
                            Map<String,Object>map=new HashMap<String,Object>();
                            map.put("point",jo.getString("DOOR_NAME"));
                            map.put("should_time",jo.getString("SHOULD_TIME"));
                            map.put("actual_time",jo.getString("ACTUAL_TIME"));
                            map.put("MPTDP_ID",jo.getString("MPTDP_ID"));
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

    public static task_point_fragment newInstance(String text) {
        task_point_fragment fragment = new task_point_fragment();
        Bundle args = new Bundle();
        args.putString("MPTD_ID",text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        ((ViewGroup) view.getParent()).removeView(view);
    }
}
