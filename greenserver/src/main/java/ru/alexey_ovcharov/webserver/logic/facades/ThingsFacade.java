package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import ru.alexey_ovcharov.webserver.persist.Things;

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
