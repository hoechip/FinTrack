package com.example.fintrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_ENVELOPES = "envelopes";
    public static final String COLUMN_ENV_ID = "id";
    public static final String COLUMN_ENV_NAME = "name";
    public static final String COLUMN_ENV_ICON = "icon";
    public static final String COLUMN_ENV_LIMIT = "budget_limit";
    public static final String COLUMN_ENV_SPENT = "spent";
    public static final String COLUMN_ENV_REMAINING = "remaining";

    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_TRANS_ID = "id";
    public static final String COLUMN_TRANS_TITLE = "title";
    public static final String COLUMN_TRANS_AMOUNT = "amount";
    public static final String COLUMN_TRANS_ENV_ID = "envelope_id";
    public static final String COLUMN_TRANS_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ENVELOPES_TABLE = "CREATE TABLE " + TABLE_ENVELOPES + "("
                + COLUMN_ENV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ENV_NAME + " TEXT,"
                + COLUMN_ENV_ICON + " TEXT,"
                + COLUMN_ENV_LIMIT + " REAL,"
                + COLUMN_ENV_SPENT + " REAL,"
                + COLUMN_ENV_REMAINING + " REAL" + ")";
        db.execSQL(CREATE_ENVELOPES_TABLE);

        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TRANS_TITLE + " TEXT,"
                + COLUMN_TRANS_AMOUNT + " REAL,"
                + COLUMN_TRANS_ENV_ID + " INTEGER,"
                + COLUMN_TRANS_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_TRANS_ENV_ID + ") REFERENCES " + TABLE_ENVELOPES + "(" + COLUMN_ENV_ID + ")" + ")";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);

        insertInitialEnvelopes(db);
    }

    private void insertInitialEnvelopes(SQLiteDatabase db) {
        String[][] initialData = {
                {"Ăn uống", "🍔", "2000000"},
                {"Thuê nhà", "🏠", "5000000"},
                {"Đi chơi", "🎮", "1000000"},
                {"Đi lại", "🚲", "500000"}
        };
        for (String[] data : initialData) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENV_NAME, data[0]);
            values.put(COLUMN_ENV_ICON, data[1]);
            values.put(COLUMN_ENV_LIMIT, Double.parseDouble(data[2]));
            values.put(COLUMN_ENV_SPENT, 0);
            values.put(COLUMN_ENV_REMAINING, Double.parseDouble(data[2]));
            db.insert(TABLE_ENVELOPES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENVELOPES);
        onCreate(db);
    }
}
