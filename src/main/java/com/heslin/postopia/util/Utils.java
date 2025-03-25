package com.heslin.postopia.util;

public class Utils {
    public static <T extends Enum<T>> T getEnumByOrdinal(Class<T> enumClass, int ordinal) {
        T[] values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid ordinal for " + enumClass.getSimpleName());
        }
        return values[ordinal];
    }
}
