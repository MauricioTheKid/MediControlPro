package com.example.medicontrolpro.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class AuthRepository {
    private static final String PREFS_NAME = "MediControlAuth";
    private static final String KEY_USERS = "registered_users";
    private static final String KEY_LOGGED_IN_USER = "logged_in_user";

    private SharedPreferences sharedPreferences;

    public AuthRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean registerUser(User user) {
        Map<String, User> users = getRegisteredUsers();

        // Verificar si el usuario ya existe
        if (users.containsKey(user.getUsername())) {
            return false;
        }

        users.put(user.getUsername(), user);
        saveUsers(users);
        return true;
    }

    public User loginUser(String username, String password) {
        Map<String, User> users = getRegisteredUsers();
        User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            // Guardar usuario logueado
            saveLoggedInUser(user);
            return user;
        }
        return null;
    }

    public User getLoggedInUser() {
        String userJson = sharedPreferences.getString(KEY_LOGGED_IN_USER, null);
        if (userJson != null) {
            return userFromJson(userJson);
        }
        return null;
    }

    public void logout() {
        sharedPreferences.edit().remove(KEY_LOGGED_IN_USER).apply();
    }

    public boolean isUserLoggedIn() {
        return getLoggedInUser() != null;
    }

    private Map<String, User> getRegisteredUsers() {
        String usersJson = sharedPreferences.getString(KEY_USERS, "{}");
        return usersFromJson(usersJson);
    }

    private void saveUsers(Map<String, User> users) {
        String usersJson = usersToJson(users);
        sharedPreferences.edit().putString(KEY_USERS, usersJson).apply();
    }

    private void saveLoggedInUser(User user) {
        String userJson = userToJson(user);
        sharedPreferences.edit().putString(KEY_LOGGED_IN_USER, userJson).apply();
    }

    private String userToJson(User user) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", user.getUsername());
            jsonObject.put("password", user.getPassword());
            jsonObject.put("firstName", user.getFirstName());
            jsonObject.put("lastName", user.getLastName());
            jsonObject.put("dui", user.getDui());
            jsonObject.put("email", user.getEmail());
            jsonObject.put("phoneNumber", user.getPhoneNumber());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private User userFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            User user = new User();
            user.setUsername(jsonObject.getString("username"));
            user.setPassword(jsonObject.getString("password"));
            user.setFirstName(jsonObject.getString("firstName"));
            user.setLastName(jsonObject.getString("lastName"));
            user.setDui(jsonObject.getString("dui"));
            user.setEmail(jsonObject.getString("email"));
            user.setPhoneNumber(jsonObject.getString("phoneNumber"));
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String usersToJson(Map<String, User> users) {
        try {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, User> entry : users.entrySet()) {
                jsonObject.put(entry.getKey(), userToJson(entry.getValue()));
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private Map<String, User> usersFromJson(String json) {
        Map<String, User> users = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String userJson = jsonObject.getString(key);
                users.put(key, userFromJson(userJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return users;
    }
}