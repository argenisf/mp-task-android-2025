package com.example.mptaskandroid2025;
import android.content.Context;
import android.content.SharedPreferences;
public class LocalStorageHandler {

    private static final String PREF_NAME = "MPToDoTaskStorage";
    private final SharedPreferences sharedPreferences;

    public LocalStorageHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retrieves a string value from local storage.
     * @param key The key under which the value is stored.
     * @return The stored string, or null if not found.
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    /**
     * Stores a string value into local storage.
     * @param key The key under which the value will be stored.
     * @param value The string value to store.
     */
    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply(); // apply() writes asynchronously, commit() is synchronous
    }


}//end of class
