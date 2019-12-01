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

    private static DatabaseHelper sInstance;

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "expenses";

    // Table Names
    private static final String TABLE_DECK = "decks";
    private static final String TABLE_MEMBER = "members";
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TABLE_TRANSACTION_MEMBER = "transaction_members";
    private static final String TABLE_CURRENCY = "currency";

    //Common keys
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    //Foreign keys
    private static final String DECK_FK = "deck_id";
    private static final String MEMBER_FK = "member_id";
    private static final String TRANSACTION_FK = "transaction_id";
    private static final String CURRENCY_FK = "currency_id";

    //Transaction columns
    private static final String TRANSACTION_DESCRIPTION = "description";
    private static final String TRANSACTION_VALUE = "value";
    private static final String TRANSACTION_REAL_VALUE = "realValue";

    //Currency columns
    private static final String CURRENCY_CODE = "code";
    private static final String CURRENCY_AMOUNT = "amount";
    private static final String CURRENCY_RATE = "rate";
    private static final String CURRENCY_COUNTRY = "country";




    // Table Create Statements
    // Deck table create statement
    private static final String CREATE_TABLE_DECK = "CREATE TABLE IF NOT EXISTS " + TABLE_DECK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT)";

    // Member table create statement
    private static final String CREATE_TABLE_MEMBER = "CREATE TABLE IF NOT EXISTS " + TABLE_MEMBER
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, " + DECK_FK + " INTEGER)";


    // Currency table create statement
    private static final String CREATE_TABLE_CURRENCY = "CREATE TABLE IF NOT EXISTS " + TABLE_CURRENCY
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + CURRENCY_CODE + " TEXT, " + KEY_NAME + " TEXT, "
            + CURRENCY_AMOUNT + " REAL, " + CURRENCY_RATE + " REAL, "  + CURRENCY_COUNTRY + " TEXT)";

    // Transaction table create statement
    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + TRANSACTION_DESCRIPTION +" TEXT, " + TRANSACTION_VALUE
            + " REAL, " + CURRENCY_FK + " TEXT, " + DECK_FK + " INTEGER, " + MEMBER_FK + " INTEGER, " + TRANSACTION_REAL_VALUE + " REAL)";

    // Transaction_members table create statement
    private static final String CREATE_TABLE_TRANSACTION_MEMBER = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION_MEMBER
            + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + TRANSACTION_FK + " INTEGER, " + MEMBER_FK + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_DECK);
        db.execSQL(CREATE_TABLE_MEMBER);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(CREATE_TABLE_TRANSACTION_MEMBER);
        db.execSQL(CREATE_TABLE_CURRENCY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_MEMBER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY);

        // create new tables
        onCreate(db);
    }



    /**
     * INSERTs
     */

    public long createDeck(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);

        return db.insert(TABLE_DECK, null, values);
    }

    public long createMember(String name, long deckId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(DECK_FK, deckId);

        return db.insert(TABLE_MEMBER, null, values);
    }

    public void createTransactionMember(long transaction, long member) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_FK, transaction);
        values.put(MEMBER_FK, member);

        db.insert(TABLE_TRANSACTION_MEMBER, null, values);
    }

    public long createTransaction(String description, double value, long currencyId, long deckId, long memberId, List<Member> members) {
        SQLiteDatabase db = this.getWritableDatabase();

        Currency c = getCurrency(currencyId);

        double realValue = (value * c.getRate())/c.getAmount();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_DESCRIPTION, description);
        values.put(TRANSACTION_VALUE, value);
        values.put(CURRENCY_FK, currencyId);
        values.put(DECK_FK, deckId);
        values.put(MEMBER_FK, memberId);
        values.put(TRANSACTION_REAL_VALUE, realValue);

        long transactionId = db.insert(TABLE_TRANSACTION, null, values);

        for (Member member : members) {
            createTransactionMember(transactionId, member.getId());
        }

        return transactionId;
    }

    public long createCurrency(String code, String name, double amount, double rate, String country) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CURRENCY_CODE, code);
        values.put(KEY_NAME, name);
        values.put(CURRENCY_AMOUNT, amount);
        values.put(CURRENCY_RATE, rate);
        values.put(CURRENCY_COUNTRY, country);

        return db.insert(TABLE_CURRENCY, null, values);
    }

    /**
     * GETTERs
     */

    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<Currency>();
        String selectQuery = "SELECT  * FROM " + TABLE_CURRENCY;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Currency currency = new Currency(
                        c.getLong(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(CURRENCY_CODE)),
                        c.getString(c.getColumnIndex(KEY_NAME)),
                        c.getDouble(c.getColumnIndex(CURRENCY_AMOUNT)),
                        c.getDouble(c.getColumnIndex(CURRENCY_RATE)),
                        c.getString(c.getColumnIndex(CURRENCY_COUNTRY))
                );

                currencies.add(currency);
            } while (c.moveToNext());
        }
        c.close();
        return currencies;
    }


    public List<Deck> getAllDecks() {
        List<Deck> decks = new ArrayList<Deck>();
        String selectQuery = "SELECT  * FROM " + TABLE_DECK;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Deck deck = new Deck(
                        c.getLong(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(KEY_NAME))
                );

                decks.add(deck);
            } while (c.moveToNext());
        }
        c.close();
        return decks;
    }

    public Deck getDeck(long deckId){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_DECK + " WHERE "
                + KEY_ID + " = " + deckId;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Deck d = new Deck(c.getLong(c.getColumnIndex(KEY_ID)), c.getString(c.getColumnIndex(KEY_NAME)));

        c.close();
        return d;
    }

    public boolean deckExists(String name){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_DECK + " WHERE "
                + KEY_NAME + " = '" + name + "'";

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.getCount() > 0)
            return true;

        return false;
    }

    public boolean memberExists(String name, long deckId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_MEMBER + " WHERE "
                + KEY_NAME + " = '" + name + "'" + " AND " + DECK_FK + " = " + deckId;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.getCount() > 0)
            return true;

        return false;
    }


    public Currency getCurrency(long currencyId){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_CURRENCY + " WHERE "
                + KEY_ID + " = " + currencyId;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Currency cur = new Currency(
                c.getLong(c.getColumnIndex(KEY_ID)),
                c.getString(c.getColumnIndex(CURRENCY_CODE)),
                c.getString(c.getColumnIndex(KEY_NAME)),
                c.getDouble(c.getColumnIndex(CURRENCY_AMOUNT)),
                c.getDouble(c.getColumnIndex(CURRENCY_RATE)),
                c.getString(c.getColumnIndex(CURRENCY_COUNTRY))
        );

        c.close();

        return cur;
    }

    public Member getMember(long memberId){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_MEMBER+ " WHERE "
                + KEY_ID + " = " + memberId;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Member m = new Member(
                c.getLong(c.getColumnIndex(KEY_ID)),
                c.getString(c.getColumnIndex(KEY_NAME)),
                getDeck(c.getLong(c.getColumnIndex(DECK_FK)))
        );

        c.close();
        return m;
    }

    public List<Member> getMembers(long deckId) {
        List<Member> members = new ArrayList<Member>();
        String selectQuery = "SELECT  * FROM " + TABLE_MEMBER + " WHERE " + DECK_FK + " = " + deckId;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Member member = new Member(
                        c.getLong(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(KEY_NAME)),
                        getDeck(c.getLong(c.getColumnIndex(DECK_FK)))
                );

                members.add(member);
            } while (c.moveToNext());
        }
        c.close();
        return members;
    }

    public List<Transaction> getTransactions(long deckId) {
        List<Transaction> transactions = new ArrayList<Transaction>();
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTION + " WHERE " + DECK_FK + " = " + deckId;;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                        c.getLong(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(TRANSACTION_DESCRIPTION)),
                        c.getDouble(c.getColumnIndex(TRANSACTION_VALUE)),
                        getCurrency(c.getLong(c.getColumnIndex(CURRENCY_FK))),
                        getDeck(c.getLong(c.getColumnIndex(DECK_FK))),
                        getMember(c.getLong(c.getColumnIndex(MEMBER_FK))),
                        c.getDouble(c.getColumnIndex(TRANSACTION_REAL_VALUE))
                );

                List<Member> members = new ArrayList<Member>();

                for (long memberId : getTransactionMembers(transaction.getId())){
                    members.add(getMember(memberId));
                }

                transaction.setForWhom(members);

                transactions.add(transaction);
            } while (c.moveToNext());
        }
        c.close();
        return transactions;
    }

    private List<Long> getTransactionMembers(long transactionId) {
        List<Long> members = new ArrayList<Long>();
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTION_MEMBER + " WHERE " + TRANSACTION_FK + " = " + transactionId;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                members.add(c.getLong(c.getColumnIndex(MEMBER_FK)));
            } while (c.moveToNext());
        }
        c.close();
        return members;
    }

    public void deleteCurrencies(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_CURRENCY);

    }

    public void deleteDeck(long deckId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_MEMBER + " WHERE " + DECK_FK + " = " + deckId);
        db.execSQL("delete from "+ TABLE_TRANSACTION_MEMBER + " WHERE " + TRANSACTION_FK + " IN (SELECT "
                + KEY_ID + " FROM " + TABLE_TRANSACTION + " WHERE " + DECK_FK + " = " + deckId + ")");
        db.execSQL("delete from "+ TABLE_TRANSACTION + " WHERE " + DECK_FK + " = " + deckId);
        db.execSQL("delete from "+ TABLE_DECK + " WHERE " + KEY_ID + " = " + deckId);

    }


    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_TRANSACTION_MEMBER + " WHERE " + TRANSACTION_FK + " = " + id);
        db.execSQL("delete from "+ TABLE_TRANSACTION + " WHERE " + KEY_ID + " = " + id);
    }
}
