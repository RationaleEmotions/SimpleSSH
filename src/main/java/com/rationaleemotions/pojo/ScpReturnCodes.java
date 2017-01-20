package com.rationaleemotions.pojo;

/**
 * REFERENCE : http://support.attachmate.com/techdocs/2116.html
 */
public enum ScpReturnCodes {
    UNKNOWN(- 1, ""),
    SUCCESS(0, ""),
    GENERAL_ERROR_IN_FILE_COPY(1, "General error in file copy "),
    DESTINATION_NOT_DIR(2, "Destination is not directory, but it should be "),
    MAX_SYMLINK_LIMIT_EXCEEDED(3, "Maximum symlink level exceeded "),
    CONNECTION_TO_HOST_FAILED(4, "Connecting to host failed"),
    CONNECTION_BROKEN(5, "Connection broken"),
    FILE_DOESNT_EXIST(6, "File does not exist "),
    NO_PERMISSION(7, "No permission to access file"),
    SFTP_PROTOCOL_ERROR(8, "General error in sftp protocol"),
    FTP_MISMATCH(9, "File transfer protocol mismatch"),
    NO_RESULT(10, "No file matches a given criteria"),
    CONNECTION_DENIED(65, "Host not allowed to connect"),
    SSH_PROTOCOL_ERROR(66, "General error in ssh protocol"),
    KEY_XCHANGE_FAILED(67, "Key exchange failed"),
    RESERVED(68, "Reserved"),
    MAC_ERROR(69, "MAC error"),
    COMPRESSION_ERROR(70, "Compression error"),
    SERVICE_UNAVAILABLE(71, "Service not available"),
    UNSUPPORTED_PROTOCOL_VERSION(72, "Protocol version not supported"),
    HOSTKEY_UNVERIFIABLE(73, "Host key not verifiable"),
    CONNECTION_FAILED(74, "Connection failed"),
    DISCONNECTED_BY_APPLN(75, "Disconnected by application"),
    TOO_MANY_CONNECTIONS(76, "Too many connections"),
    AUTH_CANCELLED_BY_USER(77, "Authentication cancelled by user"),
    AUTH_METHODS_EXHAUSTED(78, "No more authentication methods available"),
    INVALID_USER(79, "Invalid user name");

    ScpReturnCodes(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    private int code;
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ScpReturnCodes parse(int code) {
        ScpReturnCodes returnCode = UNKNOWN;
        for (ScpReturnCodes each : values()) {
            if (each.code == code) {
                return each;
            }
        }
        return returnCode;
    }
}
