package ru.alexey_ovcharov.greenguide.mobile.persist;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

/**
 * Created by Алексей on 29.04.2017.
 */

@Entity
public class Note {
    public static final String TABLE_NAME = "notes";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String ID_NOTE_COLUMN = "id_note";
    public static final String NOTE_TEXT_COLUMN = "note_text";
    public static final String ID_NOTE_TYPE_COLUMN = "id_note_type";

    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME
            + " (" + ID_NOTE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            NOTE_TEXT_COLUMN + "TEXT NOT NULL, " +
            ID_NOTE_TYPE_COLUMN + " INTEGER NOT NULL " +
            "       REFERENCES note_types (id_note_type) ON DELETE RESTRICT ON UPDATE CASCADE, "
            + GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";
    private int idNote;
    private String noteText;
    private int idNoteType;
    private String guid;

    public int getIdNoteType() {
        return idNoteType;
    }

    public void setIdNoteType(int idNoteType) {
        this.idNoteType = idNoteType;
    }

    public Note() {
    }

    public int getIdNote() {
        return idNote;
    }

    public void setIdNote(int idNote) {
        this.idNote = idNote;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}
