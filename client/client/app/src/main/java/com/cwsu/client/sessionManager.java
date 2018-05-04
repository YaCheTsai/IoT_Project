package com.cwsu.client;

import android.content.Context;
import android.content.SharedPreferences;


class sessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static String userIDTag = "userName";
    private static String tokenTag = "token";
    private static String adminTag = "admin";
    private static String roleTag = "role";
//    private static String Tag = "role";

    sessionManager(Context context){
        int PRIVATE_MODE = 0;
        String prefName = "LoginSession";
        pref = context.getSharedPreferences(prefName, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }


    void setUserID(String name){

        editor.putString(userIDTag, name);
        editor.commit();
    }
    void setToken(String token) {
        editor.putString(tokenTag, token);
        editor.commit();
    }
    void setAdmin(String isAdmin) {
        editor.putString(adminTag, isAdmin);
        editor.commit();
    }
    void setRole(String roleID) {
        editor.putString(roleTag, roleID);
        editor.commit();
    }

    String getUserID(){
        return pref.getString(userIDTag, "not exist");
    }
    String getToken(){return pref.getString(tokenTag, "not avaliable");}
    String getAdmin(){return pref.getString(adminTag, "not avaliable");}
    String getRole(){return pref.getString(roleTag, "not avaliable");}
}
