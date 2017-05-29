package ru.alexey_ovcharov.greenguide.mobile.persist;

import java.lang.annotation.Documented;

/**
 * Created by Алексей on 26.04.2017.
 */
@Documented
public @interface Entity {

    public static final String GUID_COLUMN_NAME = "guid";
}
