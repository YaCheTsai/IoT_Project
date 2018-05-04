package com.cwsu.client;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.cwsu.client.Setting.serverURL;


public class AuthorizedUserManagement extends AppCompatActivity {

    private String TAG = AuthorizedUserManagement.class.getSimpleName();
    private SwipeMenuListView swipeMenuListView;
    String uName, rName, uID, Gateway_ID;
    ArrayList<HashMap<String, String>> contactList;
    String selectedUserName;
    String selectedUserID;
    private RequestQueue queue;
    Button addNewUser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setTitle(R.string.authorized_user_management);
        setContentView(R.layout.activity_authorized_user_management);

        contactList = new ArrayList<>();
        swipeMenuListView = (SwipeMenuListView) findViewById(R.id.list);

        //get value
        Bundle bundle = getIntent().getExtras();
        uName = bundle.getString("uName");
        rName = bundle.getString("rName");
        uID = bundle.getString("uID");
        Gateway_ID = bundle.getString("Gateway_ID");
        initModule();
        initListener();
        generateSwipeMenu (uName,rName,uID);
    }

    private void initModule(){
        queue = Volley.newRequestQueue(AuthorizedUserManagement.this);
    }

    private void initListener(){
        addNewUser = (Button)findViewById(R.id.add_new_user);
        addNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("Gateway_ID",Gateway_ID);
                intent.setClass(AuthorizedUserManagement.this, AddNewAuthoUser.class);
                startActivity(intent);
            }
        });
    }

    private void generateSwipeMenu (String uName, String rName, String userID){
        try {
            generateContactList(uName, rName, userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ListAdapter adapter = new SimpleAdapter(AuthorizedUserManagement.this, contactList,
                R.layout.authorized_user_list, new String[]{"U.USER_NAME", "R.ROLE_NAME", "User_ID"},
                new int[]{R.id.name, R.id.role, R.id.userid});
        swipeMenuListView.setAdapter(adapter);
        //swipeMenuListView.setOnItemClickListener(new ListClickHandler());

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(170);
                // set item title
                openItem.setTitle("Edit");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_action_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        swipeMenuListView.setMenuCreator(creator);

        swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                //selected list item---user name
                HashMap<String, Object> item = (HashMap<String, Object>) swipeMenuListView.getItemAtPosition(position);
                selectedUserName = (String) item.get("U.USER_NAME");
                selectedUserID = (String) item.get("User_ID");
                switch (index) {
                    case 0:
                        // Edit user
                        Intent intent = new Intent(AuthorizedUserManagement.this, EditAuthorizedUser.class);
                        intent.putExtra("HOMEOWNER_ID", uID);
                        intent.putExtra("GATEWAY_ID", Gateway_ID);
                        intent.putExtra("SELECTED_ITEM", selectedUserName);
                        startActivity(intent);
                        break;
                    case 1:
                        // delete user


                        new DeleteUser().execute();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void generateContactList(String uName,String rName,String userID) throws JSONException {

        JSONArray nameArr = new JSONArray(uName);
        JSONArray roleArr = new JSONArray(rName);
        JSONArray idArr = new JSONArray(userID);
        for(int i=0 ; i<nameArr.length() ; i++){
            HashMap<String, String> contact = new HashMap<>();
            contact.put("U.USER_NAME", nameArr.get(i).toString());
            contact.put("R.ROLE_NAME", roleArr.get(i).toString());
            contact.put("User_ID", idArr.get(i).toString());
            contactList.add(contact);
        }
    }
    //delete user
    private class DeleteUser extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String deleteUidUrl = serverURL + "/userDeleteUID";
            JSONObject request = new JSONObject();
            try {
                request.put("User_ID", selectedUserID);
            } catch(Exception e) {
                e.printStackTrace();
            }
            JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, deleteUidUrl, request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject responseObj) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(AuthorizedUserManagement.this, "Done!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AuthorizedUserManagement.this, MenuActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MenuActivity.class));
        this.finish();
        super.onBackPressed();
    }
}
