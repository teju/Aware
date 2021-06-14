package com.watch.aware.app.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.watch.aware.app.models.Steps;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static  int database_version  = 1;
    private final Context context;
    public String Steps = "CREATE TABLE StepsCount (Id INTEGER PRIMARY KEY AUTOINCREMENT, stepsCount TEXT ," +
            "distance TEXT, cal TEXT,dateTime TEXT)";

    private static final String DELETE_STEPS = "DROP TABLE IF EXISTS StepsCount" ;


    public DataBaseHelper(Context context) {
        super(context, "aware_db",null, database_version);
        this.context=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Steps);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_STEPS);
        onCreate(db);
    }


    public boolean stepsInsert(DataBaseHelper dbh, String stepsCount, String dateTime,String distance,String cal){
        SQLiteDatabase sq=dbh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("stepsCount", stepsCount);
        cv.put("dateTime", dateTime);
        cv.put("distance", distance);
        cv.put("cal", cal);
        sq.insert("StepsCount", null, cv);
        return true;

    }

    public List<Steps> getAllSteps(String where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT * FROM StepsCount "+where ;

        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("DataBaseHelper123 getAllSteps " + selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setID(cursor.getString(0));
                steps.setStepCount(cursor.getString(1));
                steps.setDataTime(cursor.getString(2));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        return dataListList;
    }



    public void deleteSteps (String ID) {
        SQLiteDatabase database = this.getWritableDatabase();
        String where = "";
        if(ID.length() != 0) {
            where = " where bike_id = '"+ID+"'";
        }
        String deleteQuery = "Delete from Cart"+where;
        Log.d("DataBaseHelper123 deleteQuery", deleteQuery);
        database.execSQL(deleteQuery);
    }
}
