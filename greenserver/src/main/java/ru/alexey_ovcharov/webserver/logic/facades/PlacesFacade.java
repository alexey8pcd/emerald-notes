package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import ru.alexey_ovcharov.webserver.persist.Places;

/**
@author Alexey
*/
@Stateless
public class PlacesFacade extends AbstractFacade<Places> {

    public PlacesFacade() {
        super(Places.class);
    }

}
