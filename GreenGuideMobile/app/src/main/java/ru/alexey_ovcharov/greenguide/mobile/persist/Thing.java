package ru.alexey_ovcharov.greenguide.mobile.persist;

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
    public static final String IMAGE_COLUMN = "image";

    public static final String IMAGE_FOR_THING_DROP_SCRIPT = "DROP TABLE IF EXISTS "
            + IMAGE_FOR_THING_TABLE_NAME + ";";
    public static final String IMAGE_FOR_THING_CREATE_SCRIPT =
            "CREATE TABLE " + IMAGE_FOR_THING_TABLE_NAME + " (" +
                    ID_IMAGE_FOR_THING_COLUMN + " INTEGER NOT NULL, " +
                    ID_THING_COLUMN + " INTEGER NOT NULL REFERENCES things (id_thing) " +
                    "           ON DELETE RESTRICT ON UPDATE CASCADE, " +
                    IMAGE_COLUMN + " VARCHAR NOT NULL)";


    private int idThing;
    private String name;
    private String description;
    private int dangerForEnvironment;
    private int decompositionTime;
    private int idCountry;
    private int idCategory;
    private String guid;

    public Thing() {
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

    public int getDecompositionTime() {
        return decompositionTime;
    }

    public void setDecompositionTime(int decompositionTime) {
        this.decompositionTime = decompositionTime;
    }

    public int getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(int idCountry) {
        this.idCountry = idCountry;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

}
