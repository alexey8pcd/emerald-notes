package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.database.Cursor;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

/**
 * Created by Алексей on 29.04.2017.
 */

@Entity
public class Thing {

    public static final String TABLE_NAME = "things";
    public static final String ID_THING_COLUMN = "id_thing";
    public static final String NAME_COLUMN = "name";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String DANGER_FOR_ENVIRONMENT_COLUMN = "danger_for_environment";
    public static final String DECOMPOSITION_TIME_COLUMN = "decomposition_time";
    public static final String ID_COUNTRY_COLUMN = "id_country";
    public static final String ID_CATEGORY_COLUMN = "id_category";

    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME +
            " (" + ID_THING_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            NAME_COLUMN + " VARCHAR (100) NOT NULL, " +
            DESCRIPTION_COLUMN + " TEXT NOT NULL, " +
            DANGER_FOR_ENVIRONMENT_COLUMN + " INTEGER NOT NULL, " +
            DECOMPOSITION_TIME_COLUMN + " INTEGER, " +
            ID_COUNTRY_COLUMN + " INTEGER REFERENCES countries (id_country) " +
            "   ON DELETE RESTRICT ON UPDATE CASCADE, id_category INTEGER NOT NULL " +
            "   REFERENCES categories_of_things (id_category) ON DELETE RESTRICT ON UPDATE CASCADE, " +
            Entity.GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";
    public static final String IMAGE_FOR_THING_TABLE_NAME = "images_for_thing";
    public static final String ID_IMAGE_FOR_THING_COLUMN = "id_image_for_thing";

    public static final String IMAGE_FOR_THING_DROP_SCRIPT = "DROP TABLE IF EXISTS "
            + IMAGE_FOR_THING_TABLE_NAME + ";";
    public static final String IMAGE_FOR_THING_CREATE_SCRIPT =
            "CREATE TABLE " + IMAGE_FOR_THING_TABLE_NAME + " (" +
                    ID_IMAGE_FOR_THING_COLUMN + " INTEGER PRIMARY KEY NOT NULL, " +
                    ID_THING_COLUMN + " INTEGER NOT NULL, " +
                    Image.ID_IMAGE_COLUMN + " INTEGER NOT NULL)";
    public static final String[] DANGER_LABELS = {
            "Нет данных",
            "Не опасно",
            "Малоопасно",
            "Умеренно опасно",
            "Опасно",
            "Очень опасно",
    };

    private int idThing;
    private String name;
    private String description;
    private int dangerForEnvironment;
    private Integer decompositionTime;
    private Integer idCountry;
    private int idCategory;
    private String guid;
    private Image image;

    public Thing() {
    }

    public Thing(Cursor cursor) {
        idThing = cursor.getInt(cursor.getColumnIndex(ID_THING_COLUMN));
        name = cursor.getString(cursor.getColumnIndex(NAME_COLUMN));
        description = cursor.getString(cursor.getColumnIndex(DESCRIPTION_COLUMN));
        dangerForEnvironment = cursor.getInt(cursor.getColumnIndex(DANGER_FOR_ENVIRONMENT_COLUMN));
        decompositionTime = cursor.getInt(cursor.getColumnIndex(DECOMPOSITION_TIME_COLUMN));
        idCountry = cursor.getInt(cursor.getColumnIndex(ID_COUNTRY_COLUMN));
        idCategory = cursor.getInt(cursor.getColumnIndex(ID_CATEGORY_COLUMN));
        guid = cursor.getString(cursor.getColumnIndex(GUID_COLUMN_NAME));
    }

    public Image getImage() {
        return image;
    }

    public Thing(String name, String description, int dangerForEnvironment, Integer decompositionTimeMonth) {
        this.name = name;
        this.description = description;
        this.dangerForEnvironment = dangerForEnvironment;
        this.decompositionTime = decompositionTimeMonth;
    }

    public String getGuid() {
        return guid;
    }

    public int getIdThing() {
        return idThing;
    }

    public void setIdThing(int idThing) {
        this.idThing = idThing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDangerForEnvironment() {
        return dangerForEnvironment;
    }

    public void setDangerForEnvironment(int dangerForEnvironment) {
        this.dangerForEnvironment = dangerForEnvironment;
    }

    public Integer getDecompositionTime() {
        return decompositionTime;
    }

    public void setDecompositionTime(Integer decompositionTime) {
        this.decompositionTime = decompositionTime;
    }

    public Integer getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(Integer idCountry) {
        this.idCountry = idCountry;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
