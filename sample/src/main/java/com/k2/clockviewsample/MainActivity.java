package com.k2.clockviewsample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.k2.analogueclockview.AnalogueClock;

public class MainActivity extends AppCompatActivity {

    AnalogueClock clockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockView = (AnalogueClock) findViewById(R.id.clock_view);
        clockView.setHourHandColor(Color.WHITE);
        clockView.setMinuteHandColor(Color.WHITE);
    }
}
