package com.cwsu.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class EditAuthorizedUser extends AppCompatActivity {

    String HOMEOWNER_ID,GATEWAY_ID,USER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.edit_authorized_user);
        setContentView(R.layout.activity_edit_authorized_user);

        Bundle bundle = getIntent().getExtras();
        HOMEOWNER_ID = bundle.getString("HOMEOWNER_ID");
        GATEWAY_ID = bundle.getString("GATEWAY_ID");
        USER_NAME = bundle.getString("SELECTED_ITEM");
        TextView name = (TextView) findViewById(R.id.name);
        name.setText(USER_NAME);
        TextView gatewayID = (TextView) findViewById(R.id.gatewayID);
        gatewayID.setText(GATEWAY_ID);
    }
}
