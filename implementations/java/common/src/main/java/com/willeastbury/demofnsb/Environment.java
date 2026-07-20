package com.willeastbury.demofnsb;

public final class Environment {
    private Environment() {
    }

    public static String optional(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static String optional(String name) {
        return optional(name, null);
    }

    public static String required(String name) {
        String value = optional(name);
        if (value == null) {
            throw new IllegalStateException(
                "Required environment variable is missing: " + name);
        }
        return value;
    }

    public static boolean booleanValue(
        String name,
        boolean defaultValue) {

        String value = optional(name);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static int integerValue(String name, int defaultValue) {
        String value = optional(name);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                name + " must be an integer",
                exception);
        }
    }
}
