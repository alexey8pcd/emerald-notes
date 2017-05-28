package ru.alexey_ovcharov.webserver.persist;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
@author Alexey
*/
@Entity
@Table(name = "note_types")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "NoteTypes.findAll", query = "SELECT n FROM NoteTypes n")
    , @NamedQuery(name = "NoteTypes.findByIdNoteType", query = "SELECT n FROM NoteTypes n WHERE n.idNoteType = :idNoteType")
    , @NamedQuery(name = "NoteTypes.findByNoteType", query = "SELECT n FROM NoteTypes n WHERE n.noteType = :noteType")})
public class NoteTypes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_note_type")
    private Integer idNoteType;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "note_type")
    private String noteType;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idNoteType", fetch = FetchType.LAZY)
    private Collection<Notes> notesCollection;

    @Column(name = "guid")
    @NotNull
    private UUID guid;

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }
    
    public NoteTypes() {
    }

    public NoteTypes(Integer idNoteType) {
        this.idNoteType = idNoteType;
    }

    public NoteTypes(Integer idNoteType, String noteType) {
        this.idNoteType = idNoteType;
        this.noteType = noteType;
    }

    public Integer getIdNoteType() {
        return idNoteType;
    }

    public void setIdNoteType(Integer idNoteType) {
        this.idNoteType = idNoteType;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    @XmlTransient
    public Collection<Notes> getNotesCollection() {
        return notesCollection;
    }

    public void setNotesCollection(Collection<Notes> notesCollection) {
        this.notesCollection = notesCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idNoteType != null ? idNoteType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NoteTypes)) {
            return false;
        }
        NoteTypes other = (NoteTypes) object;
        if ((this.idNoteType == null && other.idNoteType != null) || (this.idNoteType != null && !this.idNoteType.equals(other.idNoteType))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.NoteTypes[ idNoteType=" + idNoteType + " ]";
    }

}
