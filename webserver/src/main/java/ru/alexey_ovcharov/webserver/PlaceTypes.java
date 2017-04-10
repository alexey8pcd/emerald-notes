package ru.alexey_ovcharov.webserver;

import java.io.Serializable;
import java.util.Collection;
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
@Table(name = "place_types")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PlaceTypes.findAll", query = "SELECT p FROM PlaceTypes p")
    , @NamedQuery(name = "PlaceTypes.findByIdPlaceType", query = "SELECT p FROM PlaceTypes p WHERE p.idPlaceType = :idPlaceType")
    , @NamedQuery(name = "PlaceTypes.findByType", query = "SELECT p FROM PlaceTypes p WHERE p.type = :type")})
public class PlaceTypes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_place_type")
    private Integer idPlaceType;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40)
    @Column(name = "type")
    private String type;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idPlaceType", fetch = FetchType.LAZY)
    private Collection<Places> placesCollection;

    public PlaceTypes() {
    }

    public PlaceTypes(Integer idPlaceType) {
        this.idPlaceType = idPlaceType;
    }

    public PlaceTypes(Integer idPlaceType, String type) {
        this.idPlaceType = idPlaceType;
        this.type = type;
    }

    public Integer getIdPlaceType() {
        return idPlaceType;
    }

    public void setIdPlaceType(Integer idPlaceType) {
        this.idPlaceType = idPlaceType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlTransient
    public Collection<Places> getPlacesCollection() {
        return placesCollection;
    }

    public void setPlacesCollection(Collection<Places> placesCollection) {
        this.placesCollection = placesCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPlaceType != null ? idPlaceType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlaceTypes)) {
            return false;
        }
        PlaceTypes other = (PlaceTypes) object;
        if ((this.idPlaceType == null && other.idPlaceType != null) || (this.idPlaceType != null && !this.idPlaceType.equals(other.idPlaceType))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.PlaceTypes[ idPlaceType=" + idPlaceType + " ]";
    }

}
