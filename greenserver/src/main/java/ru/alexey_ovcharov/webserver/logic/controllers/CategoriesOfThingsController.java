package ru.alexey_ovcharov.webserver.logic.controllers;

import ru.alexey_ovcharov.webserver.logic.facades.CategoriesOfThingsFacade;
import ru.alexey_ovcharov.webserver.common.util.JsfUtil;
import ru.alexey_ovcharov.webserver.common.util.JsfUtil.PersistAction;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import ru.alexey_ovcharov.webserver.persist.CategoriesOfThings;
import ru.alexey_ovcharov.webserver.common.util.JsfUtil;

@Named("categoriesOfThingsController")
@SessionScoped
public class CategoriesOfThingsController implements Serializable {

    @EJB
    private ru.alexey_ovcharov.webserver.logic.facades.CategoriesOfThingsFacade ejbFacade;
    private List<CategoriesOfThings> items = null;
    private CategoriesOfThings selected;

    public CategoriesOfThingsController() {
    }

    public CategoriesOfThings getSelected() {
        return selected;
    }

    public void setSelected(CategoriesOfThings selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private CategoriesOfThingsFacade getFacade() {
        return ejbFacade;
    }

    public CategoriesOfThings prepareCreate() {
        selected = new CategoriesOfThings();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("CategoriesOfThingsCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("CategoriesOfThingsUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("CategoriesOfThingsDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<CategoriesOfThings> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public CategoriesOfThings getCategoriesOfThings(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<CategoriesOfThings> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<CategoriesOfThings> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = CategoriesOfThings.class)
    public static class CategoriesOfThingsControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CategoriesOfThingsController controller = (CategoriesOfThingsController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "categoriesOfThingsController");
            return controller.getCategoriesOfThings(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof CategoriesOfThings) {
                CategoriesOfThings o = (CategoriesOfThings) object;
                return getStringKey(o.getIdCategory());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), CategoriesOfThings.class.getName()});
                return null;
            }
        }

    }

}
