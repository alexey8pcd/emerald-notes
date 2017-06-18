package ru.alexey_ovcharov.webserver.logic.controllers;

import ru.alexey_ovcharov.webserver.logic.facades.ThingsFacade;
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
import ru.alexey_ovcharov.webserver.common.util.JsfUtil;
import ru.alexey_ovcharov.webserver.persist.Things;

@Named("thingsController")
@SessionScoped
public class ThingsController implements Serializable {

    @EJB
    private ru.alexey_ovcharov.webserver.logic.facades.ThingsFacade ejbFacade;
    private List<Things> items = null;
    private Things selected;
    public static final String[] DANGER_LABELS = {
        "Нет данных",
        "Полезно",
        "Не опасно",
        "Малоопасно",
        "Умеренно опасно",
        "Опасно",
        "Очень опасно",};

    public ThingsController() {
    }

    public Things getSelected() {
        return selected;
    }

    public void setSelected(Things selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ThingsFacade getFacade() {
        return ejbFacade;
    }

    public Things prepareCreate() {
        selected = new Things();
        initializeEmbeddableKey();
        return selected;
    }

    public String getDangerInfo(int idDanger) {
        return DANGER_LABELS[idDanger];
    }

    public String getDecompositionLabel(Integer decompositionTimeMonth) {
        String decompTimeStr = "";
        if (decompositionTimeMonth != null) {
            int years = decompositionTimeMonth / 12;
            int month = decompositionTimeMonth % 12;
            if (years != 0) {
                decompTimeStr += "Лет: " + years;
            }
            if (month != 0) {
                if (years != 0) {
                    decompTimeStr += ", ";
                }
                decompTimeStr += "Месяцев: " + month;
            }
        } else {
            decompTimeStr = "Нет данных";
        }
        return decompTimeStr;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("ThingsCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("ThingsUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("ThingsDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Things> getItems() {
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

    public Things getThings(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<Things> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Things> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Things.class)
    public static class ThingsControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ThingsController controller = (ThingsController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "thingsController");
            return controller.getThings(getKey(value));
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
            if (object instanceof Things) {
                Things o = (Things) object;
                return getStringKey(o.getIdThing());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Things.class.getName()});
                return null;
            }
        }

    }

}
