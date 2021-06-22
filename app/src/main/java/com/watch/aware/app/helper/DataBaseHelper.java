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
    private static  int database_version  = 9;
    private final Context context;
    public String Steps = "CREATE TABLE StepsCount (Id INTEGER PRIMARY KEY AUTOINCREMENT, stepsCount TEXT UNIQUE," +
            "distance TEXT, cal TEXT,date DATE,time TEXT UNIQUE,total_count TEXT)";

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
                               String cal,String time,int total_count){
        SQLiteDatabase sq=dbh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        System.out.println("DataBaseHelper123 stepsInsert " + stepsCount+" time "+time);

        cv.put("stepsCount", stepsCount);
        cv.put("date", date);
        cv.put("distance", distance);
        cv.put("cal", cal);
        cv.put("time", time);
        cv.put("total_count", total_count);
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
                steps.setTotal_count(cursor.getString(6));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        for (Steps data: dataListList){
            System.out.println(" DataBaseHelper123 getAllSteps :"
                    + data.getStepCount()+
                    " time "+data.getTime() +
                    " date "+data.getDate() +" total "+data.getTotal_count());

        }
        System.out.println("DataBaseHelper123 getAllSteps " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }

    public List<Steps> getAllStepsWeekly(int where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT * FROM StepsCount WHERE CAST(strftime('%w', date) AS integer) = "+ where+" ORDER BY CAST(total_count as INT) DESC";

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
                steps.setTotal_count(cursor.getString(6));
                System.out.println(" DataBaseHelper123 getAllStepsWeekly :"
                        + steps.getStepCount()+" time "+steps.getTime()+
                        " date "+steps.getDate()+" total "+steps.getTotal_count());

                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        for(Steps print:dataListList ) {
            System.out.println(" DataBaseHelper123 getAllStepsMonthly steps :"
                    + print.getStepCount()+" time "+print.getTime()+" total count "+print.getTotal_count() +" "+dataListList.get(0).getStepCount());

        }
        System.out.println("DataBaseHelper123 getAllStepsWeekly " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }
    public List<Steps> getAllStepsMonthly(int where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT * from (SELECT * FROM StepsCount ) AS sub  WHERE CAST(strftime('%m', date) AS integer) = "+ where+" GROUP BY date ";

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
                steps.setTotal_count(cursor.getString(6));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        for(Steps print:dataListList ) {
            System.out.println(" DataBaseHelper123 getAllStepsMonthly steps :"
                    + print.getStepCount()+" time "+print.getTime()+" total count "+print.getTotal_count());

        }
        System.out.println("DataBaseHelper123 getAllStepsMonthly " + selectQuery +" dataListList "+dataListList.size());

        return dataListList;
    }
    public List<Steps> getAllStepsYearly(int where) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT * from (SELECT * FROM StepsCount ) AS sub  WHERE CAST(strftime('%Y', date) AS integer) = "+ where+" GROUP BY date ";

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
                steps.setTotal_count(cursor.getString(6));

                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        for(Steps print:dataListList ) {
            System.out.println(" DataBaseHelper123 getAllStepsYearly steps :"
                    + print.getStepCount()+" time "+print.getTime()+" total count "+print.getTotal_count());

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
