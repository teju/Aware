package com.watch.aware.app.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.iapps.libs.helpers.BaseHelper;
import com.watch.aware.app.models.HeartRate;
import com.watch.aware.app.models.SpoRate;
import com.watch.aware.app.models.Steps;
import com.watch.aware.app.models.TempRate;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static  int database_version  = 23;
    private final Context context;
    public String Steps = "CREATE TABLE StepsCount (stepsCount TEXT ," +
            "distance TEXT ,cal TEXT,date DATE,time TEXT , total_steps TEXT , total_cal TEXT , total_dist TEXT, " +
            "PRIMARY KEY (time,date))";

    public String HeartRate = "CREATE TABLE HeartRate (heartRate int ,date DATE,time double ,PRIMARY KEY (time,heartRate))";
    public String SpoRate = "CREATE TABLE SpoRate (Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "SpoRate int ,date DATE,time decimal UNIQUE)";
    public String TempRate = "CREATE TABLE TempRate (TempRate decimal ,date DATE,time decimal,  PRIMARY KEY (time,TempRate))";

    private static final String DELETE_STEPS = "DROP TABLE IF EXISTS StepsCount" ;
    private static final String DELETE_HeartRate = "DROP TABLE IF EXISTS HeartRate" ;
    private static final String DELETE_SpoRate = "DROP TABLE IF EXISTS SpoRate" ;
    private static final String DELETE_TempRate = "DROP TABLE IF EXISTS TempRate" ;


    public DataBaseHelper(Context context) {
        super(context, "aware_db",null, database_version);
        this.context=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Steps);
        db.execSQL(HeartRate);
        db.execSQL(SpoRate);
        db.execSQL(TempRate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_STEPS);
        db.execSQL(DELETE_HeartRate);
        db.execSQL(DELETE_SpoRate);
        db.execSQL(DELETE_TempRate);
        onCreate(db);
    }

    public boolean heartInsert(DataBaseHelper dbh, int heartRate, String date, String time){
        try {
            SQLiteDatabase sq = dbh.getWritableDatabase();
            ContentValues cv = new ContentValues();
            System.out.println("DataBaseHelper123 heartRateInsert " + heartRate + " time " + time+" date "+date);
            cv.put("heartRate", heartRate);
            cv.put("date", date);
            cv.put("time", time);
            sq.insert("HeartRate", null, cv);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public boolean SPoInsert(DataBaseHelper dbh, int SpoRate, String date, String time){
        try {
            SQLiteDatabase sq = dbh.getWritableDatabase();
            ContentValues cv = new ContentValues();
            System.out.println("DataBaseHelper123 SpoRateInsert " + SpoRate + " time " + time);
            cv.put("SpoRate", SpoRate);
            cv.put("date", date);
            cv.put("time", time);
            sq.insert("SpoRate", null, cv);
        }catch (Exception e){

        }
        return true;

    }

    public boolean TempInsert(DataBaseHelper dbh, Double TempRate, String date, String time){
        try {
            SQLiteDatabase sq = dbh.getWritableDatabase();
            ContentValues cv = new ContentValues();
            System.out.println("DataBaseHelper123 TempRateInsert " + TempRate + " time " + time);
            cv.put("TempRate", TempRate);
            cv.put("date", date);
            cv.put("time", time);
            sq.insert("TempRate", null, cv);
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }


    public boolean stepsInsert(DataBaseHelper dbh, String stepsCount, String date,String distance,
                               String cal,String time,String total_steps,String total_cal,String total_dist){
        if(!BaseHelper.isEmpty(time)) {
            try {
                SQLiteDatabase sq = dbh.getWritableDatabase();
                ContentValues cv = new ContentValues();
                System.out.println("DataBaseHelper123 stepsInsert "
                        + stepsCount + " time " + time+ " date "+date +" total "+total_steps);

                cv.put("stepsCount", stepsCount);
                cv.put("date", date);
                cv.put("distance", distance);
                cv.put("cal", cal);
                cv.put("time", time);
                cv.put("total_steps", total_steps);
                cv.put("total_dist", total_dist);
                cv.put("total_cal", total_cal);
                sq.insert("StepsCount", null, cv);
            } catch (Exception e) {
                e.toString();
                System.out.println("DataBaseHelper123 stepsInsert Exception " + e.toString());

            }
        }
        return true;

    }
    public List<Steps> getAllSteps(String where) {
        List<Steps> dataListList = new ArrayList<Steps>();
        try {

            String selectQuery = "SELECT * FROM StepsCount " + where;

            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Steps steps = new Steps();
                    steps.setStepCount(cursor.getString(0));
                    steps.setDistance(cursor.getString(1));
                    steps.setCal(cursor.getString(2));
                    steps.setDate(cursor.getString(3));
                    steps.setTime(cursor.getString(4));
                    steps.setTotal_steps(cursor.getString(5));
                    steps.setTotal_cal(cursor.getString(6));
                    steps.setTotal_dist(cursor.getString(7));
                    dataListList.add(steps);
                } while (cursor.moveToNext());
            }
            System.out.println("DataBaseHelper123 getAllSteps " + selectQuery + " dataListList " + dataListList.size());
        } catch (SQLiteCantOpenDatabaseException e){
            return null;
        }
        catch (Exception e){
            return dataListList;
        }
        return dataListList;
    }
    public int getMaxSteps(String date,String type) {
       double maxStep = 0.0;
        try {

            String selectQuery = "SELECT CAST(stepsCount AS INTEGER) as steps ,date,time from StepsCount where date is '" +date+
                    "' order by steps desc Limit 1";
            System.out.println("DataBaseHelper123 getAllSteps " + selectQuery );

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            maxStep = cursor.getDouble(0);

        } catch (Exception e){

        }
        return (int)maxStep;
    }

    public List<com.watch.aware.app.models.HeartRate> getAllHeartRate(String where) {
        List<HeartRate> dataListList = new ArrayList<HeartRate>();
        try {

            String selectQuery = "SELECT * FROM HeartRate " + where;

            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    HeartRate steps = new HeartRate();
                    steps.setHeartRate(cursor.getInt(0));
                    steps.setDate(cursor.getString(1));
                    steps.setTime(cursor.getString(2));
                    dataListList.add(steps);
                } while (cursor.moveToNext());
            }
            System.out.println("DataBaseHelper123 getAllHeartRate " + selectQuery);

        } catch (Exception e){
            e.printStackTrace();
        }
        return dataListList;
    }
    public List<com.watch.aware.app.models.SpoRate> getAllSpoRate(String where) {
        List<com.watch.aware.app.models.SpoRate> dataListList = new ArrayList<SpoRate>();
        try {

            String selectQuery = "SELECT  * FROM SpoRate " + where;

            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    SpoRate steps = new SpoRate();
                    steps.setID(cursor.getString(0));
                    steps.setSpoRate(cursor.getInt(1));
                    steps.setDate(cursor.getString(2));
                    steps.setTime(cursor.getString(3));
                    dataListList.add(steps);
                } while (cursor.moveToNext());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return dataListList;
    }
    public List<com.watch.aware.app.models.TempRate> getAllTemp(String where) {
        List<com.watch.aware.app.models.TempRate> dataListList = new ArrayList<TempRate>();
        try {

            String selectQuery = "SELECT * FROM TempRate " + where;

            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    com.watch.aware.app.models.TempRate steps = new TempRate();
                    steps.setTempRate(cursor.getDouble(0));
                    steps.setDate(cursor.getString(1));
                    steps.setTime(cursor.getString(2));
                    dataListList.add(steps);
                } while (cursor.moveToNext());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return dataListList;
    }
    public List<Steps> getAllStepsWeekly(int week,int month ) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT rowid, stepsCount,distance,cal,date,time FROM StepsCount" +
                " WHERE CAST(strftime('%w', date) AS integer) = "+ week+ " AND CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022 AND stepsCount != 0  AND DATE(date) >= DATE('now', 'weekday 0', '-7 days') ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setStepCount(cursor.getString(1));
                steps.setDistance(cursor.getString(2));
                steps.setCal(cursor.getString(3));
                steps.setDate(cursor.getString(4));
                steps.setTime(cursor.getString(5));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }

        return dataListList;
    }


    public List<HeartRate> getAllHeartRatesWeekly(int week,int month ) {
        List<HeartRate> dataListList = new ArrayList<HeartRate>();

        String selectQuery = "SELECT *  FROM HeartRate" +
                " WHERE CAST(strftime('%w', date) AS integer) = "+ week+ " AND CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022 " +
                "AND DATE(date) >= DATE('now', 'weekday 0', '-7 days') AND heartRate != 0 ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                HeartRate steps = new HeartRate();
                steps.setHeartRate(cursor.getInt(0));
                steps.setDate(cursor.getString(1));
                steps.setTime(cursor.getString(2));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }

        return dataListList;
    }
    public List<SpoRate> getAllSpoWeekly(int week,int month ) {
        List<SpoRate> dataListList = new ArrayList<SpoRate>();

        String selectQuery = "SELECT *  FROM SpoRate" +
                " WHERE CAST(strftime('%w', date) AS integer) = "+ week+ " AND CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022 " +
                "AND DATE(date) >= DATE('now', 'weekday 0', '-7 days') AND SpoRate != 0 ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SpoRate steps = new SpoRate();
                steps.setID(cursor.getString(0));
                steps.setSpoRate(cursor.getInt(1));
                steps.setDate(cursor.getString(2));
                steps.setTime(cursor.getString(3));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }

        return dataListList;
    }
    public List<TempRate> getAllTempWeekly(int week,int month ) {
        List<TempRate> dataListList = new ArrayList<TempRate>();

        String selectQuery = "SELECT *  FROM TempRate" +
                " WHERE CAST(strftime('%w', date) AS integer) = "+ week+ " AND CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022 " +
                "AND DATE(date) >= DATE('now', 'weekday 0', '-7 days') AND TempRate != 0 ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TempRate steps = new TempRate();
                steps.setTempRate(cursor.getDouble(0));
                steps.setDate(cursor.getString(1));
                steps.setTime(cursor.getString(2));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }

        return dataListList;
    }

    public List<Steps> getAllStepsMonthly(int month) {
        List<Steps> dataListList = new ArrayList<Steps>();

        String selectQuery = "SELECT rowid, stepsCount,distance,cal,date,time FROM StepsCount" +
                " WHERE CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022  ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Steps steps = new Steps();
                steps.setStepCount(cursor.getString(1));
                steps.setDistance(cursor.getString(2));
                steps.setCal(cursor.getString(3));
                steps.setDate(cursor.getString(4));
                steps.setTime(cursor.getString(5));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        System.out.println("DataBaseHelper123 getAllStepsMonthly " + selectQuery );

        return dataListList;
    }

    public List<HeartRate> getAllHeartMonthly(int month) {
        List<HeartRate> dataListList = new ArrayList<HeartRate>();

        String selectQuery = "SELECT *  FROM HeartRate" +
                " WHERE CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022  AND heartRate != 0 ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                HeartRate steps = new HeartRate();
                steps.setHeartRate(cursor.getInt(0));
                steps.setDate(cursor.getString(1));
                steps.setTime(cursor.getString(2));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        return dataListList;
    }

    public List<SpoRate> getAllSpoMonthly(int month) {
        List<SpoRate> dataListList = new ArrayList<SpoRate>();

        String selectQuery = "SELECT *  FROM SpoRate" +
                " WHERE CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022  AND SpoRate != 0 ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SpoRate steps = new SpoRate();
                steps.setID(cursor.getString(0));
                steps.setSpoRate(cursor.getInt(1));
                steps.setDate(cursor.getString(2));
                steps.setTime(cursor.getString(3));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        return dataListList;
    }
    public List<TempRate> getAllTempMonthly(int month) {
        List<TempRate> dataListList = new ArrayList<TempRate>();

        String selectQuery = "SELECT *  FROM TempRate" +
                " WHERE CAST(strftime('%m', date) AS integer) = "+month+" " +
                " AND CAST(strftime('%Y', date) AS integer) = 2022  AND TempRate != 0 ORDER BY time DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TempRate steps = new TempRate();
                steps.setTempRate(cursor.getDouble(0));
                    steps.setDate(cursor.getString(1));
                steps.setTime(cursor.getString(2));
                dataListList.add(steps);
            } while (cursor.moveToNext());
        }
        return dataListList;
    }
    public void deleteSteps (String time,String date) {
        SQLiteDatabase database = this.getWritableDatabase();
        String where = " where time = '"+time+"' AND date = '"+date+"'";
        String deleteQuery = "Delete from StepsCount"+where;
        System.out.println("DataBaseHelper123 deleteQuery " + deleteQuery);
        database.execSQL(deleteQuery);
    }

    public boolean update(String s,String time,String date,DataBaseHelper dbh,
                          String total_steps,String total_cal,String total_dist) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        System.out.println("DataBaseHelper123 update date " + date+" time "+time+" s "+s);
        db.execSQL("UPDATE StepsCount SET stepsCount = "+s+" total_steps = "+total_steps+
                " total_cal = " +total_cal+" total_dist = "+total_dist+" where time = '"+time+"' AND date = '"+date+"'" );
        return true;
    }
}
