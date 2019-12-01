package com.example.expenses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    Switch onlyWifi;
    Spinner defaultCurrency;


    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    public static String sPref = null;

    List<Currency> currencies;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        onlyWifi = findViewById(R.id.switchData);
        defaultCurrency = findViewById(R.id.defaultCurrency);

        final SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);


        sPref = sharedPrefs.getString(getString(R.string.download_key), WIFI);

        if ( sPref.equals(WIFI) )
            onlyWifi.setChecked(true);
        else
            onlyWifi.setChecked(false);


        onlyWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                if (isChecked) {
                    editor.putString(getString(R.string.download_key), WIFI);
                }
                else{
                    editor.putString(getString(R.string.download_key), ANY);
                }
                editor.commit();

            }
        });

        db = DatabaseHelper.getInstance(SettingsActivity.this);

        currencies = db.getAllCurrencies();

        final long[] currIds = new long[currencies.size()];
        String[] currNames = new String[currencies.size()];
        for ( int i = 0; i < currencies.size(); i++ ) {
            currNames[i] = "" + currencies.get(i).getCountry() + " - " + currencies.get(i).getName();
            currIds[i] = currencies.get(i).getId();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(SettingsActivity.this, android.R.layout.simple_spinner_item, currNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        defaultCurrency.setAdapter(adapter);
        defaultCurrency.setSelection(Integer.parseInt(sharedPrefs.getString(getString(R.string.default_currency), ""+1))-1);

        defaultCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(getString(R.string.default_currency), ""+currIds[(int)id]);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
