package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import ru.alexey_ovcharov.webserver.persist.CategoriesOfThings;

/**
@author Alexey
*/
@Stateless
public class CategoriesOfThingsFacade extends AbstractFacade<CategoriesOfThings> {

    public CategoriesOfThingsFacade() {
        super(CategoriesOfThings.class);
    }

}
