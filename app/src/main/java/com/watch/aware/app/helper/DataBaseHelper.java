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
    private static  int database_version  = 6;
    private final Context context;
    public String Steps = "CREATE TABLE StepsCount (Id INTEGER PRIMARY KEY AUTOINCREMENT, stepsCount TEXT UNIQUE," +
            "distance TEXT, cal TEXT,date TEXT,time TEXT)";

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


    public boolean stepsInsert(DataBaseHelper dbh, String stepsCount, String date,String distance,
                               String cal,String time){
        SQLiteDatabase sq=dbh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        System.out.println("DataBaseHelper123 stepsInsert " + stepsCount+" time "+time);

        cv.put("stepsCount", stepsCount);
        cv.put("date", date);
        cv.put("distance", distance);
        cv.put("cal", cal);
        cv.put("time", time);
        sq.insert("StepsCount", null, cv);
        return true;

    }

    public List<Steps> getAllSteps(String where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT * FROM StepsCount "+where ;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setID(cursor.getString(0));
                steps.setStepCount(cursor.getString(1));
                steps.setDistance(cursor.getString(2));
                steps.setCal(cursor.getString(3));
                steps.setDate(cursor.getString(4));
                steps.setTime(cursor.getString(5));
                System.out.println(" DataBaseHelper123 steps :"
                        + cursor.getString(1)+
                        " time "+cursor.getString(5) +" date "+cursor.getString(4));

                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        System.out.println("DataBaseHelper123 getAllSteps " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }
    public List<Steps> getAllStepsDaily(int from,int to) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT stepsCount ,cal, distance ," +
                "CAST(strftime('%h', date) AS integer) AS dow FROM StepsCount WHERE dow BETWEEN "+ from+" AND"+to+"  ORDER BY stepsCount DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setStepCount(cursor.getString(0));
                steps.setCal(cursor.getString(1));
                steps.setDistance(cursor.getString(2));
                System.out.println(" DataBaseHelper123 getAllStepsWeekly :"
                        + cursor.getString(0)+" time "+cursor.getString(1));

                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        System.out.println("DataBaseHelper123 getAllStepsWeekly " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }

    public List<Steps> getAllStepsWeekly(int where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT stepsCount ,cal, distance ," +
                "CAST(strftime('%w', date) AS integer) AS dow FROM StepsCount WHERE dow = "+ where+"  ORDER BY stepsCount DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setStepCount(cursor.getString(0));
                steps.setCal(cursor.getString(1));
                steps.setDistance(cursor.getString(2));
                System.out.println(" DataBaseHelper123 getAllStepsWeekly :"
                        + cursor.getString(0)+" time "+cursor.getString(1));

                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        System.out.println("DataBaseHelper123 getAllStepsWeekly " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }
    public List<Steps> getAllStepsMonthly(int where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT stepsCount ,cal, distance ,CAST(strftime('%m', date) AS integer)" +
                " AS dow FROM StepsCount WHERE dow = "+ where+"  ORDER BY stepsCount DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setStepCount(cursor.getString(0));
                steps.setCal(cursor.getString(1));
                steps.setDistance(cursor.getString(2));
                System.out.println(" DataBaseHelper123 getAllStepsMonthly :"
                        + cursor.getString(0)+" time "+cursor.getString(1));

                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        System.out.println("DataBaseHelper123 getAllStepsWeekly " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }

    public void deleteSteps (String ID) {
        SQLiteDatabase database = this.getWritableDatabase();
        String where = "";
        if(ID.length() != 0) {
            where = " where stepsCount = "+ID+"";
        }
        String deleteQuery = "Delete from StepsCount"+where;
        System.out.println("DataBaseHelper123 deleteQuery " + deleteQuery);

        database.execSQL(deleteQuery);
    }

    public boolean update(String s) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE StepsCount SET stepsCount = "+s);
        return true;
    }
}
