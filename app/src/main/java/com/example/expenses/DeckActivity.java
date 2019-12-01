package com.example.expenses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
                value.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

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


                builder.setView(layout);


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

        showTransactionList();
        showDebtsList();
        showCount();

    }

    public void showTransactionList(){

        for ( int i = 0; i < transactions.size(); i++ ) {
            Transaction tr = transactions.get(i);

            LinearLayout layout = new LinearLayout(DeckActivity.this);

            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            transactionsList.addView(layout);

            TextView tv1 = new TextView(DeckActivity.this);
            tv1.setText("" + tr.getDescription() + " - " + tr.getValue() + " " + tr.getCurrency().getCode());
            TextView tv2 = new TextView(DeckActivity.this);
            StringBuilder tmp = new StringBuilder("" + tr.getWho().getName() + " zaplatil za: ");

            for ( int j = 0; j < tr.getForWhom().size(); j++ ){
                if (j == tr.getForWhom().size() - 1)
                    tmp.append(tr.getForWhom().get(j).getName());
                else
                    tmp.append(tr.getForWhom().get(j).getName()).append(", ");
            }

            tv2.setText(tmp.toString());

            layout.addView(tv1);
            layout.addView(tv2);
        }

    }

    public void showDebtsList(){

    }

    public void showCount(){
        
    }

}
