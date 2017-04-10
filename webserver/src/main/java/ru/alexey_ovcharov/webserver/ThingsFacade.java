package ru.alexey_ovcharov.webserver;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
@author Alexey
*/
@Stateless
public class ThingsFacade extends AbstractFacade<Things> {

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ThingsFacade() {
        super(Things.class);
    }

}
