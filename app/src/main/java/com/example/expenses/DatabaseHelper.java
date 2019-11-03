package com.example.expenses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "expenses";

    // Table Names
    private static final String TABLE_DECK = "decks";
    private static final String TABLE_MEMBER = "members";
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TABLE_TRANSACTION_MEMBER = "transaction_members";

    //Common keys
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    //Foreign keys
    private static final String DECK_FK = "deck_id";
    private static final String MEMBER_FK = "member_id";
    private static final String TRANSACTION_FK = "transaction_id";

    //Transaction columns
    private static final String TRANSACTION_DESCRITION = "description";
    private static final String TRANSACTION_VALUE = "value";
    private static final String TRANSACTION_CURRENCY = "currency";



    // Table Create Statements
    // Deck table create statement
    private static final String CREATE_TABLE_DECK = "CREATE TABLE IF NOT EXISTS " + TABLE_DECK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT)";

    // Member table create statement
    private static final String CREATE_TABLE_MEMBER = "CREATE TABLE IF NOT EXISTS " + TABLE_MEMBER
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, " + DECK_FK + " INTEGER)";

    // Transaction table create statement
    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + TRANSACTION_DESCRITION +" TEXT, " + TRANSACTION_VALUE
            + " REAL, " + TRANSACTION_CURRENCY + " TEXT, " + DECK_FK + " INTEGER, " + MEMBER_FK + " INTEGER)";

    // Transaction_members table create statement
    private static final String CREATE_TABLE_TRANSACTION_MEMBER = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION_MEMBER
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + TRANSACTION_FK + " INTEGER, " + MEMBER_FK + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_DECK);
        db.execSQL(CREATE_TABLE_MEMBER);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(CREATE_TABLE_TRANSACTION_MEMBER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_MEMBER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECK);

        // create new tables
        onCreate(db);
    }

    /**
     * INSERTs
     */

    public long createDeck(Deck deck) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, deck.getName());

        return db.insert(TABLE_DECK, null, values);
    }

    public long createMember(Member member) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, member.getName());
        values.put(DECK_FK, member.getDeck().getId());

        return db.insert(TABLE_MEMBER, null, values);
    }

    public void createTransactionMember(long transaction, int member) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_FK, transaction);
        values.put(MEMBER_FK, member);

        db.insert(TABLE_MEMBER, null, values);
    }

    public long createTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_DESCRITION, transaction.getDescription());
        values.put(TRANSACTION_VALUE, transaction.getValue());
        values.put(TRANSACTION_CURRENCY, transaction.getCurrency());
        values.put(DECK_FK, transaction.getDeckId().getId());
        values.put(MEMBER_FK, transaction.getWho().getId());

        long transactionId = db.insert(TABLE_MEMBER, null, values);

        for (Member member : transaction.getForWhom()) {
            createTransactionMember(transactionId, member.getId());
        }

        return transactionId;
    }

    //TODO: create Currency

    /**
     * GETTERs
     */

    public List<Deck> getDecks() {
        List<Deck> decks = new ArrayList<Deck>();
        String selectQuery = "SELECT  * FROM " + TABLE_DECK;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Deck deck = new Deck(
                        c.getInt(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(KEY_NAME))
                );

                decks.add(deck);
            } while (c.moveToNext());
        }
        return decks;
    }

    public List<Member> getMembers(int deckId) {
        List<Member> members = new ArrayList<Member>();
        String selectQuery = "SELECT  * FROM " + TABLE_MEMBER + " WHERE " + DECK_FK + " = " + deckId;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Member member = new Member(
                        c.getInt(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(KEY_NAME))
                        //TODO: pridat deck + napsat getter na id
                );

                members.add(member);
            } while (c.moveToNext());
        }
        return members;
    }

    public List<Transaction> getTransactions(int deckId) {
        List<Transaction> transactions = new ArrayList<Transaction>();
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTION + " WHERE " + DECK_FK + " = " + deckId;;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                        c.getInt(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(TRANSACTION_DESCRITION)),
                        c.getDouble(c.getColumnIndex(TRANSACTION_VALUE)),
                        //TODO: predelat currency na objekt
                        c.getString(c.getColumnIndex(TRANSACTION_CURRENCY)),
                        //TODO: to same jak nize
                        new Member(c.getInt(c.getColumnIndex(MEMBER_FK)))
                        //TODO: pridat deck + napsat getter na id
                );

                List<Member> members = new ArrayList<Member>();

                for (int memberId : getTransactionMembers(transaction.getId())){
                    //TODO: get member podle id
                    members.add(new Member(memberId));
                }

                transaction.setForWhom(members);

                transactions.add(transaction);
            } while (c.moveToNext());
        }
        return transactions;
    }

    private List<Integer> getTransactionMembers(int transactionId) {
        List<Integer> members = new ArrayList<Integer>();
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTION_MEMBER + " WHERE " + TRANSACTION_FK + " = " + transactionId;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                members.add(c.getInt(c.getColumnIndex(MEMBER_FK)));
            } while (c.moveToNext());
        }
        return members;
    }

    //TODO: Get currency podle KODU
}
