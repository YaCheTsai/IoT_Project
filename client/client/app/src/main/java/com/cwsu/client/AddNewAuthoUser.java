package com.cwsu.client;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import util.Calculate;

import static com.cwsu.client.Setting.serverURL;

public class AddNewAuthoUser extends AppCompatActivity {
    private Spinner roleSpinner;
    private RequestQueue queue;
    ArrayList<String> roleList = new ArrayList<String>();
    EditText startDate, endDate;
    EditText startTime, endTime;
    Button btnCancel, btnAddNewUser;
    Context context = this;
    Calendar startDateCalendar = Calendar.getInstance();
    Calendar endDateCalendar = Calendar.getInstance();
    Calendar startTimeCalendar = Calendar.getInstance();
    Calendar endTimeCalendar = Calendar.getInstance();
    String dateFormat = "yyyy/MM/dd";
    SimpleDateFormat dateSDF = new SimpleDateFormat(dateFormat, Locale.GERMAN);
    String ROLE_ID, GATEWAY_ID, START_DATE, END_DATE, START_TIME, END_TIME;
    //Boolean UserIDValid=false;
    String checkAccountmsg = "";
    String SELECTED_ROLE = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_new_user);
        setContentView(R.layout.activity_add_new_autho_user);

        initModule();
        initLayout();
        initListener();

        //Set date to current date
        long currentdate = System.currentTimeMillis();
        String dateString = dateSDF.format(currentdate);
        startDate.setText(dateString);
        endDate.setText(dateString);
        //Set time to current time
        Date date = new Date();
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        startTime.setText(timeFormat.format(date));
        endTime.setText(timeFormat.format(date));
        // onclick - popup datepicker and timepicker
        startDate.setOnClickListener(DateListener);
        endDate.setOnClickListener(DateListener);
        startTime.setOnClickListener(TimeListener);
        endTime.setOnClickListener(TimeListener);


    }

    public void Cancel(View view) {
        //Intent intent = new Intent(AddNewAuthoUser.this, HomePage.class);
        //startActivity(intent);
    }

    public void AddAuthoUser(View view) {

        START_DATE = ChangeDateFormat(((EditText) findViewById(R.id.startDate)).getText().toString());
        END_DATE = ChangeDateFormat(((EditText) findViewById(R.id.endDate)).getText().toString());
        START_TIME = ChangeTimeFormat(((EditText) findViewById(R.id.startTime)).getText().toString());
        END_TIME = ChangeTimeFormat(((EditText) findViewById(R.id.endTime)).getText().toString());


    }

    private void initModule() {
        queue = Volley.newRequestQueue(AddNewAuthoUser.this);
        Bundle bundle = getIntent().getExtras();
        GATEWAY_ID = bundle.getString("Gateway_ID");

    }

    private void initLayout() {
        btnCancel = (Button) findViewById(R.id.cancel_button);
        btnAddNewUser = (Button) findViewById(R.id.next_step_button);
        roleSpinner = (Spinner) findViewById(R.id.role_spinner);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        startTime = (EditText) findViewById(R.id.startTime);
        endTime = (EditText) findViewById(R.id.endTime);
        START_DATE = ((EditText) findViewById(R.id.startDate)).getText().toString();
        roleList.add("醫護人員");
        roleList.add("照服員");
        roleList.add("訪客");
        roleList.add("家人");
        ArrayAdapter adapter = new ArrayAdapter(AddNewAuthoUser.this, android.R.layout.simple_spinner_item, roleList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

    }

    private void initListener() {


        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(AddNewAuthoUser.this, "selected role is  "+roleList.get(position), Toast.LENGTH_SHORT).show();
                //SELECTED_ROLE = roleSpinner.getItemAtPosition(position).toString().toString();
                SELECTED_ROLE = roleList.get(position);
                Log.i("Role id : ",SELECTED_ROLE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //optionally do something here
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnAddNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                START_DATE = ChangeDateFormat(((EditText) findViewById(R.id.startDate)).getText().toString());
                END_DATE = ChangeDateFormat(((EditText) findViewById(R.id.endDate)).getText().toString());
                START_TIME = ChangeTimeFormat(((EditText) findViewById(R.id.startTime)).getText().toString());
                END_TIME = ChangeTimeFormat(((EditText) findViewById(R.id.endTime)).getText().toString());
                final String USER_ID = ((EditText) findViewById(R.id.user_id)).getText().toString();

                ROLE_ID = setRoleId(SELECTED_ROLE);

                String authUrl = serverURL + "/insertNewAuthUser";
                String FROM_DATE = START_DATE + " " + START_TIME;
                String EXPIRE_DATE = END_DATE + " " + END_TIME;

                //generate the data to be transmitted
                JSONObject request = new JSONObject();
                try {
                    request.put("USER_ID", USER_ID);
                    request.put("FROM_DATE", FROM_DATE);
                    request.put("EXPIRE_DATE", EXPIRE_DATE);
                    request.put("GATEWAY_ID", GATEWAY_ID);
                    request.put("ROLE_ID", ROLE_ID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //connect to 'authURL(Server)' ,send msg 'request' and get the response 'responseObj'
                JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, authUrl, request,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject responseObj) {
                                try {
                                    Toast.makeText(getApplicationContext(), "新增角色成功", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.i("auth", e.toString());
                                    Toast.makeText(getApplicationContext(), "新增角色失敗", Toast.LENGTH_SHORT).show();
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
        });


    }

    private String setRoleId(String role) {
        switch (role) {

            case "醫護人員":
                return "1";
            case "照服員":
                return "2";
            case "訪客":
                return "3";
            case "家人":
                return "4";
            default:
                return null;
        }
    }

    private View.OnClickListener DateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.startDate:
                    new DatePickerDialog(context, startDateSetListener, startDateCalendar
                            .get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH),
                            startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    break;
                case R.id.endDate:
                    new DatePickerDialog(context, endDateSetListener, endDateCalendar
                            .get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH),
                            endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    break;
            }
        }
    };
    //start date listener
    private DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, monthOfYear);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            //update
            startDate.setText(dateSDF.format(startDateCalendar.getTime()));
        }
    };
    //end date listener
    private DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, monthOfYear);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            //update
            endDate.setText(dateSDF.format(endDateCalendar.getTime()));
        }
    };

    private View.OnClickListener TimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.startTime:
                    new TimePickerDialog(context, startTimeSetListener, startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)).show();
                    break;
                case R.id.endTime:
                    new TimePickerDialog(context, endTimeSetListener, endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)).show();
                    break;
            }
        }
    };
    //start time listener
    private TimePickerDialog.OnTimeSetListener startTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
            java.text.DateFormat timeSDF = android.text.format.DateFormat.getTimeFormat(context);
            Date date = new Date();
            date.setHours(hourOfDay);
            date.setMinutes(minuteOfHour);
            startTime.setText(timeSDF.format(date));
        }
    };
    //end time listener
    private TimePickerDialog.OnTimeSetListener endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
            java.text.DateFormat timeSDF = android.text.format.DateFormat.getTimeFormat(context);
            Date date = new Date();
            date.setHours(hourOfDay);
            date.setMinutes(minuteOfHour);
            endTime.setText(timeSDF.format(date));
        }
    };


    //Change date format
    private String ChangeDateFormat(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date _date = inputFormat.parse(date);
            return outputFormat.format(_date);
        } catch (ParseException pe) {
            return "Date";
        }
    }

    //Change time format
    private String ChangeTimeFormat(String time) {
        java.text.DateFormat inputFormat = android.text.format.DateFormat.getTimeFormat(context);
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss");
        try {
            Date _date = inputFormat.parse(time);
            return outputFormat.format(_date);
        } catch (ParseException pe) {
            return "Date";
        }
    }

}
