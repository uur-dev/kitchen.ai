package com.br3akPoint.util;

public class UserContext {
    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> email = new ThreadLocal<>();
    private static final ThreadLocal<String> deviceType = new ThreadLocal<>();

    public static void setUserId(Long id) { userId.set(id); }
    public static Long getUserId() { return userId.get(); }

    public static void setEmail(String mail) { email.set(mail); }
    public static String getEmail() { return email.get(); }

    public static void setDeviceType(String type) { deviceType.set(type); }
    public static String getDeviceType() { return deviceType.get(); }

    public static void clear() {
        userId.remove();
        email.remove();
        deviceType.remove();
    }
}