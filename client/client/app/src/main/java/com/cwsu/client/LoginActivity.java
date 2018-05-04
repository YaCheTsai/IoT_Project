package com.cwsu.client;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import static util.Calculate.sha256;

public class LoginActivity extends AppCompatActivity {

    private EditText loginUserID, loginPW;
    private Button SignInButton, registerButton, forgetPWButton;
    private TextView condition;
    private RequestQueue queue;
    private sessionManager sessionHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.app_name);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        initModule();
        initLayout();
    }

    private void initModule(){
        queue = Volley.newRequestQueue(LoginActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());

    }

    private void initLayout(){
        loginUserID = (EditText) findViewById(R.id.user_id);
        loginPW = (EditText) findViewById(R.id.password);
        condition = (TextView)findViewById(R.id.condition);

        SignInButton = (Button) findViewById(R.id.login_button);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    loginBy4G();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });
        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        forgetPWButton = (Button) findViewById(R.id.forget_password_button);
        forgetPWButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, ForgetPWActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loginBy4G () throws NoSuchAlgorithmException {
        final String idText = loginUserID.getText().toString();
        final String pwText = loginPW.getText().toString();
        String authUrl = serverURL + "/login";
        String hPW = sha256(pwText);

        //generate the data to be transmitted
        JSONObject request = new JSONObject();
        try {
            request.put("UID", idText);
            request.put("PW", pwText);
            request.put("hPW",hPW);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //connect to 'authURL(Server)' ,send msg 'request' and get the response 'responseObj'
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, authUrl, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObj) {
                        try {
                            String Login = responseObj.getString("login");
                            if(Login.equals("yes")){
                                String Token = responseObj.getString("token");
                                String allrole = responseObj.getString("Role");
                                String roleID = responseObj.getString("RoleID");
                                String isAdmin = responseObj.getString("isAdmin");
                                Log.i("Login Role : ",allrole);
                                Log.i("Login Token : ",Token);
                                Log.i("Login isAdmin : ",isAdmin);
                                sessionHelper.setToken(Token);
                                sessionHelper.setUserID(idText);
                                sessionHelper.setAdmin(isAdmin);
                                sessionHelper.setRole(roleID);
                                Intent intent = new Intent();
                                intent.putExtra("Role", allrole);
                                intent.putExtra("UserID",idText);
                                intent.setClass(LoginActivity.this, MenuActivity.class);
                                //intent.setClass(LoginActivity.this, RoleListActivity.class);
                                startActivity(intent);
                            }

                        } catch (Exception e) {
                            Log.i("auth", e.toString());
                            Toast.makeText(getApplicationContext(), "authenticated fail", Toast.LENGTH_SHORT).show();
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


