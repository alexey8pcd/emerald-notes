package ru.alexey_ovcharov.webserver.persist;

import ru.alexey_ovcharov.webserver.persist.JsfUtil;
import ru.alexey_ovcharov.webserver.persist.JsfUtil.PersistAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.primefaces.model.UploadedFile;

@Named("placeTypesController")
@SessionScoped
public class PlaceTypesController implements Serializable {

    @EJB
    private ru.alexey_ovcharov.webserver.persist.PlaceTypesFacade ejbFacade;
    private List<PlaceTypes> items = null;
    private PlaceTypes selected;

    public PlaceTypesController() {
    }

    public PlaceTypes getSelected() {
        return selected;
    }

    public void setSelected(PlaceTypes selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private PlaceTypesFacade getFacade() {
        return ejbFacade;
    }

    public PlaceTypes prepareCreate() {
        selected = new PlaceTypes();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("PlaceTypesCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("PlaceTypesUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("PlaceTypesDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<PlaceTypes> getItems() {
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

    public PlaceTypes getPlaceTypes(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<PlaceTypes> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<PlaceTypes> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public List<String> getTypesAsString() {
        List<PlaceTypes> placeTypeses = getFacade().findAll();
        List<String> list = new ArrayList<>(placeTypeses.size());
        for (PlaceTypes placeTypese : placeTypeses) {
            list.add(placeTypese.getType());
        }
        return list;
    }

    @FacesConverter(forClass = PlaceTypes.class)
    public static class PlaceTypesControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PlaceTypesController controller = (PlaceTypesController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "placeTypesController");
            return controller.getPlaceTypes(getKey(value));
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
            if (object instanceof PlaceTypes) {
                PlaceTypes o = (PlaceTypes) object;
                return getStringKey(o.getIdPlaceType());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), PlaceTypes.class.getName()});
                return null;
            }
        }

    }

}
