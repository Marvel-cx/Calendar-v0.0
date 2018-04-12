package com.codbking.calendar.exaple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private SimpleAdapter simp_adapter;
    private List<Map<String,Object>>dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        listView =(ListView) findViewById(R.id.listView);
        dataList =new ArrayList<Map<String,Object>>();
        simp_adapter =new SimpleAdapter(this,getData(),R.layout.task_item,new String[]{"pic","text"},new int[]{R.id.pic,R.id.text});
        listView.setAdapter(simp_adapter);
        listView.setOnItemClickListener(this);
    }
    private List<Map<String,Object>> getData(){
        for (int i=0;i<20;i++){
           Map<String,Object>map=new HashMap<String,Object>();
           map.put("pic",R.mipmap.ic_launcher);
           map.put("text","巡更任务"+i);
           dataList.add(map);
        }
        return dataList;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String text=listView.getItemAtPosition(i)+"";
        Toast.makeText(this,"position="+i+" text="+text,Toast.LENGTH_SHORT).show();
    }
}
