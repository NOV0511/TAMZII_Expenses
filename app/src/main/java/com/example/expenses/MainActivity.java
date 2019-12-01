package com.example.expenses;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    private static final String URL =
            "http://www.cnb.cz/cs/financni_trhy/devizovy_trh/kurzy_devizoveho_trhu/denni_kurz.xml";

    private static boolean wifiConnected = false;
    private static boolean mobileConnected = false;

    public static String sPref = null;

    ListView decksView;
    Button addDeck;

    List<Deck> decks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(getApplicationContext());


        decksView = (ListView)findViewById(R.id.deckList);
        addDeck = (Button)findViewById(R.id.addDeck);



        addDeck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Zadejte jméno projektu:");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addDeck(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);


        sPref = sharedPrefs.getString(getString(R.string.download_key), WIFI);
        /*
        TODO: tyto shared preferences do settings
        Log.d("sharing", sPref);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.download_key), "any");
        editor.commit();
        sPref = sharedPrefs.getString(getString(R.string.download_key), "Wi-Fi");
        Log.d("sharing", sPref);*/

        updateConnectedFlags();
        loadCurrencies();
        viewDecks();
    }

    private void viewDecks(){
        decks = db.getAllDecks();

        final String[] deckNames = new String[decks.size()];
        final long[] deckIds = new long[decks.size()];

        for ( int i = 0; i < decks.size(); i++ ) {
            deckNames[i] = decks.get(i).getName();
            deckIds[i] = decks.get(i).getId();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.deck_list, deckNames);
        decksView.setAdapter(adapter);
        decksView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent deckActivity= new Intent(getApplicationContext(), DeckActivity.class);
                deckActivity.putExtra("deckId", deckIds[arg2]);
                startActivity(deckActivity);
            }

        });
    }

    private void addDeck(String name){
        if ( !db.deckExists(name) ){
            db.createDeck(name);
            viewDecks();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alert");
            builder.setMessage("Projekt se zadaným jménem již existuje.");



            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });


            builder.show();
        }
    }



    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }


    private void loadCurrencies() {
        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((sPref.equals(WIFI)) && (wifiConnected))) {
            db.deleteCurrencies();
            new DownloadXmlTask(this).execute(URL);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class DownloadXmlTask extends AsyncTask<String, Void, List<Currency>> {

        Context context;

        public DownloadXmlTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<Currency> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return null;
            } catch (XmlPullParserException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Currency> result) {
            Toast.makeText(context, result.size() + " items downloaded", Toast.LENGTH_SHORT).show();
        }
    }


    private List<Currency> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        CNBXmlParser cnbXmlParser = new CNBXmlParser(db);
        List<Currency> currencies = null;

        try {
            stream = downloadUrl(urlString);
            currencies = cnbXmlParser.parse(stream);

        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return currencies;
    }


    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);

        URLConnection conn = (URLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestProperty("method", "GET");
        conn.setDoInput(true);

        InputStream in = new BufferedInputStream(conn.getInputStream());

        return in;
    }
}
