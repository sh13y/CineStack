package com.example.cinestack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CineStack.db";
    private static final int DATABASE_VERSION = 4;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_PROFILE_IMAGE = "profile_image";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Movies table
    private static final String TABLE_MOVIES = "movies";
    private static final String COLUMN_MOVIE_ID = "movie_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_GENRE = "genre";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_REVIEW = "review";
    private static final String COLUMN_USER_ID_FK = "user_id";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                    COLUMN_PROFILE_IMAGE + " TEXT, " +
                    COLUMN_CREATED_AT + " TEXT NOT NULL" +
                    ")";

    private static final String CREATE_MOVIES_TABLE =
            "CREATE TABLE " + TABLE_MOVIES + " (" +
                    COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_GENRE + " TEXT NOT NULL, " +
                    COLUMN_YEAR + " INTEGER NOT NULL, " +
                    COLUMN_REVIEW + " TEXT, " +
                    COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ================= USER METHODS =================

    public boolean registerUser(String username, String email, String password, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false;

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.toLowerCase().trim());
        values.put(COLUMN_EMAIL, email.toLowerCase().trim());
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_FULL_NAME, fullName.trim());
        values.put(COLUMN_PROFILE_IMAGE, "");
        values.put(COLUMN_CREATED_AT, getCurrentTimestamp());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1;
    }

    public boolean loginUser(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE (" + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?) AND " +
                        COLUMN_PASSWORD + "=?",
                new String[]{
                        usernameOrEmail.toLowerCase().trim(),
                        usernameOrEmail.toLowerCase().trim(),
                        hashedPassword
                });

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
    }

    public int getUserId(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS +
                        " WHERE (" + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?) AND " +
                        COLUMN_PASSWORD + "=?",
                new String[]{
                        usernameOrEmail.toLowerCase().trim(),
                        usernameOrEmail.toLowerCase().trim(),
                        hashedPassword
                });

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return userId;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + "=?",
                new String[]{username.toLowerCase().trim()}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isUsernameTaken(String username, int currentUserId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_ID + "!=?",
                new String[]{username.toLowerCase().trim(), String.valueOf(currentUserId)}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isEmailTaken(String email, int currentUserId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_ID + "!=?",
                new String[]{email.toLowerCase().trim(), String.valueOf(currentUserId)}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public Cursor getUserProfileById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, username, email, full_name, profile_image, created_at FROM users WHERE id=?",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean updateUserProfile(int userId, String username, String email, String password, String profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", username.toLowerCase().trim());
        values.put("email", email.toLowerCase().trim());
        values.put("profile_image", profileImage);

        if (password != null && !password.trim().isEmpty()) {
            String hashedPassword = hashPassword(password);
            if (hashedPassword == null) {
                db.close();
                return false;
            }
            values.put("password", hashedPassword);
        }

        int result = db.update(
                "users",
                values,
                "id=?",
                new String[]{String.valueOf(userId)}
        );

        db.close();
        return result > 0;
    }

    public int getMovieCountByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_MOVIES + " WHERE " + COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // ================= MOVIE METHODS =================

    public boolean insertMovie(String title, String genre, int year, String review, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title.trim());
        values.put(COLUMN_GENRE, genre.trim());
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_REVIEW, review.trim());
        values.put(COLUMN_USER_ID_FK, userId);

        long result = db.insert(TABLE_MOVIES, null, values);
        db.close();

        return result != -1;
    }

    public Cursor getMoviesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_MOVIES + " WHERE " + COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean deleteMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_MOVIES,
                COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)}
        );

        db.close();
        return result > 0;
    }

    public boolean updateMovie(int id, String title, String genre, String year, String review) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title.trim());
        values.put(COLUMN_GENRE, genre.trim());
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_REVIEW, review.trim());

        int result = db.update(
                TABLE_MOVIES,
                values,
                COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return result > 0;
    }

    public Cursor searchMovies(int userId, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_MOVIES +
                        " WHERE " + COLUMN_USER_ID_FK + "=? AND " +
                        COLUMN_TITLE + " LIKE ?",
                new String[]{String.valueOf(userId), "%" + keyword + "%"}
        );
    }
}