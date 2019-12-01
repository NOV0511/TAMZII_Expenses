package com.example.expenses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static java.lang.Math.abs;

public class DeckActivity extends AppCompatActivity {

    long deckId;

    Button showMemberButton;
    Button addTransactionButton;
    Button removeDeckButton;
    LinearLayout transactionsList;
    LinearLayout debtsList;
    TextView total;
    TextView totalCount;

    List<Transaction> transactions;
    List<Member> members;
    List<Currency> currencies;
    DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        db = DatabaseHelper.getInstance(getApplicationContext());

        deckId = getIntent().getExtras().getLong("deckId");

        showMemberButton = findViewById(R.id.showMembers);
        addTransactionButton = findViewById(R.id.addTransaction);
        removeDeckButton = findViewById(R.id.removeDeck);
        transactionsList = findViewById(R.id.transactionList);
        debtsList = findViewById(R.id.debts);
        total = findViewById(R.id.total);
        totalCount = findViewById(R.id.totalCount);

        members = db.getMembers(deckId);

        showTransactions();

        currencies = db.getAllCurrencies();

        showMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent memberActivity= new Intent(getApplicationContext(), MemberActivity.class);
                memberActivity.putExtra("deckId", deckId);
                startActivity(memberActivity);
            }
        });

        removeDeckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteDeck(deckId);
                finish();
            }
        });

        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeckActivity.this);
                builder.setTitle("Nový výdaj:");


                final LinearLayout layout = new LinearLayout(DeckActivity.this);

                layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                layout.setOrientation(LinearLayout.VERTICAL);


                TextView textDesc = new TextView(DeckActivity.this);
                textDesc.setText("Popis:");

                final EditText desc = new EditText(DeckActivity.this);
                desc.setInputType(InputType.TYPE_CLASS_TEXT);

                TextView textValue = new TextView(DeckActivity.this);
                textValue.setText("Hodnota:");

                final EditText value = new EditText(DeckActivity.this);
                value.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                TextView textCurr = new TextView(DeckActivity.this);
                textCurr.setText("Měna:");

                final long[] currIds = new long[currencies.size()];
                String[] currNames = new String[currencies.size()];
                for ( int i = 0; i < currencies.size(); i++ ) {
                    currNames[i] = "" + currencies.get(i).getCountry() + " - " + currencies.get(i).getName();
                    currIds[i] = currencies.get(i).getId();
                }

                final Spinner curr = new Spinner(DeckActivity.this);
                ArrayAdapter<String> adapter = new ArrayAdapter(DeckActivity.this, android.R.layout.simple_spinner_item, currNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                curr.setAdapter(adapter);

                TextView textWho = new TextView(DeckActivity.this);
                textWho.setText("Kdo platil:");

                final RadioButton[] rb = new RadioButton[members.size()];
                final RadioGroup who = new RadioGroup(DeckActivity.this);
                who.setOrientation(RadioGroup.VERTICAL);
                for ( int i = 0; i < members.size(); i++ ) {
                    rb[i]  = new RadioButton(DeckActivity.this);
                    rb[i].setText("" + members.get(i).getName());
                    rb[i].setId((int) members.get(i).getId());
                    who.addView(rb[i]);
                }


                TextView textForWhom = new TextView(DeckActivity.this);
                textForWhom.setText("Za koho:");

                layout.addView(textDesc);
                layout.addView(desc);
                layout.addView(textValue);
                layout.addView(value);
                layout.addView(textCurr);
                layout.addView(curr);
                layout.addView(textWho);
                layout.addView(who);
                layout.addView(textForWhom);

                for ( int i = 0; i < members.size(); i++ ) {
                    CheckBox cb = new CheckBox(DeckActivity.this);
                    cb.setText("" + members.get(i).getName());
                    cb.setId((int) members.get(i).getId());
                    layout.addView(cb);
                }

                ScrollView scroll = new ScrollView(DeckActivity.this);
                scroll.addView(layout);
                builder.setView(scroll);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Member> forWhomMem = new ArrayList<Member>();
                        int count = layout.getChildCount();
                        for (int i = 0; i < count; i++) {
                            View v = layout.getChildAt(i);
                            if (v instanceof CheckBox) {
                                if (((CheckBox) v).isChecked()){
                                    forWhomMem.add(db.getMember((long)v.getId()));
                                }
                            }
                        }

                        if(desc.getText().toString().isEmpty() || value.getText().toString().isEmpty() ||
                            who.getCheckedRadioButtonId() == -1 || forWhomMem.size() == 0){
                            dialog.cancel();
                            AlertDialog.Builder builder = new AlertDialog.Builder(DeckActivity.this);
                            builder.setTitle("Alert");
                            builder.setMessage("Musíte vyplnit všechny hodnoty.");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });


                            builder.show();
                        }else {
                            db.createTransaction(desc.getText().toString(), Double.parseDouble(value.getText().toString()),
                                    currIds[curr.getSelectedItemPosition()], deckId, (long)who.getCheckedRadioButtonId(), forWhomMem);

                            showTransactions();
                        }
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
        members = db.getMembers(deckId);
        showTransactions();
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

    public void showTransactions(){
        transactions = db.getTransactions(deckId);
        transactionsList.removeAllViewsInLayout();
        debtsList.removeAllViewsInLayout();

        showTransactionList();
        showDebtsList();
        showCount();

    }

    public void showTransactionList(){

        for ( int i = 0; i < transactions.size(); i++ ) {
            final Transaction tr = transactions.get(i);

            LinearLayout layout = new LinearLayout(DeckActivity.this);

            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            if (!tr.getDescription().equals("debtPayBack"))
                transactionsList.addView(layout);

            TextView tv1 = new TextView(DeckActivity.this);
            tv1.setText("" + tr.getDescription() + " - " + tr.getValue() + " " + tr.getCurrency().getCode());
            TextView tv2 = new TextView(DeckActivity.this);
            StringBuilder tmp = new StringBuilder("" + tr.getWho().getName() + " zaplatil/a za: ");

            for ( int j = 0; j < tr.getForWhom().size(); j++ ){
                if (j == tr.getForWhom().size() - 1)
                    tmp.append(tr.getForWhom().get(j).getName());
                else
                    tmp.append(tr.getForWhom().get(j).getName()).append(", ");
            }

            tv2.setText(tmp.toString());

            layout.addView(tv1);
            layout.addView(tv2);

            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    float initialX = 0;
                    float initialY = 0;

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = event.getX();
                            initialY = event.getY();
                            break;

                        case MotionEvent.ACTION_UP:
                            float finalX = event.getX();
                            float finalY = event.getY();
                            double absX = abs(finalX - initialX);
                            double absY = abs(finalY - initialY);


                            if (initialX < finalX &&  absX > absY && absX > 30) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DeckActivity.this);
                                builder.setTitle("Chcete odstranit tento výdaj?");

                                final TextView text1 = new TextView(DeckActivity.this);
                                text1.setText("");
                                builder.setView(text1);

                                builder.setPositiveButton("ANO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.deleteTransaction(tr.getId());
                                        showTransactions();
                                    }
                                });
                                builder.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            }
                            break;
                    }
                    return true;
                }
            });
        }

    }

    public void showDebtsList(){

        long lowest = 1;
        long highest = 1;

        if (members.size() > 1){
            lowest = members.get(0).getId();
            highest = members.get(0).getId();
        }

        Map<Long, Double> map = new HashMap<Long, Double>();
        for ( Member m : members ) {
            map.put(m.getId(), getMemberBalance(m.getId()));
            if (getMemberBalance(m.getId()) > getMemberBalance(highest)){
                highest = m.getId();
            }
            if (getMemberBalance(m.getId()) < getMemberBalance(lowest)){
                lowest = m.getId();
            }
        }
        boolean completed = true;
        double difference;


        for (Map.Entry<Long, Double> entry : map.entrySet()) {
            if (entry.getValue() > 0.1 || entry.getValue() < -0.1){
                completed = false;
            }
        }

        while ( !completed ) {
            if (map.get(highest) > map.get(lowest) * -1){
                difference = map.get(lowest) * -1;
            }else {
                difference = map.get(highest);
            }

            LinearLayout layout = new LinearLayout(DeckActivity.this);

            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            debtsList.addView(layout);
            SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
            Currency c = db.getCurrency(Long.parseLong(sharedPrefs.getString(getString(R.string.default_currency), ""+1)));

            double shownDiff = difference;
            shownDiff = (shownDiff/c.getRate())*c.getAmount();

            TextView tv1 = new TextView(DeckActivity.this);
            tv1.setText("" + db.getMember(lowest).getName() + " -> " + db.getMember(highest).getName());
            TextView tv2 = new TextView(DeckActivity.this);
            tv2.setText("" + shownDiff + " " + c.getCode());

            layout.addView(tv1);
            layout.addView(tv2);

            final double diff = difference;
            final long low = lowest;
            final long high = highest;

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeckActivity.this);
                    builder.setTitle("Chcete splatit tento dluh?");

                    final TextView text1 = new TextView(DeckActivity.this);
                    text1.setText("");
                    builder.setView(text1);

                    builder.setPositiveButton("ANO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<Member> m = new ArrayList<>();
                            m.add(db.getMember(high));
                            db.createTransaction("debtPayBack", diff, 1, deckId, low, m);
                            showTransactions();
                        }
                    });
                    builder.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                map.replace(highest, map.get(highest) - difference);
                map.replace(lowest, map.get(lowest) + difference);
            }

            boolean first = true;
            completed = true;
            for (Map.Entry<Long, Double> entry : map.entrySet()) {
                if (first) {
                    lowest = entry.getKey();
                    highest = entry.getKey();
                    first = false;
                }
                else{
                    if (entry.getValue() > map.get(highest)){
                        highest = entry.getKey();
                    }
                    if (entry.getValue() < map.get(lowest)){
                        lowest = entry.getKey();
                    }
                }
                if (entry.getValue() > 0.1 || entry.getValue() < -0.1){
                    completed = false;
                }
            }
        }
    }



    public void showCount(){
        int count = 0;
        double sum = 0;

        for ( int i = 0; i < transactions.size(); i++ ) {
            if (!transactions.get(i).getDescription().equals("debtPayBack")){
                count++;
                sum += transactions.get(i).getRealValue();
            }
        }
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
        Currency c = db.getCurrency(Long.parseLong(sharedPrefs.getString(getString(R.string.default_currency), ""+1)));

        sum = (sum/c.getRate())*c.getAmount();

        total.setText(""+ sum + " " + c.getCode());
        totalCount.setText("Počet výdajů: "+ count);
    }

    public double getMemberBalance(long memberId){
        double balance = 0.0;
        for ( int i = 0; i < transactions.size(); i++){
            Transaction t = transactions.get(i);
            if (t.getWho().getId() == memberId)
                balance += t.getRealValue();
            for ( int j = 0; j < t.getForWhom().size(); j++ ) {
                if (t.getForWhom().get(j).getId() == memberId)
                    balance -= (t.getRealValue()/t.getForWhom().size());
            }
        }

        return balance;
    }

}
