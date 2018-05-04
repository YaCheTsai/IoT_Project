package com.cwsu.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.cwsu.client.Setting.serverURL;


public class GetDataActivity extends AppCompatActivity{

    private sessionManager sessionHelper= null;
    String dtype = "";
    String rorh = "";
    String URL = "";
    String GatewayID = "";
    String RoleID = "";

    Bundle bundle;
    RequestQueue queue;
    TextView txtSensor,txtYaxis;
    LineChart graphSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getdata);

        initModule();
        initLayout();
        bundle = GetDataActivity.this.getIntent().getExtras();
        dtype = bundle.getString("datatype");
        rorh = bundle.getString("rorh");
        GatewayID = bundle.getString("gatewayid");
        RoleID = bundle.getString("roleid");
        Log.i("datatype : ", dtype);
        txtSensor.setText(setDataType(dtype));
        txtYaxis.setText(setDataType2(dtype));
        if(rorh.equals("realtime")){
            URL = serverURL + "/getrealtimedata";
        }else{
            URL = serverURL + "/gethistorydata";
        }
        getData();
    }


    private void initModule(){
        queue = Volley.newRequestQueue(GetDataActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());
    }


    private void initLayout(){
        txtSensor = (TextView)findViewById(R.id.sensor);
        txtYaxis = (TextView)findViewById(R.id.Yaxis);
        graphSensor = (LineChart) findViewById(R.id.graph_sensor);
        graphSensor.setDescription("");
        configCharAxis(graphSensor);
    }


    private void getData(){
        final String UserId = sessionHelper.getUserID();
        String token = sessionHelper.getToken();
        Log.i("Get data token : ",token);
        JSONObject request = new JSONObject();
        try {
            request.put("UserID",UserId);
            request.put("utoken",token);
            request.put("datatype",dtype);
            request.put("roleid",RoleID);
            request.put("gatewayid",GatewayID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, URL, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        try {
                            String response = responseObj.getString("data");
                            generateGraph(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Can not post request to server1", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MenuVolleyError---", error.getMessage(), error);
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e("VolleyError body---->", new String(htmlBodyBytes), error);
                        Toast.makeText(getApplicationContext(), "Can not post request to server2", Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(strReq);
    }


    private void generateGraph(String response) throws JSONException {

        JSONArray obj = new JSONArray(response);

        //time list
        List<String> dateSensor = new ArrayList<>();
        //value list
        List<Entry> dataSensor = new ArrayList<>();

        for(int i=0; i<obj.length(); i++){
            obj.getJSONObject(i).getString("TimeStamp");
            dateSensor.add(obj.getJSONObject(i).getString("TimeStamp"));
            dataSensor.add(new Entry(Float.parseFloat(obj.getJSONObject(i).getString("Value")),i));
        }

        LineDataSet dataSetSensor = new LineDataSet(dataSensor, setDataType(dtype));
        LineData ldSensor = new LineData(dateSensor,dataSetSensor);
        graphSensor.setData(ldSensor);
        graphSensor.setData(ldSensor);
        graphSensor.setVisibility(View.VISIBLE);
    }


    private String setDataType(String type){
        switch(type){
            case "1":
                return "血氧感測裝置-血氧";
            case "2":
                return "心跳感測裝置-心跳";
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
    private String setDataType2(String type){
        switch(type){
            case "1":
                return "百分比";
            case "2":
                return "次數/分鐘";
            case "3":
                return "攝氏";
            case "4":
                return "百分比";
            case "5":
                return "微克/立方公尺";
            default:
                return null;
        }
    }


    private void configCharAxis(LineChart chartLine){
        XAxis xAxis = chartLine.getXAxis();
        xAxis.setTextSize(6);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis rYAxis = chartLine.getAxisRight();
        rYAxis.setEnabled(false);
    }

}