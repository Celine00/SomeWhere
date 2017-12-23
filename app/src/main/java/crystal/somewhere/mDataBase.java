package crystal.somewhere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by Crystal on 2017/12/18.
 */

public class mDataBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "SomeWhere.db";
    private static final String TABLE_NAME = "SomeWhereTable";
    private static final int DB_VERSION = 1;

    public mDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY, "
                + "latitude REAL, "
                + "longitude REAL, "
                + "name TEXT NOT NULL, "
                + "description TEXT);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void insert(double latitude, double longitude, String name, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("name", name);
        values.put("description", description);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public  void update(String name, String description) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "name = ?";    //主键列名
        String[] whereArgs = { name };  //主键的值
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close();
    }

    public void delete(double latitude, double longitude) {
        SQLiteDatabase db = getWritableDatabase();
        //Log.e("debug", Double.toString(latitude)+" "+Double.toString(longitude));
        db.delete(TABLE_NAME, "latitude = ? AND longitude = ?", new String[]{Double.toString(latitude), Double.toString(longitude)});
        db.close();
    }

    //获取所有标记的names
    public ArrayList<String> query(double latitude, double longitude) {
        ArrayList<String> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"latitude", "longitude", "name", "description"},
                "latitude=? " + "AND longitude=?", new String[]{Double.toString(latitude), Double.toString(longitude)}, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            result.add(name);
            result.add(description);
        }
        cursor.close();
        db.close();
        return result;
    }
}
