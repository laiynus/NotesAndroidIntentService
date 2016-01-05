package by.khrapovitsky.notesclient.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import by.khrapovitsky.notesclient.model.Archive;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "notesManager";

    private static final String TABLE_NOTES = "notes";

    private static final String KEY_ID = "_id";
    private static final String KEY_NOTE_PATH = "txtPath";
    private static final String KEY_IMAGE_PATH = "imagePath";
    private static final String KEY_LAST_DATE_MODIFY = "lastDateModify";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_NOTE_PATH + " TEXT,"
                + KEY_IMAGE_PATH + " TEXT,"
                + KEY_LAST_DATE_MODIFY + " DATETIME" + ")";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public void createNote(Archive archive, String archiveName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID,archiveName);
        values.put(KEY_NOTE_PATH, archive.getTxtPath());
        values.put(KEY_IMAGE_PATH, archive.getImgPath());
        String[] date = archiveName.split("\\.");
        Date convertDate = new Date(Long.parseLong(date[0]));
        Format format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        values.put(KEY_LAST_DATE_MODIFY, format.format(convertDate));
        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    public Cursor getAllForAdapter(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT _id,txtPath,imagePath,lastDateModify FROM " + TABLE_NOTES,null);
    }

}
