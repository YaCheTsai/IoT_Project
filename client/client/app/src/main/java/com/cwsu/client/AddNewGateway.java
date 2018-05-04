package com.cwsu.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import static com.cwsu.client.Setting.serverURL;

public class AddNewGateway extends AppCompatActivity {

    private EditText gatewayID;
    private Button SubmitButton, CancelButton;
    private RequestQueue queue;
    private sessionManager sessionHelper;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_gateway);
        setTitle(R.string.add_new_gateway);
        initModule();
        initLayout();
    }

    private void initModule(){
        queue = Volley.newRequestQueue(AddNewGateway.this);
        sessionHelper = new sessionManager(getApplicationContext());
    }

    private void initLayout(){

        Bundle bundle = getIntent().getExtras();
        userID = bundle.getString("UserID");
        gatewayID = (EditText) findViewById(R.id.gateway_id);

        SubmitButton = (Button) findViewById(R.id.add_button);
        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addNewGateway();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });

        CancelButton = (Button) findViewById(R.id.cancel_button);
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(AddNewGateway.this, RoleListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addNewGateway () throws NoSuchAlgorithmException {
        final String idText = gatewayID.getText().toString();
        final String homeownerID = userID;
        String authUrl = serverURL + "/insertNewGateway";

        //generate the data to be transmitted
        JSONObject request = new JSONObject();
        try {
            request.put("GID", idText);
            request.put("UID", homeownerID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //connect to 'authURL(Server)' ,send msg 'request' and get the response 'responseObj'
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, authUrl, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        try {
                            Toast.makeText(getApplicationContext(), "新增Gateway成功", Toast.LENGTH_SHORT).show();
                            //Intent intent = new Intent();
                            //intent.setClass(AddNewGateway.this, RoleListActivity.class);
                            //startActivity(intent);
                        } catch (Exception e) {
                            Log.i("gateway", e.toString());
                            Toast.makeText(getApplicationContext(), "新增Gateway失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError---", error.getMessage(), error);
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e("VolleyError body---->", new String(htmlBodyBytes), error);
                        Toast.makeText(getApplicationContext(), "Can not post request to server", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(strReq);

    }
}
