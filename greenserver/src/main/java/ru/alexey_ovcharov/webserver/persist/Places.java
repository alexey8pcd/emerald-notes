package ru.alexey_ovcharov.webserver.persist;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
@author Alexey
*/
@Entity
@Table(name = "places")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Places.findAll", query = "SELECT p FROM Places p")
    , @NamedQuery(name = "Places.findByIdPlace", query = "SELECT p FROM Places p WHERE p.idPlace = :idPlace")
    , @NamedQuery(name = "Places.findByDescription", query = "SELECT p FROM Places p WHERE p.description = :description")
    , @NamedQuery(name = "Places.findByLatitude", query = "SELECT p FROM Places p WHERE p.latitude = :latitude")
    , @NamedQuery(name = "Places.findByLongitude", query = "SELECT p FROM Places p WHERE p.longitude = :longitude")
    , @NamedQuery(name = "Places.findByAddress", query = "SELECT p FROM Places p WHERE p.address = :address")
    , @NamedQuery(name = "Places.findByDateCreate", query = "SELECT p FROM Places p WHERE p.dateCreate = :dateCreate")})
public class Places implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_place")
    private Integer idPlace;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "latitude")
    private BigDecimal latitude;
    @Column(name = "longitude")
    private BigDecimal longitude;
    @Size(max = 100)
    @Column(name = "address")
    private String address;
    @Column(name = "date_create")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreate;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idPlace", fetch = FetchType.LAZY)
    private Collection<ImagesForPlace> imagesForPlaceCollection;
    @JoinColumn(name = "id_country", referencedColumnName = "id_country")
    @ManyToOne(fetch = FetchType.LAZY)
    private Countries idCountry;
    @JoinColumn(name = "id_place_type", referencedColumnName = "id_place_type")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PlaceTypes idPlaceType;

    public Places() {
    }

    public Places(Integer idPlace) {
        this.idPlace = idPlace;
    }

    public Integer getIdPlace() {
        return idPlace;
    }

    public void setIdPlace(Integer idPlace) {
        this.idPlace = idPlace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    @XmlTransient
    public Collection<ImagesForPlace> getImagesForPlaceCollection() {
        return imagesForPlaceCollection;
    }

    public void setImagesForPlaceCollection(Collection<ImagesForPlace> imagesForPlaceCollection) {
        this.imagesForPlaceCollection = imagesForPlaceCollection;
    }

    public Countries getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(Countries idCountry) {
        this.idCountry = idCountry;
    }

    public PlaceTypes getIdPlaceType() {
        return idPlaceType;
    }

    public void setIdPlaceType(PlaceTypes idPlaceType) {
        this.idPlaceType = idPlaceType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPlace != null ? idPlace.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Places)) {
            return false;
        }
        Places other = (Places) object;
        if ((this.idPlace == null && other.idPlace != null) || (this.idPlace != null && !this.idPlace.equals(other.idPlace))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.Places[ idPlace=" + idPlace + " ]";
    }

}
