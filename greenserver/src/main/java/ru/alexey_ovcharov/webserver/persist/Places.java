package ru.alexey_ovcharov.webserver.persist;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.model.ByteArrayContent;
import org.primefaces.model.StreamedContent;
import ru.alexey_ovcharov.webserver.common.util.Constants;
import ru.alexey_ovcharov.webserver.common.util.LangUtil;
import ru.alexey_ovcharov.webserver.common.util.Nullable;

/**
 * @author Alexey
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

    @Column(name = "guid")
    @NotNull
    private UUID guid;

    public Places(JSONObject placeInfoJSON) throws JSONException, ParseException {
        guid = UUID.fromString(placeInfoJSON.getString("guid"));
        description = placeInfoJSON.getString("description");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_YYYY_MM_DD);
        simpleDateFormat.setLenient(false);
        dateCreate = simpleDateFormat.parse(placeInfoJSON.getString("date_create"));
        if (placeInfoJSON.has("address")) {
            address = placeInfoJSON.getString("address");
        }
        latitude = LangUtil.toDecimalOrNullIfEmpty(placeInfoJSON.optString("latitude"));
        longitude = LangUtil.toDecimalOrNullIfEmpty(placeInfoJSON.optString("longitude"));
    }

    @NotNull
    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("description", description);
        result.put("guid", guid.toString());
        if (address != null) {
            result.put("address", address);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_YYYY_MM_DD);
        result.put("date_create", simpleDateFormat.format(dateCreate));
        if (latitude != null) {
            result.put("latitude", latitude.doubleValue());
        }
        if (longitude != null) {
            result.put("longitude", longitude.doubleValue());
        }
        result.put("type", idPlaceType.getType());
        JSONArray imagesIdsArray = new JSONArray();
        for (ImagesForPlace imagesForPlace : imagesForPlaceCollection) {
            imagesIdsArray.put(imagesForPlace.getIdImage().getGuid().toString());
        }
        result.put("images", imagesIdsArray);
        if (idCountry != null) {
            result.put("country", idCountry.getCountry());
        }
        return result;
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

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
