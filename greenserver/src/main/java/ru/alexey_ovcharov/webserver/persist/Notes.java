package ru.alexey_ovcharov.webserver.persist;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
@author Alexey
*/
@Entity
@Table(name = "notes")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Notes.findAll", query = "SELECT n FROM Notes n")
    , @NamedQuery(name = "Notes.findByIdNote", query = "SELECT n FROM Notes n WHERE n.idNote = :idNote")
    , @NamedQuery(name = "Notes.findByNoteText", query = "SELECT n FROM Notes n WHERE n.noteText = :noteText")})
public class Notes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_note")
    private Integer idNote;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "note_text")
    private String noteText;
    @JoinColumn(name = "id_note_type", referencedColumnName = "id_note_type")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private NoteTypes idNoteType;

    public Notes() {
    }

    public Notes(Integer idNote) {
        this.idNote = idNote;
    }

    public Notes(Integer idNote, String noteText) {
        this.idNote = idNote;
        this.noteText = noteText;
    }

    public Integer getIdNote() {
        return idNote;
    }

    public void setIdNote(Integer idNote) {
        this.idNote = idNote;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public NoteTypes getIdNoteType() {
        return idNoteType;
    }

    public void setIdNoteType(NoteTypes idNoteType) {
        this.idNoteType = idNoteType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idNote != null ? idNote.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Notes)) {
            return false;
        }
        Notes other = (Notes) object;
        if ((this.idNote == null && other.idNote != null) || (this.idNote != null && !this.idNote.equals(other.idNote))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.Notes[ idNote=" + idNote + " ]";
    }

}
