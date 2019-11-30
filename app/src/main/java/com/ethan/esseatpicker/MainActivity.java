package com.ethan.esseatpicker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public SeatPicker seatPicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        seatPicker = findViewById(R.id.seat_picker);
        seatPicker.setMaxSelected(3);
        seatPicker.setSeatClassifier(new SeatPicker.SeatClassifier() {
            @Override
            public boolean isValid(int row, int column) {
                if (row == 4 ) return false;
                return true;
            }

            @Override
            public boolean isSold(int row, int column) {
                if (row == 2 && column == 2) return true;
                return false;
            }

            @Override
            public void selected(int row, int colum) {
                Toast.makeText(MainActivity.this, "Selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void unSelected(int row, int column) {
                Toast.makeText(MainActivity.this, "UnSelected", Toast.LENGTH_SHORT).show();
            }
        });
        seatPicker.initData(10,12);
    }
}
