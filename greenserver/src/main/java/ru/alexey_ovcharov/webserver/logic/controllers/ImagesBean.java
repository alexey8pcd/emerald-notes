package ru.alexey_ovcharov.webserver.logic.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.io.IOUtils;
import ru.alexey_ovcharov.webserver.persist.Images;
import ru.alexey_ovcharov.webserver.persist.ImagesForPlace;
import ru.alexey_ovcharov.webserver.persist.ImagesForThing;
import ru.alexey_ovcharov.webserver.persist.Places;
import ru.alexey_ovcharov.webserver.persist.Things;

/**
 *
 * @author Admin
 */
@Named(value = "imagesBean")
@SessionScoped
public class ImagesBean implements Serializable {

    private static final String IMG_DIR = "C:/Storage/emerald-notes/greenserver/src/main/webapp/imgs";

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    protected EntityManager em;

    /**
     * Creates a new instance of ImagesBean
     */
    public ImagesBean() {
    }

    public String getImageForThing(Things thing) {
        if (thing != null) {
            Things things = em.find(Things.class, thing.getIdThing());
            Collection<ImagesForThing> imagesForPlaceCollection = things.getImagesForThingCollection();
            if (imagesForPlaceCollection.size() > 0) {
                ImagesForThing imagesForThing = imagesForPlaceCollection.iterator().next();
                Images idImage = imagesForThing.getIdImage();
                Images finded = em.find(Images.class, idImage.getIdImage());
                try {
                    File file = new File(IMG_DIR + File.separator
                            + idImage.getGuid().toString() + ".png");
                    file.deleteOnExit();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        IOUtils.write(finded.getImageData(), fileOutputStream);
                    }
                    return idImage.getGuid().toString() + ".png";
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        return "";
    }
    
    public String getImageForPlace(Places places) {
        if (places != null) {
            Places placesF = em.find(Places.class, places.getIdPlace());
            Collection<ImagesForPlace> imagesForPlaceCollection = placesF.getImagesForPlaceCollection();
            if (imagesForPlaceCollection.size() > 0) {
                ImagesForPlace imagesForPlace = imagesForPlaceCollection.iterator().next();
                Images idImage = imagesForPlace.getIdImage();
                Images finded = em.find(Images.class, idImage.getIdImage());
                try {
                    File file = new File(IMG_DIR + File.separator
                            + idImage.getGuid().toString() + ".png");
                    file.deleteOnExit();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        IOUtils.write(finded.getImageData(), fileOutputStream);
                    }
                    return idImage.getGuid().toString() + ".png";
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        return "";
    }

}
