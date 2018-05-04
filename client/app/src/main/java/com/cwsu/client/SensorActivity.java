package com.cwsu.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class SensorActivity extends AppCompatActivity{

    TableLayout tableLayout;
    Bundle b;
    String GatewayID, RoleID, RoleRange;
    private RequestQueue queue;
    private sessionManager sessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        initModule();
        initLayout();
    }

    private void initModule(){
        queue = Volley.newRequestQueue(SensorActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());
        b = getIntent().getExtras();
        GatewayID = b.getString("Gateway_ID");
        RoleID = sessionHelper.getRole();
        RoleRange = setRoleRange(sessionHelper.getRole());
    }

    private void initLayout(){

        tableLayout = (TableLayout) findViewById(R.id.table_layout);
        String[] RoleRangeList = RoleRange.split("_");
        int i=0;
        int length = 0;
        if(GatewayID.equals("1")){
            i=0;
            length = 2;
        }else {
            i=2;
            length = 5;
        }
        for(; i<length ; i++){
            if(RoleRangeList[i].equals("1")){

                final TextView sensorTitle = new TextView(this);
                sensorTitle.setText(setDataType(String.valueOf(i+1)));
                sensorTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
                sensorTitle.setTag(i+1);
                tableLayout.addView(sensorTitle);

                final TableRow tableSensor = new TableRow(this);

                final Button btnSensorR = new Button(this);
                btnSensorR.setText("即時資料");
                btnSensorR.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                btnSensorR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SensorActivity.this,GetDataActivity.class);
                        intent.putExtra("datatype", sensorTitle.getTag().toString());
                        intent.putExtra("rorh", "realtime");
                        intent.putExtra("gatewayid",GatewayID);
                        intent.putExtra("roleid",RoleID);
                        startActivity(intent);
                    }
                });

                final Button btnSensorH = new Button(this);
                btnSensorH.setText("歷史資料");
                btnSensorH.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                btnSensorH.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SensorActivity.this,GetDataActivity.class);
                        intent.putExtra("datatype", sensorTitle.getTag().toString());
                        intent.putExtra("rorh", "history");
                        intent.putExtra("gatewayid",GatewayID);
                        intent.putExtra("roleid",RoleID);
                        startActivity(intent);
                    }
                });

                final Button btnSensorP = new Button(this);
                btnSensorP.setText("隱私政策");
                btnSensorP.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                btnSensorP.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                tableSensor.addView(btnSensorR);
                tableSensor.addView(btnSensorH);
                tableSensor.addView(btnSensorP);
                tableLayout.addView(tableSensor);
            }
        }

    }

    private String setDataType(String type){
        switch(type){
            case "1":
                return "血氧感測裝置";
            case "2":
                return "心跳感測裝置";
            case "3":
                return "溫度感測裝置";
            case "4":
                return "濕度感測裝置";
            case "5":
                return "PM2.5";
            default:
                return null;
        }
    }
    private String setRoleRange(String RoleID) {
        switch(RoleID){
            case "0":
                return "1_1_1_1_1";
            case "1":
                return "1_1_0_0_0";
            case "2":
                return "0_0_1_1_1";
            case "3":
                return "1_1_1_1_1";
            default:
                return null;
        }
    }

}
