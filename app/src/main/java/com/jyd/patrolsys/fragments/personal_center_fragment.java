package com.jyd.patrolsys.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.codbking.calendar.exaple.R;


/**
 * Created by caixiaoqi on 2018/3/30.
 */

public class personal_center_fragment extends Fragment {
    private View view;
    private LinearLayout my_submit,reset_pwd;
    private Fragment new_password;
    private String emp_ID;
    private String emp_ZH;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (view == null) {
            view = inflater.inflate(R.layout.tab_personal_center, container, false);
        }
        emp_ID=(String) getArguments().getSerializable("emp_id");
        emp_ZH= (String) getArguments().getSerializable("emp_zh");
        my_submit=(LinearLayout) view.findViewById(R.id.my_submit);
        reset_pwd=(LinearLayout) view.findViewById(R.id.reset_pwd);
        new_password =new new_pwd();
        reset_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_password=new_pwd.newInstance(emp_ID,emp_ZH);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                //transaction.add(R.id.framelayout, point_fragment);
                transaction.replace(R.id.framelayout,new_password);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        ((ViewGroup) view.getParent()).removeView(view);
    }
}
