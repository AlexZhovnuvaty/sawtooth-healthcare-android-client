package com.dehr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.dehr.protobuf.Payload;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sawtooth.sdk.signing.PrivateKey;
import sawtooth.sdk.signing.Secp256k1Context;
import sawtooth.sdk.signing.Secp256k1PrivateKey;
import android.util.Base64;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private TextView  mTextViewResult;

    private TextInputEditText mEditTextURL;
    private Button mButtonSetURL;
    private Button mButtonGetItems;
    private Button mButtonAddLabTest;

    public static final String DEHR_PREFS_NAME = "dEHRPrefsFile";
    public static final String PARAM_NAME_URL = "RestApiURL";
    public static final String DEHR_PRIVATE_KEY = "private_key";
    public static final String DEHR_PUBLIC_KEY = "public_key";
    public static final String DEHR_REST_API_DEFAULT = "http://localhost:8040";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
//        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        mTextViewResult = findViewById(R.id.text_view_result);
        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());
        mButtonSetURL = findViewById(R.id.btn_set_rest_api);
        mButtonGetItems = findViewById(R.id.btn_get_items);
        mButtonAddLabTest = findViewById(R.id.btn_add_lab_test);
        mEditTextURL = findViewById(R.id.edit_rest_api_url);
        //Check button states
        SharedPreferences prefs = getSharedPreferences(DEHR_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(PARAM_NAME_URL, DEHR_REST_API_DEFAULT);
        mEditTextURL.setText(restoredText);
        //Save URL
        mButtonSetURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences(DEHR_PREFS_NAME, MODE_PRIVATE).edit();
                String newURL = Objects.requireNonNull(mEditTextURL.getText()).toString();
                editor.putString(PARAM_NAME_URL, newURL);
                editor.apply();
                Log.d("SET_URL", newURL);
                Toast.makeText(MainActivity.this, newURL + " is set", Toast.LENGTH_SHORT).show();
            }
        });
        //Get items
        mButtonGetItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient client = new OkHttpClient();
                String urlSuff = "/state?address=" + Addressing.makePulseListAddress();
                SharedPreferences prefs = getSharedPreferences(DEHR_PREFS_NAME, MODE_PRIVATE);
                String url = prefs.getString(PARAM_NAME_URL, DEHR_REST_API_DEFAULT);

                Log.d("GET_ITEMS", url);
                Request request = new Request.Builder()
                        .url(url + urlSuff)
                        .get()
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        final String myError = e.getMessage();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewResult.setText(myError);
                                Toast.makeText(MainActivity.this, myError, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
//                        if (response.isSuccessful()) {
                            final String myResponse = response.body().string();
                            Map<String,Pulse> pulseList =new HashMap<>();
                            JsonObject jsonObject = new JsonParser().parse(myResponse).getAsJsonObject();
                            JsonArray jsonPulseList = jsonObject.getAsJsonArray("data");
                            for(JsonElement el: jsonPulseList){
                                String address = el.getAsJsonObject().get("address").getAsString();
                                String data = el.getAsJsonObject().get("data").getAsString();
                                byte[] decodedBytes = Base64.decode(data, Base64.DEFAULT);
                                Payload.AddPulse cl;
                                try {
                                    cl = Payload.AddPulse.parseFrom(decodedBytes);
                                    pulseList.put(address, new Pulse(cl.getPublicKey(), Integer.parseInt(cl.getPulse()), Long.parseLong(cl.getTimestamp())));
                                } catch (InvalidProtocolBufferException e) {
                                    pulseList.put(address, new Pulse("-1", -1, -1));
                                    e.printStackTrace();
                                }

                            }
                            StringBuilder sb = new StringBuilder();
                            for (Map.Entry<String, Pulse> entry : pulseList.entrySet()) {
                                sb.append("Address: ")
                                        .append(entry.getKey())
                                        .append("; Value: ")
                                        .append(entry.getValue().toString())
                                        .append(System.getProperty("line.separator"));
                            }
                            final String output = sb.toString();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mTextViewResult.setText(output);
                                }
                            });
//                        }
                    }
                });
            }
        });
        //Add Test Lab
        mButtonAddLabTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient client = new OkHttpClient();

//                String url = "https://reqres.in/api/users?page=2";

                DEHRRequestHandler requestHandler = new DEHRRequestHandler(getPrivateKey());
                try {
                    Date date= new Date();
                    long time = date.getTime();
                    final Timestamp ts = new Timestamp(time);
                    Random rand = new Random();
                    final int n = rand.nextInt(50);
                    byte[] pulseBody = requestHandler.addPulse(n, ts.getTime()).toByteArray();
                    RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), pulseBody);

                    SharedPreferences prefs = getSharedPreferences(DEHR_PREFS_NAME, MODE_PRIVATE);
                    String url = prefs.getString(PARAM_NAME_URL, DEHR_REST_API_DEFAULT);
                    Log.d("ADD_PULSE", url);
                    String urlSuff = "/batches";
                    Request request = new Request.Builder()
                            .url(url + urlSuff)
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            final String myError = e.getMessage();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTextViewResult.setText(myError);
                                    Toast.makeText(MainActivity.this, myError, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
//                            if (response.isSuccessful()) {
                                final String myResponse = response.body().string();
                                Log.d("ADD_PULSE", myResponse);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTextViewResult.setText(myResponse);
                                        String msg = "Pulse: " + n + ", Timestamp: " + ts.getTime() + " sent";
                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
//                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

//        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void init() {
//        SharedPreferences prefs = getSharedPreferences(DEHR_PRIVATE_KEY, MODE_PRIVATE);
//        String prKey = prefs.getString(DEHR_PRIVATE_KEY, null);
//        if(prKey == null){
//            prKey = generatePrivateKey();
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(DEHR_PRIVATE_KEY, prKey);
//            editor.apply();
//        }
        getPublicKey(getPrivateKey());

//        prefs = getSharedPreferences(DEHR_PUBLIC_KEY, MODE_PRIVATE);
//        String pubKey = prefs.getString(DEHR_PUBLIC_KEY, null);
//        if(pubKey == null){
//            PrivateKey prKeyObj = Secp256k1PrivateKey.fromHex(prKey);
//            pubKey = generatePublicKey(prKeyObj);
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(DEHR_PUBLIC_KEY, pubKey);
//            editor.apply();
//        }

    }

    private String getPublicKey(PrivateKey privateKey){
        SharedPreferences prefs = getSharedPreferences(DEHR_PUBLIC_KEY, MODE_PRIVATE);
        String pubKey = prefs.getString(DEHR_PUBLIC_KEY, null);
        if(pubKey == null){
            pubKey = generatePublicKey(privateKey);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DEHR_PUBLIC_KEY, pubKey);
            editor.apply();
        }
        return pubKey;
    }

    private Secp256k1PrivateKey getPrivateKey(){
        SharedPreferences prefs = getSharedPreferences(DEHR_PRIVATE_KEY, MODE_PRIVATE);
        String prKey = prefs.getString(DEHR_PRIVATE_KEY, null);
        if(prKey == null){
            prKey = generatePrivateKey();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DEHR_PRIVATE_KEY, prKey);
            editor.apply();
        }
        return Secp256k1PrivateKey.fromHex(prKey);
    }

    private String generatePrivateKey() {
        Secp256k1Context context = new Secp256k1Context();
        return context.newRandomPrivateKey().hex();
    }

    private String generatePublicKey(PrivateKey privateKey){
        Secp256k1Context context = new Secp256k1Context();
        return context.getPublicKey(privateKey).hex();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}