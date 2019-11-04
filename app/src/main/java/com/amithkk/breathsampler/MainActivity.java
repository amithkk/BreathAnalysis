package com.amithkk.breathsampler;

import android.content.Intent;
import android.os.Bundle;

import com.amithkk.breathsampler.pojo.BreathResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_health:
                    return true;
                default:
                    notImplemented();
                    return true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_health);
    }

    private void notImplemented()
    {
        Toast.makeText(this, "Unimplemented", Toast.LENGTH_SHORT).show();
    }

    public void launchBreathDash(View view) {

        startActivity(new Intent(MainActivity.this, BreathHistoryActivity.class));

    }
}
