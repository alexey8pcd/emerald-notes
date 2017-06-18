package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

/**
 * Created by Алексей on 25.04.2017.
 */
@Entity
public class CategoryOfThing {

    public static final String TABLE_NAME = "categories_of_things";
    public static final String ID_CATEGORY_COLUMN = "id_category";
    public static final String CATEGORY_COLUMN = "category";
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME + " (" +
            ID_CATEGORY_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            CATEGORY_COLUMN + " VARCHAR (50) NOT NULL UNIQUE, " +
            GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private int idCategory;
    private String category;
    private String guid;

    public CategoryOfThing(Cursor cursor) {
        idCategory = cursor.getInt(cursor.getColumnIndex(CategoryOfThing.ID_CATEGORY_COLUMN));
        category = cursor.getString(cursor.getColumnIndex(CategoryOfThing.CATEGORY_COLUMN));
        guid = cursor.getString(cursor.getColumnIndex(Entity.GUID_COLUMN_NAME));
    }

    public String getGuid() {
        return guid;
    }

    public CategoryOfThing() {

    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @NonNull
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ID_CATEGORY_COLUMN, idCategory);
        jsonObject.put(CATEGORY_COLUMN, category);
        jsonObject.put(Entity.GUID_COLUMN_NAME, guid);
        return jsonObject;
    }
}
