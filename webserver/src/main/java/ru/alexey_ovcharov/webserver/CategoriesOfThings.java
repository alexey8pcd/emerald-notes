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
@Table(name = "categories_of_things")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CategoriesOfThings.findAll", query = "SELECT c FROM CategoriesOfThings c")
    , @NamedQuery(name = "CategoriesOfThings.findByIdCategory", query = "SELECT c FROM CategoriesOfThings c WHERE c.idCategory = :idCategory")
    , @NamedQuery(name = "CategoriesOfThings.findByCategory", query = "SELECT c FROM CategoriesOfThings c WHERE c.category = :category")})
public class CategoriesOfThings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_category")
    private Integer idCategory;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "category")
    private String category;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCategory", fetch = FetchType.LAZY)
    private Collection<Things> thingsCollection;

    public CategoriesOfThings() {
    }

    public CategoriesOfThings(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public CategoriesOfThings(Integer idCategory, String category) {
        this.idCategory = idCategory;
        this.category = category;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        hash += (idCategory != null ? idCategory.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CategoriesOfThings)) {
            return false;
        }
        CategoriesOfThings other = (CategoriesOfThings) object;
        if ((this.idCategory == null && other.idCategory != null) || (this.idCategory != null && !this.idCategory.equals(other.idCategory))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.CategoriesOfThings[ idCategory=" + idCategory + " ]";
    }

}
