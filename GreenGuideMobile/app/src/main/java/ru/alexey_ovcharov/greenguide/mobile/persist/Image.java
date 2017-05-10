package ru.alexey_ovcharov.greenguide.mobile.persist;

/**
 * Created by Алексей on 05.05.2017.
 */

public class Image {
    public static final String TABLE_NAME = "images";
    public static final String ID_IMAGE_COLUMN = "id_image";
    public static final String BINARY_DATA_COLUMN = "binary_data";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME + " ("
            + ID_IMAGE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            BINARY_DATA_COLUMN + " BLOB NOT NULL UNIQUE);";

    private int idImage;
    private byte[] binaryData;

    public int getIdImage() {
        return idImage;
    }

    public void setIdImage(int idImage) {
        this.idImage = idImage;
    }

    public byte[] getBinaryData(){
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData){
        this.binaryData = binaryData;
    }
}
