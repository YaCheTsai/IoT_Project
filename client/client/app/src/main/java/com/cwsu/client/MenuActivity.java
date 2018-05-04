package com.cwsu.client;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cwsu.client.Setting.serverURL;

/**
 * Created by cwsu on 2018/3/27.
 */

public class MenuActivity extends AppCompatActivity {

    Button bAuthorized_device, bBlutooth, bAuthorized_user_management;
    Intent intent;
    private Bundle bundle;
    private RequestQueue queue;
    private sessionManager sessionHelper;
    private String allrole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initModule();
        initLayout();
        initListener();
    }

    private void initModule(){
        queue = Volley.newRequestQueue(MenuActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());
        bundle = getIntent().getExtras();
        allrole = bundle.getString("Role");
    }

    private void initLayout(){
        bAuthorized_device  = (Button) findViewById(R.id.bAuthorized_device);
        bBlutooth           = (Button) findViewById(R.id.bBlutooth);

        bAuthorized_user_management  = (Button) findViewById(R.id.bAuthorized_user_management);
        Log.i("isAdmin",sessionHelper.getAdmin());
        if(sessionHelper.getAdmin().equals("no")){
            bAuthorized_user_management.setVisibility(8);
        }
    }
    private void initListener(){

        intent = new Intent();

        bAuthorized_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(MenuActivity.this, RoleListActivity.class);
                intent.putExtra("Role",allrole);
                startActivity(intent);
            }
        });
        bAuthorized_user_management.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userManagehUrl = serverURL + "/userManagement";
                JSONObject request = new JSONObject();
                try {
//                    request.put("GATEWAY_ID", GatewayID);
//                    Log.i("GATEWAY_ID" , GatewayID);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, userManagehUrl, request,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject responseObj) {
                                try {
                                    String uName = responseObj.getString("uName");
                                    String rName = responseObj.getString("rName");
                                    String uID = responseObj.getString("uID");
                                    intent.putExtra("uName",uName);
                                    intent.putExtra("rName",rName);
                                    intent.putExtra("uID",uID);
                                    intent.setClass(MenuActivity.this, AuthorizedUserManagement.class);
                                    startActivity(intent);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    System.out.print(e.toString());
                                    Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("VolleyError---", error.getMessage(), error);
                                byte[] htmlBodyBytes = error.networkResponse.data;
                                Log.e("VolleyError body---->", new String(htmlBodyBytes), error);
                                Toast.makeText(getApplicationContext(), "Can not get the error msg from server", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                queue.add(strReq);
            }
        });


    }

//    private Button bAuth_Role , bAdd_Gateway;
//    private Button bBody_Sensor, bEnvironment_Sensor;
//    private Button bAuth_User_Management, bAuth_device;
//    private RequestQueue queue;
//    private sessionManager sessionHelper;
//    String UserID, GatewayID, RoleID;
//    Intent intent;
//    Bundle b;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setTitle(R.string.menu_page);
//        setContentView(R.layout.activity_menu2);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        initModule();
//        initLayout();
//        initListener();
//
//    }
//    private void initModule(){
//        queue = Volley.newRequestQueue(MenuActivity.this);
//        sessionHelper = new sessionManager(getApplicationContext());
//        b = getIntent().getExtras();
//        UserID = sessionHelper.getUserID();
//        GatewayID = b.getString("Gateway_ID");
//        RoleID = b.getString("ROLE_ID");
//    }
//
//    private void initLayout(){
////        bAuth_Role             = (Button) findViewById(R.id.authorized_role);
////        bAdd_Gateway           = (Button) findViewById(R.id.add_gateway);
//        bBody_Sensor           =(Button) findViewById(R.id.body_sensor);
//        bEnvironment_Sensor    =(Button) findViewById(R.id.environment_sensor);
//        bAuth_User_Management  = (Button) findViewById(R.id.authorized_user_management);
//        bAuth_device           = (Button) findViewById(R.id.device_list);
//        if(sessionHelper.getAdmin().equals("No")){
//            bAuth_User_Management.setBackgroundColor(Color.parseColor("#747E80"));
//            bAuth_device.setBackgroundColor(Color.parseColor("#747E80"));
//            bAuth_User_Management.setEnabled(false);
//            bAuth_device.setEnabled(false);
//        }
//    }
//
//    private void initListener(){
//
//        intent = new Intent();
//        intent.putExtra("USER_ID",UserID);
//        intent.putExtra("Gateway_ID",GatewayID);
//
//        bBody_Sensor.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent.setClass(MenuActivity.this, SensorActivity.class);
//                String range = setRoleRange(RoleID);
//                intent.putExtra("ROLE_Range", range);
//                intent.putExtra("Role_ID",RoleID);
//                intent.putExtra("Sensor_Type","body");
//                startActivity(intent);
//            }
//        });
//
//        bEnvironment_Sensor.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent.setClass(MenuActivity.this, SensorActivity.class);
//                String range = setRoleRange(RoleID);
//                intent.putExtra("ROLE_Range", range);
//                intent.putExtra("Role_ID",RoleID);
//                intent.putExtra("Sensor_Type","environment");
//                startActivity(intent);
//            }
//        });
//
//        bAuth_User_Management.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String userManagehUrl = serverURL + "/userManagement";
//                JSONObject request = new JSONObject();
//                try {
//                    request.put("GATEWAY_ID", GatewayID);
//                    Log.i("GATEWAY_ID" , GatewayID);
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
//                JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, userManagehUrl, request,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject responseObj) {
//                                try {
//                                    String uName = responseObj.getString("uName");
//                                    String rName = responseObj.getString("rName");
//                                    String uID = responseObj.getString("uID");
//                                    intent.putExtra("uName",uName);
//                                    intent.putExtra("rName",rName);
//                                    intent.putExtra("uID",uID);
//                                    intent.setClass(MenuActivity.this, AuthorizedUserManagement.class);
//                                    startActivity(intent);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    System.out.print(e.toString());
//                                    Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                Log.e("VolleyError---", error.getMessage(), error);
//                                byte[] htmlBodyBytes = error.networkResponse.data;
//                                Log.e("VolleyError body---->", new String(htmlBodyBytes), error);
//                                Toast.makeText(getApplicationContext(), "Can not get the error msg from server", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                );
//                queue.add(strReq);
//
//            }
//        });
//
//
//    }
//
//    private String setRoleRange(String RoleID) {
//        switch(RoleID){
//            case "0":
//                return "1_1_1_1_1";
//            case "1":
//                return "1_1_0_0_0";
//            case "2":
//                return "1_0_1_0_0";
//            case "3":
//                return "1_0_1_0_1";
//            case "4":
//                return "1_1_1_1_1";
//            default:
//                return null;
//        }
//    }

}
