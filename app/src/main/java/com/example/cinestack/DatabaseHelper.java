package com.example.cinestack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CineStack.db";
    private static final int DATABASE_VERSION = 6;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_SECURITY_QUESTION = "security_question";
    private static final String COLUMN_SECURITY_ANSWER = "security_answer";
    private static final String COLUMN_PROFILE_IMAGE = "profile_image";

    // Legacy movies table (manual user entries)
    private static final String TABLE_MOVIES = "movies";
    private static final String COLUMN_MOVIE_ID = "movie_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_GENRE = "genre";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_REVIEW = "review";
    private static final String COLUMN_USER_ID_FK = "user_id";

    // User category preferences
    private static final String TABLE_USER_CATEGORIES = "user_categories";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_CATEGORY = "category";

    // TMDB wishlist items
    private static final String TABLE_WISHLIST = "wishlist_items";
    private static final String COLUMN_WISHLIST_ID = "wishlist_id";
    private static final String COLUMN_TMDB_ID = "tmdb_id";
    private static final String COLUMN_MEDIA_TYPE = "media_type";
    private static final String COLUMN_OVERVIEW = "overview";
    private static final String COLUMN_POSTER_PATH = "poster_path";
    private static final String COLUMN_RELEASE_YEAR = "release_year";
    private static final String COLUMN_VOTE_AVERAGE = "vote_average";
    private static final String COLUMN_USER_REVIEW = "user_review";
    private static final String COLUMN_USER_RATING = "user_rating";
    private static final String COLUMN_IS_WATCHED = "is_watched";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                    COLUMN_CREATED_AT + " TEXT NOT NULL, " +
                    COLUMN_SECURITY_QUESTION + " TEXT NOT NULL, " +
                    COLUMN_SECURITY_ANSWER + " TEXT NOT NULL, " +
                    COLUMN_PROFILE_IMAGE + " TEXT" +
                    ")";

    private static final String CREATE_MOVIES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MOVIES + " (" +
                    COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_GENRE + " TEXT NOT NULL, " +
                    COLUMN_YEAR + " INTEGER NOT NULL, " +
                    COLUMN_REVIEW + " TEXT, " +
                    COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_USER_CATEGORIES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER_CATEGORIES + " (" +
                    COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    "UNIQUE(" + COLUMN_USER_ID_FK + ", " + COLUMN_CATEGORY + "), " +
                    "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_WISHLIST_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_WISHLIST + " (" +
                    COLUMN_WISHLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
                    COLUMN_TMDB_ID + " INTEGER NOT NULL, " +
                    COLUMN_MEDIA_TYPE + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_GENRE + " TEXT, " +
                    COLUMN_OVERVIEW + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT, " +
                    COLUMN_RELEASE_YEAR + " TEXT, " +
                    COLUMN_VOTE_AVERAGE + " REAL DEFAULT 0, " +
                    COLUMN_USER_REVIEW + " TEXT, " +
                    COLUMN_USER_RATING + " REAL DEFAULT 0, " +
                    COLUMN_IS_WATCHED + " INTEGER DEFAULT 0, " +
                    COLUMN_CREATED_AT + " TEXT NOT NULL, " +
                    COLUMN_UPDATED_AT + " TEXT NOT NULL, " +
                    "UNIQUE(" + COLUMN_USER_ID_FK + ", " + COLUMN_TMDB_ID + ", " + COLUMN_MEDIA_TYPE + "), " +
                    "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MOVIES_TABLE);
        db.execSQL(CREATE_USER_CATEGORIES_TABLE);
        db.execSQL(CREATE_WISHLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            addColumnIfMissing(db, TABLE_USERS, COLUMN_SECURITY_QUESTION, "TEXT NOT NULL DEFAULT ''");
            addColumnIfMissing(db, TABLE_USERS, COLUMN_SECURITY_ANSWER, "TEXT NOT NULL DEFAULT ''");
            addColumnIfMissing(db, TABLE_USERS, COLUMN_PROFILE_IMAGE, "TEXT");
        }

        if (oldVersion < 6) {
            db.execSQL(CREATE_USER_CATEGORIES_TABLE);
            db.execSQL(CREATE_WISHLIST_TABLE);
        }
    }

    private void addColumnIfMissing(SQLiteDatabase db, String table, String column, String type) {
        if (!hasColumn(db, table, column)) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }

    private boolean hasColumn(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (column.equalsIgnoreCase(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
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
            return null;
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ================= USER METHODS =================

    public boolean registerUser(String username, String email, String password, String fullName,
                                String securityQuestion, String securityAnswer) {

        SQLiteDatabase db = this.getWritableDatabase();

        String hashedPassword = hashPassword(password);
        String hashedAnswer = hashPassword(securityAnswer.toLowerCase().trim());
        if (hashedPassword == null || hashedAnswer == null) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.toLowerCase().trim());
        values.put(COLUMN_EMAIL, email.toLowerCase().trim());
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_FULL_NAME, fullName.trim());
        values.put(COLUMN_CREATED_AT, getCurrentTimestamp());
        values.put(COLUMN_SECURITY_QUESTION, securityQuestion);
        values.put(COLUMN_SECURITY_ANSWER, hashedAnswer);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1;
    }

    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE (" + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?) AND " +
                        COLUMN_PASSWORD + "=?",
                new String[]{username.toLowerCase().trim(),
                        username.toLowerCase().trim(),
                        hashedPassword});

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
    }

    public int getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS +
                        " WHERE (" + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?) AND " +
                        COLUMN_PASSWORD + "=?",
                new String[]{username.toLowerCase().trim(),
                        username.toLowerCase().trim(),
                        hashedPassword});

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
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?",
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
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[]{email.toLowerCase().trim()}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public String getUserFullName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_FULL_NAME + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USERNAME + " = ? OR " + COLUMN_EMAIL + " = ?",
                new String[]{username.toLowerCase().trim(), username.toLowerCase().trim()});

        String fullName = null;
        if (cursor.moveToFirst()) {
            fullName = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return fullName;
    }

    public String getSecurityQuestion(String usernameOrEmail) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_SECURITY_QUESTION + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?",
                new String[]{usernameOrEmail.toLowerCase().trim(),
                        usernameOrEmail.toLowerCase().trim()});

        String question = null;
        if (cursor.moveToFirst()) {
            question = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return question;
    }

    public boolean verifySecurityAnswer(String usernameOrEmail, String answer) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedAnswer = hashPassword(answer.toLowerCase().trim());

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS +
                        " WHERE (" + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?) AND " +
                        COLUMN_SECURITY_ANSWER + "=?",
                new String[]{usernameOrEmail.toLowerCase().trim(),
                        usernameOrEmail.toLowerCase().trim(),
                        hashedAnswer});

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
    }

    public boolean updatePassword(String usernameOrEmail, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        String hashedPassword = hashPassword(newPassword);
        if (hashedPassword == null) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, hashedPassword);

        int result = db.update(TABLE_USERS, values,
                COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?",
                new String[]{usernameOrEmail.toLowerCase().trim(),
                        usernameOrEmail.toLowerCase().trim()});

        db.close();
        return result > 0;
    }

    public boolean checkUserExists(String usernameOrEmail) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?",
                new String[]{usernameOrEmail.toLowerCase().trim(),
                        usernameOrEmail.toLowerCase().trim()});

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // ================= PROFILE METHODS =================

    public Cursor getUserProfileById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public int getMovieCountByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT (" +
                        "(SELECT COUNT(*) FROM " + TABLE_MOVIES + " WHERE " + COLUMN_USER_ID_FK + "=?) + " +
                        "(SELECT COUNT(*) FROM " + TABLE_WISHLIST + " WHERE " + COLUMN_USER_ID_FK + "=?)" +
                        ")",
                new String[]{String.valueOf(userId), String.valueOf(userId)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public boolean isUsernameTaken(String username, int excludeUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_ID + "!=?",
                new String[]{username.toLowerCase().trim(), String.valueOf(excludeUserId)});

        boolean taken = cursor.moveToFirst();
        cursor.close();
        db.close();
        return taken;
    }

    public boolean isEmailTaken(String email, int excludeUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_ID + "!=?",
                new String[]{email.toLowerCase().trim(), String.valueOf(excludeUserId)});

        boolean taken = cursor.moveToFirst();
        cursor.close();
        db.close();
        return taken;
    }

    public boolean updateUserProfile(int userId, String username, String email,
                                     String password, String profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.toLowerCase().trim());
        values.put(COLUMN_EMAIL, email.toLowerCase().trim());
        values.put(COLUMN_PROFILE_IMAGE, profileImage);

        if (password != null && !password.isEmpty()) {
            String hashedPassword = hashPassword(password);
            if (hashedPassword != null) {
                values.put(COLUMN_PASSWORD, hashedPassword);
            }
        }

        int result = db.update(TABLE_USERS, values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)});

        db.close();
        return result > 0;
    }

    // ================= CATEGORY METHODS =================

    public boolean saveUserCategories(int userId, List<String> categories) {
        if (categories == null || categories.size() < 3) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_USER_CATEGORIES, COLUMN_USER_ID_FK + "=?", new String[]{String.valueOf(userId)});

            for (String category : categories) {
                if (TextUtils.isEmpty(category)) {
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID_FK, userId);
                values.put(COLUMN_CATEGORY, category.trim());
                db.insertWithOnConflict(TABLE_USER_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean hasUserCategories(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USER_CATEGORIES + " WHERE " + COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}
        );

        boolean hasCategories = false;
        if (cursor.moveToFirst()) {
            hasCategories = cursor.getInt(0) >= 3;
        }
        cursor.close();
        db.close();
        return hasCategories;
    }

    public List<String> getUserCategories(int userId) {
        ArrayList<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_CATEGORY + " FROM " + TABLE_USER_CATEGORIES + " WHERE " + COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return categories;
    }

    // ================= LEGACY MOVIE METHODS =================

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
                "SELECT * FROM " + TABLE_MOVIES +
                        " WHERE " + COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)});
    }

    public boolean deleteMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MOVIES,
                COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)});
        db.close();
        return result > 0;
    }

    public boolean updateMovie(int id, String title, String genre,
                               String year, String review) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_GENRE, genre);
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_REVIEW, review);

        int result = db.update(TABLE_MOVIES,
                values,
                COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(id)});

        db.close();
        return result > 0;
    }

    public Cursor searchMovies(int userId, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_MOVIES +
                        " WHERE " + COLUMN_USER_ID_FK + "=? AND " + COLUMN_TITLE + " LIKE ?",
                new String[]{String.valueOf(userId), "%" + keyword + "%"}
        );
    }

    // ================= WISHLIST METHODS =================

    public boolean upsertWishlistItem(int userId, TmdbMediaItem item) {
        if (item == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_TMDB_ID, item.getTmdbId());
        values.put(COLUMN_MEDIA_TYPE, item.getMediaType());
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_GENRE, item.getGenre());
        values.put(COLUMN_OVERVIEW, item.getOverview());
        values.put(COLUMN_POSTER_PATH, item.getPosterPath());
        values.put(COLUMN_RELEASE_YEAR, item.getYear());
        values.put(COLUMN_VOTE_AVERAGE, item.getVoteAverage());
        values.put(COLUMN_UPDATED_AT, getCurrentTimestamp());

        if (!isWishlistItemExists(userId, item.getTmdbId(), item.getMediaType())) {
            values.put(COLUMN_CREATED_AT, getCurrentTimestamp());
            long result = db.insert(TABLE_WISHLIST, null, values);
            db.close();
            return result != -1;
        } else {
            int result = db.update(TABLE_WISHLIST,
                    values,
                    COLUMN_USER_ID_FK + "=? AND " + COLUMN_TMDB_ID + "=? AND " + COLUMN_MEDIA_TYPE + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(item.getTmdbId()), item.getMediaType()});
            db.close();
            return result > 0;
        }
    }

    public boolean isWishlistItemExists(int userId, int tmdbId, String mediaType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_WISHLIST +
                        " WHERE " + COLUMN_USER_ID_FK + "=? AND " + COLUMN_TMDB_ID + "=? AND " + COLUMN_MEDIA_TYPE + "=?",
                new String[]{String.valueOf(userId), String.valueOf(tmdbId), mediaType}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public Cursor getWishlistByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_WISHLIST +
                        " WHERE " + COLUMN_USER_ID_FK + "=? ORDER BY " + COLUMN_UPDATED_AT + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean removeWishlistItem(int wishlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_WISHLIST,
                COLUMN_WISHLIST_ID + "=?",
                new String[]{String.valueOf(wishlistId)});
        db.close();
        return result > 0;
    }

    public boolean updateWishlistReview(int wishlistId, float rating, String review, boolean watched) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_RATING, rating);
        values.put(COLUMN_USER_REVIEW, review == null ? "" : review.trim());
        values.put(COLUMN_IS_WATCHED, watched ? 1 : 0);
        values.put(COLUMN_UPDATED_AT, getCurrentTimestamp());

        int result = db.update(TABLE_WISHLIST,
                values,
                COLUMN_WISHLIST_ID + "=?",
                new String[]{String.valueOf(wishlistId)});
        db.close();
        return result > 0;
    }
}
