package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import ru.alexey_ovcharov.webserver.persist.PlaceTypes;

/**
@author Alexey
*/
@Stateless
public class PlaceTypesFacade extends AbstractFacade<PlaceTypes> {

    public PlaceTypesFacade() {
        super(PlaceTypes.class);
    }

}
