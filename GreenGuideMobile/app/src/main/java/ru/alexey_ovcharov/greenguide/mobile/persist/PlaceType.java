package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

/**
 * Created by Алексей on 23.04.2017.
 */
@Entity
public class PlaceType {

    public static final String TABLE_NAME = "place_types";
    public static final String ID_PLACE_TYPE_COLUMN = "id_place_type";
    public static final String TYPE_COLUMN = "type";

    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME +
            "(" + ID_PLACE_TYPE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            TYPE_COLUMN + " VARCHAR (40) NOT NULL, "
            + GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";

    private int idPlaceType;
    private String type;
    private String guid;

    public PlaceType(Cursor cursor) {
        idPlaceType = cursor.getInt(cursor.getColumnIndex(ID_PLACE_TYPE_COLUMN));
        type = cursor.getString(cursor.getColumnIndex(TYPE_COLUMN));
        guid = cursor.getString(cursor.getColumnIndex(Entity.GUID_COLUMN_NAME));
    }

    public String getGuid() {
        return guid;
    }

    public int getIdPlaceType() {
        return idPlaceType;
    }

    public void setIdPlaceType(int idPlaceType) {

        this.idPlaceType = idPlaceType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ID_PLACE_TYPE_COLUMN, idPlaceType);
        jsonObject.put(TYPE_COLUMN, type);
        jsonObject.put(Entity.GUID_COLUMN_NAME, guid);
        return jsonObject;
    }
}
