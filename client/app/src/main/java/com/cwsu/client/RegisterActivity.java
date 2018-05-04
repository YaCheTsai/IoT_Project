package com.cwsu.client;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

import static com.cwsu.client.Setting.serverURL;
import static util.Calculate.sha256;



public class RegisterActivity extends AppCompatActivity{


    EditText signupId, signupPw, signupConfirmPw, signupName, signupEmail;
    Button btnDoSignup, btnCancelSignup;
    Spinner roleSpinner;
    RequestQueue queue;
    ArrayList<String> roleList = new ArrayList<String>();
    private sessionManager sessionHelper;
    String signupSelectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initModule();
        initLayout();
        initListener();
    }


    private void initModule(){
        queue = Volley.newRequestQueue(RegisterActivity.this);
        sessionHelper = new sessionManager(getApplicationContext());
    }


    private void initLayout() {
        signupId = (EditText) findViewById(R.id.signup_uid);
        signupPw = (EditText) findViewById(R.id.signup_pw);
        signupConfirmPw = (EditText) findViewById(R.id.signup_confirm_pw);
        signupName = (EditText)findViewById(R.id.signup_name) ;
        signupEmail = (EditText) findViewById(R.id.signup_email);
        btnDoSignup = (Button) findViewById(R.id.action_register_button);
        btnCancelSignup = (Button) findViewById(R.id.cancel_button);
        roleSpinner = (Spinner) findViewById(R.id.signup_role_spinner);
        roleList.add("Homeowner");
        roleList.add("醫護人員");
        roleList.add("訪客");
        roleList.add("家人");
        ArrayAdapter adapter = new ArrayAdapter(RegisterActivity.this, android.R.layout.simple_spinner_item, roleList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
    }


    private void initListener() {

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                signupSelectedRole = setRoleId(roleList.get(position));
                Log.i("Role id : ",signupSelectedRole);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //optionally do something here
            }
        });

        btnDoSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = serverURL + "/register";
                final String signupIdText = signupId.getText().toString();
                String signupPwText = signupPw.getText().toString();
                String signupConfirmPwText = signupConfirmPw.getText().toString();
                String signupNameText = signupName.getText().toString();
                String signupEmailText = signupEmail.getText().toString();
                String hPW = "";
                try {
                    hPW = sha256(signupPwText);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                JSONObject request = new JSONObject();
                if(signupPwText.equals(signupConfirmPwText)){
                    try {
                        request.put("RegisterData", signupIdText+"_"+signupPwText+"_"+signupEmailText+"_"+signupNameText+"_"+signupSelectedRole);
                        request.put("hPW", hPW);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, url, request,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject responseObj) {
                                    try {
                                        String response = responseObj.getString("response");
                                        if(response.equals("yes")){
                                            Toast.makeText(getApplicationContext(), "register success", Toast.LENGTH_SHORT).show();
                                            RegisterActivity.this.finish();
                                            String token = responseObj.getString("token");
                                            String PK = responseObj.getString("PK");
                                            String SK = responseObj.getString("SK");
                                            Log.i("PK :", PK);
                                            Log.i("SK :", SK);

                                            sessionHelper.setUserID(signupIdText);
                                            sessionHelper.setToken(token);
                                            Log.i("token ",token);
                                        }else{
                                            Toast.makeText(getApplicationContext(), "id already exist", Toast.LENGTH_SHORT).show();
                                            signupId.setText("");
                                            signupPw.setText("");
                                            signupConfirmPw.setText("");
                                        }
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
                }else{
                    Toast.makeText(getApplicationContext(), "password and confirm password are not equal", Toast.LENGTH_SHORT).show();
                    signupPw.setText("");
                    signupConfirmPw.setText("");
                }
            }
        });

        btnCancelSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });
    }

    private String setRoleId(String role) {
        switch (role) {

            case "Homeowner":
                return "0";
            case "醫護人員":
                return "1";
            case "訪客":
                return "2";
            case "家人":
                return "3";
            default:
                return null;
        }
    }

//    private void savePKSK (String PK, String SK){
//        try {
//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//
//            String alias = "key3";
//
//            int nBefore = keyStore.size();
//
//            // Create the keys if necessary
//            if (!keyStore.containsAlias(alias)) {
//
//                Calendar notBefore = Calendar.getInstance();
//                Calendar notAfter = Calendar.getInstance();
//                notAfter.add(Calendar.YEAR, 1);
//                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
//                        .setAlias(alias)
//                        .setKeyType("RSA")
//                        .setKeySize(2048)
//                        .setSubject(new X500Principal("CN=test"))
//                        .setSerialNumber(BigInteger.ONE)
//                        .setStartDate(notBefore.getTime())
//                        .setEndDate(notAfter.getTime())
//                        .build();
//                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
//                generator.initialize(spec);
//
//                KeyPair keyPair = generator.generateKeyPair();
//            }
//            int nAfter = keyStore.size();
//            Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);
//
//            // Retrieve the keys
//            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
//            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();
//            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
//
//            Log.v(TAG, "private key = " + privateKey.toString());
//            Log.v(TAG, "public key = " + publicKey.toString());
//
//            // Encrypt the text
//            String plainText = "This text is supposed to be a secret!";
//            String dataDirectory = getApplicationInfo().dataDir;
//            String filesDirectory = getFilesDir().getAbsolutePath();
//            String encryptedDataFilePath = filesDirectory + File.separator + "keep_yer_secrets_here";
//
//            Log.v(TAG, "plainText = " + plainText);
//            Log.v(TAG, "dataDirectory = " + dataDirectory);
//            Log.v(TAG, "filesDirectory = " + filesDirectory);
//            Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);
//
//            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
//            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//
//            Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
//            outCipher.init(Cipher.DECRYPT_MODE, privateKey);
//
//            CipherOutputStream cipherOutputStream =
//                    new CipherOutputStream(
//                            new FileOutputStream(encryptedDataFilePath), inCipher);
//            cipherOutputStream.write(plainText.getBytes("UTF-8"));
//            cipherOutputStream.close();
//
//            CipherInputStream cipherInputStream =
//                    new CipherInputStream(new FileInputStream(encryptedDataFilePath),
//                            outCipher);
//            byte [] roundTrippedBytes = new byte[1000]; // TODO: dynamically resize as we get more data
//
//            int index = 0;
//            int nextByte;
//            while ((nextByte = cipherInputStream.read()) != -1) {
//                roundTrippedBytes[index] = (byte)nextByte;
//                index++;
//            }
//            String roundTrippedString = new String(roundTrippedBytes, 0, index, "UTF-8");
//            Log.v(TAG, "round tripped string = " + roundTrippedString);
//
//        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (NoSuchProviderException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (InvalidAlgorithmParameterException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (KeyStoreException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (CertificateException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (IOException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (UnrecoverableEntryException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (NoSuchPaddingException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (InvalidKeyException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (BadPaddingException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (IllegalBlockSizeException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        } catch (UnsupportedOperationException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        }
//    }
}
