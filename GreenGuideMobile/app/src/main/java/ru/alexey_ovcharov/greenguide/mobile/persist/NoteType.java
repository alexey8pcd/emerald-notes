package ru.alexey_ovcharov.greenguide.mobile.persist;

/**
 * Created by Алексей on 29.04.2017.
 */
@Entity
public class NoteType {

    public static final String TABLE_NAME = "note_types";
    public static final String ID_NOTE_TYPE_COLUMN = "id_note_type";
    public static final String NOTE_TYPE_COLUMN = "note_type";

    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME
            + " (" + ID_NOTE_TYPE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            NOTE_TYPE_COLUMN + "TEXT NOT NULL, " +
            NOTE_TYPE_COLUMN + " VARCHAR (100) NOT NULL)";

    private int idNoteType;
    private String noteType;

    public NoteType() {
    }

    public int getIdNoteType() {
        return idNoteType;
    }

    public void setIdNoteType(int idNoteType) {
        this.idNoteType = idNoteType;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }
}
