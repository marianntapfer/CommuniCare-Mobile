package com.example.communicare_mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NavUtils;
import androidx.room.Room;

import com.example.communicare_mobile.db.AppDatabase;
import com.example.communicare_mobile.db.Settings;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";
    private boolean gender;
    private String textLanguage;
    private String speechLanguage;
    private AppDatabase db;
    String[] language = {"Estonian", "English", "Russian"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "main-db")
                .allowMainThreadQueries()
                .build();

        Settings settings = db.settingsDao().findById(0);
        if (settings == null) {
            settings = createDefaultSettings();
        }

        gender = settings.gender;
        textLanguage = settings.textLanguage;
        speechLanguage = settings.speechLanguage;

        SwitchCompat genderSwitch = findViewById(R.id.genderSwitch);
        genderSwitch.setChecked(gender);
        genderSwitch.setOnCheckedChangeListener((view, isChecked) -> gender = isChecked);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(button -> saveChanges());
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(button -> cancelChanges());

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner speechSpin = findViewById(R.id.speechSpinner);
        speechSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //Performing action onItemSelected and onNothing selected
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                speechLanguage = language[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner textSpin = findViewById(R.id.textSpinner);
        textSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //Performing action onItemSelected and onNothing selected
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                textLanguage = language[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, R.layout.spinner_item, language);
        aa.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        speechSpin.setAdapter(aa);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aaa = new ArrayAdapter(this, R.layout.spinner_item, language);
        aaa.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        textSpin.setAdapter(aa);

        speechSpin.setSelection(getIndex(speechLanguage));
        textSpin.setSelection(getIndex(textLanguage));

    }

    private void saveChanges() {
        Settings savedSettings = new Settings();
        savedSettings.id = 0;
        savedSettings.textLanguage = textLanguage;
        savedSettings.speechLanguage = speechLanguage;
        savedSettings.gender = gender;
        db.settingsDao().updateSettings(savedSettings);
        NavUtils.navigateUpFromSameTask(this);
    }

    private void cancelChanges() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private Settings createDefaultSettings() {
        Settings newSettings = new Settings();
        newSettings.id = 0;
        newSettings.gender = true;
        newSettings.speechLanguage = "Estonian";
        newSettings.textLanguage = "Estonian";
        db.settingsDao().insertAll(newSettings);
        return newSettings;
    }

    private int getIndex(String myString) {
        for (int i = 0; i < language.length; i++) {
            if (language[i].equals(myString)) {
                return i;
            }
        }
        return 0;
    }
}
