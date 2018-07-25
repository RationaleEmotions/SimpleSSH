package com.rationaleemotions.utils;

public final class Strings {

    private Strings() {
        //Defeat instantiation
    }

    /**
     * @param text - The text that is to be tested.
     * @return - <code>true</code> if the text is neither null nor empty (after trimming)
     */
    public static boolean isNotNullAndNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * @param text - The text that is to be tested.
     * @return - <code>true</code> if the text is either null or empty (after trimming)
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}
