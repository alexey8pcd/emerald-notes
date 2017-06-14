package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import ru.alexey_ovcharov.webserver.persist.Images;

/**
@author Alexey
*/
@Stateless
public class ImagesFacade extends AbstractFacade<Images> {

    public ImagesFacade() {
        super(Images.class);
    }

}
