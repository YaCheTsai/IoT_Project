package com.cwsu.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cwsu on 2018/3/27.
 */

public class RoleListActivity extends AppCompatActivity {

    private sessionManager sessionHelper;
    private Button AddGWButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.role_list);
        setContentView(R.layout.activity_role_list2);
        initModule();
        initLayout();
    }
    private void initModule(){
        sessionHelper = new sessionManager(getApplicationContext());
    }

    private void initLayout(){
        Bundle bundle = getIntent().getExtras();
        String allrole = bundle.getString("Role");

        try {
            generateRole(allrole);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void generateRole(String response) throws JSONException {

        JSONObject obj = new JSONObject(response);
        LinearLayout ll = (LinearLayout)findViewById(R.id.linear_Vertival_Role);

        JSONArray sensorArr = obj.getJSONArray(sessionHelper.getRole());

        final TextView tvTitle = new TextView(this);
        Log.i("role id", sessionHelper.getRole());
        tvTitle.setText(setRoleList(Integer.parseInt(sessionHelper.getRole())));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        tvTitle.setTextColor(Color.parseColor("#FCFCFC"));
        tvTitle.setBackgroundColor(Color.parseColor("#3949ab"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        tvTitle.setLayoutParams(params);
        params.setMargins(0,30,0,0);
        tvTitle.setPadding(0,20,0,20);
        tvTitle.setGravity(Gravity.CENTER);
        ll.addView(tvTitle);
        for(int j=0 ; j<sensorArr.length() ; j++){
            final TextView tvRole = new TextView(this);
            tvRole.setText(setGateway(sensorArr.getString(j)));
            tvRole.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            tvRole.setTextColor(Color.parseColor("#FCFCFC"));
            tvRole.setBackgroundColor(Color.parseColor("#9e9e9e"));
            //第一个参数为宽的设置，第二个参数为高的设置。
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            tvRole.setLayoutParams(params2);
            tvRole.setPadding(0,20,0,20);
            tvRole.setGravity(Gravity.CENTER);
            params.setMargins(0,30,0,0);
            tvRole.setTag(sensorArr.getString(j));
            tvRole.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RoleListActivity.this,SensorActivity.class);
                    intent.putExtra("Gateway_ID",tvRole.getTag().toString());
                    startActivity(intent);
                }
            });
            ll.addView(tvRole);
        }

    }

    private String setRoleList(int type){
        switch(type){
            case 0:
                return "Home Owner";
            case 1:
                return "醫護人員";
            case 2:
                return "訪客";
            case 3:
                return "家人";
            default:
                return null;
        }
    }

    private String setGateway(String type){
        switch(type){
            case "1":
                return "老人房間匣道器";
            case "2":
                return "客廳匣道器";
            default:
                return null;
        }
    }

}
