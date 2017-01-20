package com.rationaleemotions.utils;

/**
 *
 */
public class Preconditions {

    public static void checkArgument(boolean condition, String errorMsg) {
        if (!condition) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static void checkState(boolean condition, String errorMsg) {
        if (!condition) {
            throw new IllegalStateException((errorMsg));
        }
    }
}
