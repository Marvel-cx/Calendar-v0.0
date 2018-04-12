package com.jyd.patrolsys.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.codbking.calendar.CaledarAdapter;
import com.codbking.calendar.CalendarBean;
import com.codbking.calendar.CalendarDateView;
import com.codbking.calendar.CalendarUtil;
import com.codbking.calendar.CalendarView;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.jyd.patrolsys.fragments.Utils.px;

/**
 * Created by caixiaoqi on 2018/3/29.
 */

public class patrol_task_fragment extends Fragment {
    private View view;
    private Unbinder unbinder;
    private SimpleAdapter simp_adapter;
    private String emp_sta_id;
    private String emp_name;
    private String reqString;
    private String start_time;
    private String end_time;
    private String yema="1";
    private String tiaoshu="-1";
    private Handler handler;
    private String result;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();
    @BindView(R.id.calendarDateView)
    CalendarDateView mCalendarDateView;
    @BindView(R.id.list)
    ListView mList;
    @BindView(R.id.title)
    TextView mTitle;
    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (view == null) {
            view = inflater.inflate(R.layout.activity_main, container, false);
        }
        unbinder= ButterKnife.bind(this,view);
        /**
         * 执行人，站点ID，开始时间，结束时间等参数；
         */
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar calendar = Calendar.getInstance();
//        start_time=sf.format(calendar.getTime()).toString();
//        calendar.add(Calendar.DAY_OF_MONTH,2);
//        end_time=sf.format(calendar.getTime()).toString();
        start_time="2018-04-09";
        end_time="2018-04-15";
        emp_sta_id=(String) getArguments().getSerializable("emp_sta_id");
        emp_name= (String) getArguments().getSerializable("emp_name");
        try {
            reqString=DesUtil.encryptData(start_time+"||||"+end_time+"||||"+emp_sta_id+"||||"+emp_name+"||||"+yema+"||||"+tiaoshu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getData();
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text=mList.getItemAtPosition(i)+"";
                HashMap<String,Object> map = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String MPTD_ID= (String) map.get("MPTD_ID");
                Toast.makeText(getActivity(),"position="+i+MPTD_ID,Toast.LENGTH_SHORT).show();
                task_point_fragment point_fragment=task_point_fragment.newInstance(MPTD_ID);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                //transaction.add(R.id.framelayout, point_fragment);
                transaction.replace(R.id.framelayout,point_fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        mCalendarDateView.setAdapter(new CaledarAdapter() {
            @Override
            public View getView(View convertView, ViewGroup parentView, CalendarBean bean) {
                TextView txtview;
                if (convertView == null) {
                    convertView = LayoutInflater.from(parentView.getContext()).inflate(R.layout.item_calendar,null);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(px(48), px(48));
                    convertView.setLayoutParams(params);
                }

                txtview = (TextView) convertView.findViewById(R.id.text);

                txtview.setText("" + bean.day);
                if (bean.mothFlag != 0) {
                    txtview.setTextColor(0xff9299a1);
                } else {
                    txtview.setTextColor(0xffffffff);
                }
                return convertView;
            }
        });

        mCalendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, CalendarBean bean) {
                mTitle.setText(bean.year + "/" + getDisPlayNumber(bean.moth) + "/" + getDisPlayNumber(bean.day));
                String req_date=bean.year+"-"+getDisPlayNumber(bean.moth)+"-"+getDisPlayNumber(bean.day);
                Toast.makeText(getActivity(),req_date,Toast.LENGTH_SHORT).show();
                try {
                    reqString=DesUtil.encryptData(req_date+"||||"+req_date+"||||"+emp_sta_id+"||||"+emp_name+"||||"+yema+"||||"+tiaoshu);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                                    map.put("pic",R.mipmap.ready);
                                    map.put("text",jo.getString("MPTD_MPT_NAME"));
                                    map.put("task_time",jo.getString("MPTD_DATETIME"));
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
            }
        });

        int[] data = CalendarUtil.getYMD(new Date());
        mTitle.setText(data[0] + "/" + data[1] + "/" + data[2]);

        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
                        simp_adapter =new SimpleAdapter(getActivity(),dataList,R.layout.task_item,new String[]{"pic","text","task_time"},new int[]{R.id.pic,R.id.text,R.id.task_time});
                        mList.setAdapter(simp_adapter);
                        break;
                    default:
                        break;
                }
            }
        };
        return view;
}


    private String getDisPlayNumber(int num) {
        return num < 10 ? "0" + num : "" + num;
    }

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
                            map.put("pic",R.mipmap.ready);
                            map.put("text",jo.getString("MPTD_MPT_NAME"));
                            map.put("task_time",jo.getString("MPTD_DATETIME"));
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

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        ((ViewGroup) view.getParent()).removeView(view);
        unbinder.unbind();
    }
}
