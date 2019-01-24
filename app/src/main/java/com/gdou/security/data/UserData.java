package com.gdou.security.Application;

public class UserData{

    private static long id;

    private static String username;

    public static long getId() {
        return id;
    }

    public static void setId(long id) {
        id = id;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        username = username;
    }
}
