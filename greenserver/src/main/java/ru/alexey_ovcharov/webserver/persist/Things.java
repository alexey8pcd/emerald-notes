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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "things")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Things.findAll", query = "SELECT t FROM Things t")
    , @NamedQuery(name = "Things.findByIdThing", query = "SELECT t FROM Things t WHERE t.idThing = :idThing")
    , @NamedQuery(name = "Things.findByName", query = "SELECT t FROM Things t WHERE t.name = :name")
    , @NamedQuery(name = "Things.findByDescription", query = "SELECT t FROM Things t WHERE t.description = :description")
    , @NamedQuery(name = "Things.findByIdDangerForEnvironment", query = "SELECT t FROM Things t WHERE t.idDangerForEnvironment = :idDangerForEnvironment")
    , @NamedQuery(name = "Things.findByDecompositionTime", query = "SELECT t FROM Things t WHERE t.decompositionTime = :decompositionTime")})
public class Things implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_thing")
    private Integer idThing;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_danger_for_environment")
    private int idDangerForEnvironment;
    @Column(name = "decomposition_time")
    private Integer decompositionTime;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idThing", fetch = FetchType.LAZY)
    private Collection<ImagesForThing> imagesForThingCollection;
    @JoinColumn(name = "id_category", referencedColumnName = "id_category")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CategoriesOfThings idCategory;
    @JoinColumn(name = "id_country", referencedColumnName = "id_country")
    @ManyToOne(fetch = FetchType.LAZY)
    private Countries idCountry;

    @Column(name = "guid")
    @NotNull
    private UUID guid;

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }
    
    public Things() {
    }

    public Things(Integer idThing) {
        this.idThing = idThing;
    }

    public Things(Integer idThing, String name, String description, int idDangerForEnvironment) {
        this.idThing = idThing;
        this.name = name;
        this.description = description;
        this.idDangerForEnvironment = idDangerForEnvironment;
    }

    public Integer getIdThing() {
        return idThing;
    }

    public void setIdThing(Integer idThing) {
        this.idThing = idThing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIdDangerForEnvironment() {
        return idDangerForEnvironment;
    }

    public void setIdDangerForEnvironment(int idDangerForEnvironment) {
        this.idDangerForEnvironment = idDangerForEnvironment;
    }

    public Integer getDecompositionTime() {
        return decompositionTime;
    }

    public void setDecompositionTime(Integer decompositionTime) {
        this.decompositionTime = decompositionTime;
    }

    @XmlTransient
    public Collection<ImagesForThing> getImagesForThingCollection() {
        return imagesForThingCollection;
    }

    public void setImagesForThingCollection(Collection<ImagesForThing> imagesForThingCollection) {
        this.imagesForThingCollection = imagesForThingCollection;
    }

    public CategoriesOfThings getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(CategoriesOfThings idCategory) {
        this.idCategory = idCategory;
    }

    public Countries getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(Countries idCountry) {
        this.idCountry = idCountry;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idThing != null ? idThing.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Things)) {
            return false;
        }
        Things other = (Things) object;
        if ((this.idThing == null && other.idThing != null) || (this.idThing != null && !this.idThing.equals(other.idThing))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.Things[ idThing=" + idThing + " ]";
    }

}
