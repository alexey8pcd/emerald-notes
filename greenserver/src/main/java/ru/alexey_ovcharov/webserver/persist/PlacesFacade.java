package ru.alexey_ovcharov.webserver.persist;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
@author Alexey
*/
@Stateless
public class PlacesFacade extends AbstractFacade<Places> {

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void edit(Places entity) {
        super.edit(entity); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    public PlacesFacade() {
        super(Places.class);
    }

}
