package com.gdou.security;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.gdou.security.data.UserData;
import com.gdou.security.data.UserResult;
import com.gdou.security.utils.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout drawerLayout;

    private ImageButton startButton;

    private Toolbar toolbar;

    private NavigationView navigationView;

    private ImageView menu_image;

    private Menu menu;

    private MenuItem nameItem;

    private MenuItem telItem;

    private MenuItem numberItem;

    private MenuItem ageItem;

    private static UserResult userResult;

    private static String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        navigationView = findViewById(R.id.nav_view);
        menu_image = findViewById(R.id.menu_image);
        menu_image.setOnClickListener(this);
        menu = navigationView.getMenu();
        telItem = menu.findItem(R.id.nav_tel);
        nameItem = menu.findItem(R.id.nav_name);
        ageItem = menu.findItem(R.id.nav_age);
        numberItem = menu.findItem(R.id.nav_number);
        Intent intent = getIntent();
        account = intent.getStringExtra("account");
        getInformation();
    }

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    switch (item.getItemId()) {
    //        case android.R.id.home:
    //            drawerLayout.openDrawer(GravityCompat.START);
    //            break;
    //        default:
    //            break;
    //    }
    //    return true;
    //}

    private void getInformation(){
        String address = "http://192.168.2.125:1234/admin/getInfo";
        String username = UserData.username;
        System.out.println(username);
        HttpUtil.getInformation(address, account, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String responseText = body.toString();
                byte[] bytes = body.bytes();
                String result = new String(bytes);
                userResult = new Gson().fromJson(result, UserResult.class);
                UserData.id = userResult.data.guardId;
                Log.d("MainActivity",userResult.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        telItem.setTitle(userResult.data.phone);
                        nameItem.setTitle(userResult.data.name);
                        ageItem.setTitle(userResult.data.age + "岁");
                        numberItem.setTitle(userResult.data.number);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_image:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.startButton:
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("id",userResult.data.guardId);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
