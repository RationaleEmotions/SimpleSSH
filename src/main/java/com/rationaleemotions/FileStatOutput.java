package com.rationaleemotions;

/**
 *
 */
enum FileStatOutput {
    FILE,
    DIRECTORY,
    DOESNOT_EXIST,
    UNKNOWN;

    static FileStatOutput parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }
        if ("regular file".equalsIgnoreCase(text.trim())) {
            return FILE;
        }
        if ("directory".equalsIgnoreCase(text.trim())) {
            return DIRECTORY;
        }
        if (text.trim().toLowerCase().contains("no such file or directory")) {
            return DOESNOT_EXIST;
        }
        return UNKNOWN;
    }
}
