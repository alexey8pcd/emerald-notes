package ru.alexey_ovcharov.webserver.persist;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.model.ByteArrayContent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author Alexey
 */
@Entity
@Table(name = "images")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Images.findAll", query = "SELECT i FROM Images i")
    , @NamedQuery(name = "Images.findByIdImage", query = "SELECT i FROM Images i WHERE i.idImage = :idImage")})
public class Images implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_image")
    private Integer idImage;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "image_data")
    private byte[] imageData;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idImage", fetch = FetchType.LAZY)
    private Collection<ImagesForThing> imagesForThingCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idImage", fetch = FetchType.LAZY)
    private Collection<ImagesForPlace> imagesForPlaceCollection;

    @Column(name = "guid")
    @NotNull
    private UUID guid;

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public Images(JSONObject imageJSON) throws JSONException {
        idImage = imageJSON.getInt("id_image");
        guid = UUID.fromString(imageJSON.getString("guid"));
        String decodedBytes = imageJSON.getString("binary_data");
        imageData = Base64.decodeBase64(decodedBytes);
    }

    public Images() {
    }

    public Images(Integer idImage) {
        this.idImage = idImage;
    }

    public Images(Integer idImage, byte[] imageData) {
        this.idImage = idImage;
        this.imageData = imageData;
    }

    public Integer getIdImage() {
        return idImage;
    }

    public void setIdImage(Integer idImage) {
        this.idImage = idImage;
    }

    public byte[] getImageData() {
        return imageData;
    }

//    public StreamedContent getStreamedContent() {
//        if (imageData != null) {
//            return new ByteArrayContent(imageData, "image/png");
//        } else {
//            return new ByteArrayContent(new byte[0]);
//        }
//    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @XmlTransient
    public Collection<ImagesForThing> getImagesForThingCollection() {
        return imagesForThingCollection;
    }

    public void setImagesForThingCollection(Collection<ImagesForThing> imagesForThingCollection) {
        this.imagesForThingCollection = imagesForThingCollection;
    }

    @XmlTransient
    public Collection<ImagesForPlace> getImagesForPlaceCollection() {
        return imagesForPlaceCollection;
    }

    public void setImagesForPlaceCollection(Collection<ImagesForPlace> imagesForPlaceCollection) {
        this.imagesForPlaceCollection = imagesForPlaceCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idImage != null ? idImage.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Images)) {
            return false;
        }
        Images other = (Images) object;
        if ((this.idImage == null && other.idImage != null) || (this.idImage != null && !this.idImage.equals(other.idImage))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.Images[ idImage=" + idImage + " ]";
    }

}
