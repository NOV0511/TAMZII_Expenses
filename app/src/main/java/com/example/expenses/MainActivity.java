package com.example.expenses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(getApplicationContext());



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
