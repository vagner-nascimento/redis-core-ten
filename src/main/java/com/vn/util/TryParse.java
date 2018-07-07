package com.vn.util;

import org.jetbrains.annotations.Nullable;

public final class TryParse {
    /**
     * Try to parse string to a Long value.
     *
     * @param value
     * @return If success, return the parsed value, else, returns null.
     */
    @Nullable
    public static Long toLong(String value) {
        Long parsedValue = null;
        try {
            parsedValue = Long.parseLong(value);
        } catch (Exception e) {
        }

        return parsedValue;
    }

    /**
     * Try to parse string to a Double value.
     *
     * @param value
     * @return If success, return the parsed value, else, returns null.
     */
    @Nullable
    public static Double toDouble(String value) {
        Double parsedValue = null;
        try {
            parsedValue = Double.parseDouble(value);
        } catch (Exception e) {
        }

        return parsedValue;
    }

    /**
     * Try to parse string to a Double value.
     *
     * @param value
     * @return If success, return the parsed value, else, returns null.
     */
    @Nullable
    public static Integer toInteger(String value) {
        Integer parsedValue = null;
        try {
            parsedValue = Integer.parseInt(value);
        } catch (Exception e) {
        }

        return parsedValue;
    }
}
