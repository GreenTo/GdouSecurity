package com.gdou.security;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gdou.security.data.UserData;
import com.gdou.security.utils.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText usernameText;

    private EditText passwordText;

    private Button signButton;

    private String username;

    private String password;

    //private String address;

    //private static long SERVE_ERROR = 1;

    //private static long CLIENT_ERROR = 2;

    //private static long LOGIN_SUCCESS = 3;

    //private long status = 0;

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //SmartToast.error("服务器发生故障，请稍后再试...");
                    Toast.makeText(LoginActivity.this,"服务器发生故障，请稍后再试...",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    //SmartToast.warning("账号或密码错误!");
                    Toast.makeText(LoginActivity.this,"账号或密码错误!",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("account",username);
                    startActivity(intent);
                    break;
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameText = findViewById(R.id.username);
        passwordText = findViewById(R.id.password);
        signButton = findViewById(R.id.sign);
        signButton.setOnClickListener(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = pref.getString("username", "");
        String password = pref.getString("password", "");
        if (!username.isEmpty() && !password.isEmpty()){
            usernameText.setText(username);
            passwordText.setText(password);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign:
                username = usernameText.getText().toString();
                password = passwordText.getText().toString();

                final Message message = new Message();
                HttpUtil.loginRequest(username, password, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        message.what = 1;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        ResponseBody body = response.body();
                        byte[] bytes = body.bytes();
                        String result = new String(bytes);
                        if (!result.contains("200")){
                            message.what = 2;
                        }else {
                            message.what = 3;
                            editor = pref.edit();
                            editor.putString("username",username);
                            editor.putString("password",password);
                            editor.apply();
                            UserData.username = username;
                        }
                        handler.sendMessage(message);
                    }
                });
                break;
            default:
                break;
        }
    }

}
