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
import javax.xml.bind.annotation.XmlRootElement;

/**
@author Alexey
*/
@Entity
@Table(name = "images_for_thing")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ImagesForThing.findAll", query = "SELECT i FROM ImagesForThing i")
    , @NamedQuery(name = "ImagesForThing.findByIdImageForThing", query = "SELECT i FROM ImagesForThing i WHERE i.idImageForThing = :idImageForThing")})
public class ImagesForThing implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_image_for_thing")
    private Long idImageForThing;
    @JoinColumn(name = "id_image", referencedColumnName = "id_image")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Images idImage;
    @JoinColumn(name = "id_thing", referencedColumnName = "id_thing")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Things idThing;

    public ImagesForThing() {
    }

    public ImagesForThing(Long idImageForThing) {
        this.idImageForThing = idImageForThing;
    }

    public Long getIdImageForThing() {
        return idImageForThing;
    }

    public void setIdImageForThing(Long idImageForThing) {
        this.idImageForThing = idImageForThing;
    }

    public Images getIdImage() {
        return idImage;
    }

    public void setIdImage(Images idImage) {
        this.idImage = idImage;
    }

    public Things getIdThing() {
        return idThing;
    }

    public void setIdThing(Things idThing) {
        this.idThing = idThing;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idImageForThing != null ? idImageForThing.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ImagesForThing)) {
            return false;
        }
        ImagesForThing other = (ImagesForThing) object;
        if ((this.idImageForThing == null && other.idImageForThing != null) || (this.idImageForThing != null && !this.idImageForThing.equals(other.idImageForThing))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.ImagesForThing[ idImageForThing=" + idImageForThing + " ]";
    }

}
