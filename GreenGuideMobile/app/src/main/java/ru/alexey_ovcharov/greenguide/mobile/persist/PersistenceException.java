package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.util.Log;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

/**
 * Created by Алексей on 02.05.2017.
 */

public class PersistenceException extends Exception {

    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(String message) {
        super(message);
    }

    public void log() {
        Log.e(Commons.APP_NAME, toString(), this);
    }
}
