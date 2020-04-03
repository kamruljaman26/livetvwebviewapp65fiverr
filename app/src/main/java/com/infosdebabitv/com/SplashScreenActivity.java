package com.infosdebabitv.com;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class SplashScreenActivity extends AppCompatActivity {


    public Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Ads Services

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                startApp();
            }
        });

        thread.start();

    }

    public void doWork(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  void startApp (){
        try {
            Thread.sleep(3000);
            Intent intent = new Intent (SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
