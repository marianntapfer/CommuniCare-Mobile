package com.example.communicare_mobile;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class Spinner {
    Spinner mySpinner = (Spinner) findViewById(R.id.spinner1);
    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(Spinner.this,
            android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.language));
    myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mySpinner.setAdapter(myAdapter);
}
