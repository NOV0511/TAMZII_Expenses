package com.example.expenses;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MemberActivity extends AppCompatActivity {

    long deckId;

    DatabaseHelper db;

    Button addMemberButton;
    LinearLayout memberList;

    List<Member> members;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        db = DatabaseHelper.getInstance(getApplicationContext());

        deckId = getIntent().getExtras().getLong("deckId");

        addMemberButton = findViewById(R.id.addMember);
        memberList = findViewById(R.id.members);

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MemberActivity.this);
                builder.setTitle("Zadejte jméno:");

                final EditText input = new EditText(MemberActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addMember(input.getText().toString());
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

        showMembers();
    }

    private void addMember(String name){
        if ( !db.memberExists(name, deckId) ){
            db.createMember(name, deckId);
            showMembers();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MemberActivity.this);
            builder.setTitle("Alert");
            builder.setMessage("Uživatel se zadaným jménem již existuje.");



            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });


            builder.show();
        }

    }

    private void showMembers(){
        members = db.getMembers(deckId);
        memberList.removeAllViewsInLayout();

        for ( int i = 0; i < members.size(); i++ ) {
            Member m = members.get(i);

            LinearLayout layout = new LinearLayout(MemberActivity.this);

            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            memberList.addView(layout);

            TextView tv1 = new TextView(MemberActivity.this);
            tv1.setText("" + m.getName());
            TextView tv2 = new TextView(MemberActivity.this);
            tv2.setText("" + getMemberBalance(m.getId()));

            layout.addView(tv1);
            layout.addView(tv2);
        }

    }

    public double getMemberBalance(long memberId){
        double balance = 0.0;
        List<Transaction> tr = db.getTransactions(deckId);
        for ( int i = 0; i < tr.size(); i++){
            Transaction t = tr.get(i);
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
