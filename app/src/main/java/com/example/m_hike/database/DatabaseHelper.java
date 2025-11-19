package com.example.m_hike.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.m_hike.models.Hike;
import com.example.m_hike.models.Observation;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database info
    private static final String DATABASE_NAME = "MHikeDB";
    private static final int DATABASE_VERSION = 2; // bumped for cascade FK

    // Table names
    private static final String TABLE_HIKES = "Hikes";
    private static final String TABLE_OBSERVATIONS = "Observations";

    // Hikes Table Columns
    private static final String KEY_HIKE_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_DATE = "date";
    private static final String KEY_PARKING = "parking_available";
    private static final String KEY_LENGTH = "length";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DURATION = "estimated_duration";
    private static final String KEY_GROUP_SIZE = "max_group_size";

    // Observations Table Columns
    private static final String KEY_OBS_ID = "id";
    private static final String KEY_HIKE_FK = "hike_id";
    private static final String KEY_OBSERVATION = "observation";
    private static final String KEY_TIME = "time";
    private static final String KEY_COMMENT = "comment";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HIKES_TABLE = "CREATE TABLE " + TABLE_HIKES + "(" +
                KEY_HIKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " TEXT," +
                KEY_LOCATION + " TEXT," +
                KEY_DATE + " TEXT," +
                KEY_PARKING + " TEXT," +
                KEY_LENGTH + " REAL," +
                KEY_DIFFICULTY + " TEXT," +
                KEY_DESCRIPTION + " TEXT," +
                KEY_DURATION + " TEXT," +
                KEY_GROUP_SIZE + " INTEGER" +
                ")";
        db.execSQL(CREATE_HIKES_TABLE);

        // Observations with ON DELETE CASCADE
        String CREATE_OBSERVATIONS_TABLE = "CREATE TABLE " + TABLE_OBSERVATIONS + "(" +
                KEY_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_HIKE_FK + " INTEGER," +
                KEY_OBSERVATION + " TEXT," +
                KEY_TIME + " TEXT," +
                KEY_COMMENT + " TEXT," +
                "FOREIGN KEY(" + KEY_HIKE_FK + ") REFERENCES " + TABLE_HIKES + "(" + KEY_HIKE_ID + ") ON DELETE CASCADE" +
                ")";
        db.execSQL(CREATE_OBSERVATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migrate to add ON DELETE CASCADE without losing data
        if (oldVersion < 2) {
            db.beginTransaction();
            try {
                String CREATE_OBSERVATIONS_TABLE_NEW = "CREATE TABLE Observations_new(" +
                        KEY_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        KEY_HIKE_FK + " INTEGER," +
                        KEY_OBSERVATION + " TEXT," +
                        KEY_TIME + " TEXT," +
                        KEY_COMMENT + " TEXT," +
                        "FOREIGN KEY(" + KEY_HIKE_FK + ") REFERENCES " + TABLE_HIKES + "(" + KEY_HIKE_ID + ") ON DELETE CASCADE" +
                        ")";
                db.execSQL(CREATE_OBSERVATIONS_TABLE_NEW);
                // Remove orphan observations referencing non-existent hikes (data hygiene)
                db.execSQL("DELETE FROM " + TABLE_OBSERVATIONS + " WHERE " + KEY_HIKE_FK + " NOT IN (SELECT " + KEY_HIKE_ID + " FROM " + TABLE_HIKES + ")");
                db.execSQL("INSERT INTO Observations_new (" +
                        KEY_OBS_ID + "," + KEY_HIKE_FK + "," + KEY_OBSERVATION + "," + KEY_TIME + "," + KEY_COMMENT + ") " +
                        "SELECT " + KEY_OBS_ID + "," + KEY_HIKE_FK + "," + KEY_OBSERVATION + "," + KEY_TIME + "," + KEY_COMMENT + " FROM " + TABLE_OBSERVATIONS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATIONS);
                db.execSQL("ALTER TABLE Observations_new RENAME TO " + TABLE_OBSERVATIONS);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // CRUD operations for Hikes and Observations
    public long addHike(Hike hike) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, hike.getName());
        values.put(KEY_LOCATION, hike.getLocation());
        values.put(KEY_DATE, hike.getDate());
        values.put(KEY_PARKING, hike.getParkingAvailable());
        values.put(KEY_LENGTH, hike.getLength());
        values.put(KEY_DIFFICULTY, hike.getDifficulty());
        values.put(KEY_DESCRIPTION, hike.getDescription());
        values.put(KEY_DURATION, hike.getEstimatedDuration());
        values.put(KEY_GROUP_SIZE, hike.getMaxGroupSize());

        long id = db.insert(TABLE_HIKES, null, values);
        db.close();
        return id;
    }

    // Get all hikes
    public List<Hike> getAllHikes() {
        List<Hike> hikeList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HIKES + " ORDER BY " + KEY_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Hike hike = new Hike(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIKE_ID)),       // id
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),        // name
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),    // location
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GROUP_SIZE)),     // maxGroupSize
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DURATION)),    // estimatedDuration
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)), // description
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)),  // difficulty
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LENGTH)),      // length
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_PARKING)),     // parkingAvailable
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                );
                hikeList.add(hike);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return hikeList;
    }

    // Get a single hike by ID
    public Hike getHikeById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIKES, null, KEY_HIKE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Hike hike = null;
        if (cursor.moveToFirst()) {
            hike = new Hike(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIKE_ID)),       // id
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),        // name
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),    // location
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GROUP_SIZE)),     // maxGroupSize
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_DURATION)),    // estimatedDuration
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)), // description
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)),  // difficulty
                    cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LENGTH)),      // length
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PARKING)),     // parkingAvailable
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
            );
            cursor.close();
        }

        db.close();
        return hike;
    }

    // Update a hike
    public int updateHike(Hike hike) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, hike.getName());
        values.put(KEY_LOCATION, hike.getLocation());
        values.put(KEY_DATE, hike.getDate());
        values.put(KEY_PARKING, hike.getParkingAvailable());
        values.put(KEY_LENGTH, hike.getLength());
        values.put(KEY_DIFFICULTY, hike.getDifficulty());
        values.put(KEY_DESCRIPTION, hike.getDescription());
        values.put(KEY_DURATION, hike.getEstimatedDuration());
        values.put(KEY_GROUP_SIZE, hike.getMaxGroupSize());

        int rowsAffected = db.update(TABLE_HIKES, values, KEY_HIKE_ID + "=?", new String[]{String.valueOf(hike.getId())});
        db.close();
        return rowsAffected;
    }

    // Delete a hike (child observations removed first to satisfy FK)
    public void deleteHike(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HIKES, KEY_HIKE_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Unified bulk clear method (formerly deleteAllHikes & clearAllData)
    public void deleteAllHikes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HIKES, null, null); // Observations auto-deleted via cascade
        db.close();
    }

    // Add observation
    public long addObservation(Observation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Use the hikeId foreign key (was incorrectly using observation id)
        values.put(KEY_HIKE_FK, observation.getHikeId());
        values.put(KEY_OBSERVATION, observation.getObservation());
        values.put(KEY_TIME, observation.getTime());
        values.put(KEY_COMMENT, observation.getComment());

        long id = db.insert(TABLE_OBSERVATIONS, null, values);
        db.close();
        return  id;
    }

    // Get all observations for a hike
    public List<Observation> getObservationsForHike(int hikeId) {
        List<Observation> obsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_OBSERVATIONS, null, KEY_HIKE_FK + "=?",
                new String[]{String.valueOf(hikeId)}, null, null, KEY_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Observation obs = new Observation(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_OBS_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIKE_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OBSERVATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT))
                );
                obsList.add(obs);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return obsList;
    }

    // Convenience alias to match call sites
    public List<Observation> getObservationsByHikeId(int hikeId) {
        return getObservationsForHike(hikeId);
    }

    // Get single observation by id
    public Observation getObservationById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_OBSERVATIONS, null, KEY_OBS_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Observation obs = null;
        if (cursor != null && cursor.moveToFirst()) {
            obs = new Observation(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_OBS_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIKE_FK)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_OBSERVATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT))
            );
            cursor.close();
        }
        db.close();
        return obs;
    }

    // Update observation
    public int updateObservation(Observation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_OBSERVATION, observation.getObservation());
        values.put(KEY_TIME, observation.getTime());
        values.put(KEY_COMMENT, observation.getComment());

        int rowsAffected = db.update(TABLE_OBSERVATIONS, values,
                KEY_OBS_ID + "=?", new String[]{String.valueOf(observation.getId())});
        db.close();
        return rowsAffected;
    }

    // Delete observation
    public void deleteObservation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBSERVATIONS, KEY_OBS_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Search hikes by name
    public List<Hike> searchHikesByName(String searchTerm) {
        List<Hike> hikeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_HIKES +
                " WHERE " + KEY_NAME + " LIKE ? ORDER BY " + KEY_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"%" + searchTerm + "%"});

        if (cursor.moveToFirst()) {
            do {
                Hike hike = new Hike(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIKE_ID)),           // id
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),            // name
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),        // location
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GROUP_SIZE)),         // maxGroupSize
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DURATION)),        // estimatedDuration
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),     // description
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)),      // difficulty
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LENGTH)),          // length
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_PARKING)),         // parkingAvailable
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                );
                hikeList.add(hike);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return hikeList;
    }

    /**
     * Advanced search hikes by multiple criteria
     * @param name - search by name (can be partial)
     * @param location - search by location (can be partial)
     * @param date - search by exact date
     * @param minLength - minimum length
     * @param maxLength - maximum length
     * @return List of hikes matching ALL provided criteria
     */

    public List<Hike> advancedSearchHikes(String name, String location, String date,
                                          Double minLength, Double maxLength) {
        List<Hike> hikeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selectQuery = new StringBuilder("SELECT * FROM " + TABLE_HIKES + " WHERE 1=1");
        List<String> argsList = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            selectQuery.append(" AND " + KEY_NAME + " LIKE ?");
            argsList.add("%" + name + "%");
        }
        if (location != null && !location.isEmpty()) {
            selectQuery.append(" AND " + KEY_LOCATION + " LIKE ?");
            argsList.add("%" + location + "%");
        }
        if (date != null && !date.isEmpty()) {
            selectQuery.append(" AND " + KEY_DATE + " = ?");
            argsList.add(date);
        }
        if (minLength != null) {
            selectQuery.append(" AND " + KEY_LENGTH + " >= ?");
            argsList.add(String.valueOf(minLength));
        }
        if (maxLength != null) {
            selectQuery.append(" AND " + KEY_LENGTH + " <= ?");
            argsList.add(String.valueOf(maxLength));
        }

        selectQuery.append(" ORDER BY " + KEY_DATE + " DESC");

        Cursor cursor = db.rawQuery(selectQuery.toString(), argsList.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Hike hike = new Hike(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIKE_ID)),           // id
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),            // name
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),        // location
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GROUP_SIZE)),         // maxGroupSize
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DURATION)),        // estimatedDuration
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),     // description
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)),      // difficulty
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LENGTH)),          // length
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_PARKING)),         // parkingAvailable
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                );
                hikeList.add(hike);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return hikeList;
    }
}
