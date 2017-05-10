package ru.alexey_ovcharov.webserver.persist;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
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
@Table(name = "countries")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Countries.findAll", query = "SELECT c FROM Countries c")
    , @NamedQuery(name = "Countries.findByIdCountry", query = "SELECT c FROM Countries c WHERE c.idCountry = :idCountry")
    , @NamedQuery(name = "Countries.findByCountry", query = "SELECT c FROM Countries c WHERE c.country = :country")})
public class Countries implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_country")
    private Integer idCountry;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "country")
    private String country;
    @OneToMany(mappedBy = "idCountry", fetch = FetchType.LAZY)
    private Collection<Places> placesCollection;
    @OneToMany(mappedBy = "idCountry", fetch = FetchType.LAZY)
    private Collection<Things> thingsCollection;

    public Countries() {
    }

    public Countries(Integer idCountry) {
        this.idCountry = idCountry;
    }

    public Countries(Integer idCountry, String country) {
        this.idCountry = idCountry;
        this.country = country;
    }

    public Integer getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(Integer idCountry) {
        this.idCountry = idCountry;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @XmlTransient
    public Collection<Places> getPlacesCollection() {
        return placesCollection;
    }

    public void setPlacesCollection(Collection<Places> placesCollection) {
        this.placesCollection = placesCollection;
    }

    @XmlTransient
    public Collection<Things> getThingsCollection() {
        return thingsCollection;
    }

    public void setThingsCollection(Collection<Things> thingsCollection) {
        this.thingsCollection = thingsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idCountry != null ? idCountry.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Countries)) {
            return false;
        }
        Countries other = (Countries) object;
        if ((this.idCountry == null && other.idCountry != null) || (this.idCountry != null && !this.idCountry.equals(other.idCountry))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.Countries[ idCountry=" + idCountry + " ]";
    }

}
