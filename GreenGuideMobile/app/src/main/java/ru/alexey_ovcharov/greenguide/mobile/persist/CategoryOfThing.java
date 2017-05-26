package ru.alexey_ovcharov.greenguide.mobile.persist;

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
            CATEGORY_COLUMN + " VARCHAR (50) NOT NULL UNIQUE)";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private int idCategory;
    private String category;

    public CategoryOfThing(int idCategory, String categoryName) {
        this.idCategory = idCategory;
        this.category = categoryName;
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

}
