package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import ru.alexey_ovcharov.webserver.persist.Countries;

/**
@author Alexey
*/
@Stateless
public class CountriesFacade extends AbstractFacade<Countries> {

    public CountriesFacade() {
        super(Countries.class);
    }

}
