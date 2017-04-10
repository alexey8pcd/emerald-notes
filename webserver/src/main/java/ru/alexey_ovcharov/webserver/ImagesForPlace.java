package ru.alexey_ovcharov.webserver;

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
@Table(name = "images_for_place")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ImagesForPlace.findAll", query = "SELECT i FROM ImagesForPlace i")
    , @NamedQuery(name = "ImagesForPlace.findByIdImageForPlace", query = "SELECT i FROM ImagesForPlace i WHERE i.idImageForPlace = :idImageForPlace")})
public class ImagesForPlace implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_image_for_place")
    private Long idImageForPlace;
    @JoinColumn(name = "id_image", referencedColumnName = "id_image")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Images idImage;
    @JoinColumn(name = "id_place", referencedColumnName = "id_place")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Places idPlace;

    public ImagesForPlace() {
    }

    public ImagesForPlace(Long idImageForPlace) {
        this.idImageForPlace = idImageForPlace;
    }

    public Long getIdImageForPlace() {
        return idImageForPlace;
    }

    public void setIdImageForPlace(Long idImageForPlace) {
        this.idImageForPlace = idImageForPlace;
    }

    public Images getIdImage() {
        return idImage;
    }

    public void setIdImage(Images idImage) {
        this.idImage = idImage;
    }

    public Places getIdPlace() {
        return idPlace;
    }

    public void setIdPlace(Places idPlace) {
        this.idPlace = idPlace;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idImageForPlace != null ? idImageForPlace.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ImagesForPlace)) {
            return false;
        }
        ImagesForPlace other = (ImagesForPlace) object;
        if ((this.idImageForPlace == null && other.idImageForPlace != null) || (this.idImageForPlace != null && !this.idImageForPlace.equals(other.idImageForPlace))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.ImagesForPlace[ idImageForPlace=" + idImageForPlace + " ]";
    }

}
