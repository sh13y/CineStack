package com.example.cinestack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * SessionManager - Manages user login sessions using SharedPreferences
 * Keeps users logged in across app restarts
 * 
 * @author ICT3214 Group Project
 * @version 1.0
 */
public class SessionManager {

    // SharedPreferences file name
    private static final String PREF_NAME = "CineStackSession";
    
    // SharedPreferences keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";

    // SharedPreferences instance
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    /**
     * Constructor
     * @param context Application context
     */
    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Create login session
     * Stores user information in SharedPreferences
     * 
     * @param username User's username
     * @param fullName User's full name
     * @param email User's email
     */
    public void createLoginSession(String username, String fullName, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.commit(); // Use commit() instead of apply() to ensure data is saved immediately
    }

    /**
     * Check if user is logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged in user's username
     * @return Username or null if not logged in
     */
    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    /**
     * Get logged in user's full name
     * @return Full name or null if not logged in
     */
    public String getFullName() {
        return preferences.getString(KEY_FULL_NAME, null);
    }

    /**
     * Get logged in user's email
     * @return Email or null if not logged in
     */
    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    /**
     * Check login status and redirect to LoginActivity if not logged in
     * Call this method in activities that require authentication
     */
    public void checkLogin() {
        if (!isLoggedIn()) {
            // User is not logged in, redirect to LoginActivity
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Logout user
     * Clears all session data and redirects to LoginActivity
     */
    public void logoutUser() {
        // Clear all session data
        editor.clear();
        editor.commit();

        // Redirect to LoginActivity
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Clear all session data without redirecting
     * Useful when you want to logout but handle navigation separately
     */
    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}
