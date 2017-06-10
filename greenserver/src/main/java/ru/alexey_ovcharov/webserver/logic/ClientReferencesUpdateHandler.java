package ru.alexey_ovcharov.webserver.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.alexey_ovcharov.webserver.common.util.LoggerFactory;
import ru.alexey_ovcharov.webserver.persist.Images;
import ru.alexey_ovcharov.webserver.persist.Places;

/**
 *
 * @author Admin
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ClientReferencesUpdateHandler {

    private final Logger logger = LoggerFactory.createConsoleLogger(getClass().getSimpleName());

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    private EntityManager entityManager;

    public String getThingsJSON() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNotesJSON() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPlacesJSON(int offset, int limit, String address) throws Exception {
        logger.debug("Получил запрос на возврат мест");
        offset = Math.max(0, offset);
        limit = Math.max(1, limit);
        TypedQuery<Places> query;
        if (StringUtils.isBlank(address)) {
            query = entityManager.createNamedQuery("Places.findAll", Places.class);
        } else {
            query = entityManager.createQuery("SELECT p FROM Places p "
                    + "WHERE p.address LIKE :addr", Places.class);
            query.setParameter("addr", "%" + address + "%");
        }
        query.setMaxResults(limit);
        query.setFirstResult(offset);
        List<Places> resultList = query.getResultList();
        JSONObject result = new JSONObject();
        JSONArray placesJSONArray = new JSONArray();
        for (Places place : resultList) {
            JSONObject placeJSONObject = place.toJSON();
            placesJSONArray.put(placeJSONObject);
        }
        result.put("places", placesJSONArray);
        logger.debug("Места выбраны");
        return result.toString();
    }

    public String getImagesJSON(String... guids) throws JSONException {
        logger.debug("Получил запрос на возврат изображений");
        if (guids != null && guids.length <= 10) {
            List<UUID> uuids = new ArrayList<>(guids.length);
            for (String guid : guids) {
                uuids.add(UUID.fromString(guid));
            }
            TypedQuery<Images> query = entityManager.createQuery("SELECT i FROM "
                    + "Images i WHERE i.guid IN :guids", Images.class);
            query.setParameter("guids", uuids);
            List<Images> images = query.getResultList();
            JSONArray result = new JSONArray();
            for (Images image : images) {
                JSONObject jsonObject = image.toJSON();
                result.put(jsonObject);
            }
            return result.toString();
        }
        logger.debug("Изображения выбраны");
        return "[]";
    }

}
