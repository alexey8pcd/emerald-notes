package ru.alexey_ovcharov.webserver;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
@author Alexey
*/
@Stateless
public class CategoriesOfThingsFacade extends AbstractFacade<CategoriesOfThings> {

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CategoriesOfThingsFacade() {
        super(CategoriesOfThings.class);
    }

}
