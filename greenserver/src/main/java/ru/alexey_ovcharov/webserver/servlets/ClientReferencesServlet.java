package ru.alexey_ovcharov.webserver.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import ru.alexey_ovcharov.webserver.logic.ClientReferencesSaver;
import ru.alexey_ovcharov.webserver.common.util.LoggerFactory;
import ru.alexey_ovcharov.webserver.logic.ClientReferencesUpdateHandler;

/**
 *
 * @author Алексей
 */
public class ClientReferencesServlet extends HttpServlet {

    private static final long MAX_REQUEST_LENGTH = 4_000_000_000L;//~500мб
    private static final String GET_TYPE = "/get";
    private static final String THINGS_DATA = "/things_data";
    private static final String PLACES_DATA = "/places_data";
    private static final String IMAGES_DATA = "/images_data";
    private static final String SEND_TYPE = "/send";
    private static final String GUID_PARAM = "guid";
    @EJB
    private ClientReferencesSaver receiveHandler;
    @EJB
    private ClientReferencesUpdateHandler updateHandler;

    private final Logger logger = LoggerFactory.createConsoleLogger(ClientReferencesServlet.class.getSimpleName());
    private static final String DATA_SIZE = "data-size";
    private static final String CRC_32 = "crc32";
    private static final String IMAGE_GUID = "image-guid";
    private static final String CRITERIA = "criteria";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        String requestURI = httpServletRequest.getRequestURI();
        logger.info("Принял GET запрос от " + httpServletRequest.getRemoteHost()
                + ", URL: " + requestURI);
        Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();
        logger.info("Параметры: " + ServletUtils.paramsToString(parametersMap));
        if (requestURI.contains(GET_TYPE)) {
            try {
                returnData(requestURI, parametersMap, httpServletResponse);
            } catch (Exception e) {
                logger.error(e, e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            try (PrintWriter out = httpServletResponse.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet ClientReferencesReceiver</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Servlet ClientReferencesReceiver at "
                        + httpServletRequest.getContextPath()
                        + ", please use POST for public client data </h1>");
                out.println("</body>");
                out.println("</html>");
            }
            httpServletResponse.setContentType("text/html;charset=UTF-8");
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        long contentLength = httpServletRequest.getContentLengthLong();
        logger.info("Принял POST запрос от " + httpServletRequest.getRemoteHost()
                + ", длина содержимого: " + contentLength);
        logger.info("Заголовки: " + ServletUtils.headersToString(httpServletRequest));
        Map<String, String[]> parametersMap = httpServletRequest.getParameterMap();
        logger.info("Параметры: " + ServletUtils.paramsToString(parametersMap));
        if (contentLength <= MAX_REQUEST_LENGTH) {
            try {
                String requestURI = httpServletRequest.getRequestURI();
                if (requestURI.contains(SEND_TYPE)) {
                    logger.info("Сохраняю данные");
                    saveData(httpServletRequest, httpServletResponse);
                } else if (requestURI.contains(GET_TYPE)) {
                    logger.info("Передаю данные");
                    returnData(requestURI, parametersMap, httpServletResponse);
                }
            } catch (Exception e) {
                logger.error(e, e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Превышено ограничение на длину запроса");
        }

    }

    private void returnData(String requestURI, Map<String, String[]> parametersMap,
            HttpServletResponse httpServletResponse) throws Exception {
        int indexOf = requestURI.lastIndexOf("/");
        String offsetParam = ServletUtils.getFirstParameter(parametersMap, OFFSET);
        int offset = NumberUtils.toInt(offsetParam, 0);

        String limitParam = ServletUtils.getFirstParameter(parametersMap, LIMIT);
        int limit = NumberUtils.toInt(limitParam, Integer.MAX_VALUE);

        String criteriaParam = ServletUtils.getFirstParameter(parametersMap, CRITERIA);

        String command = requestURI.substring(indexOf);
        String response;
        switch (command) {
            case PLACES_DATA:
                //для мест критерием является город
                response = updateHandler.getPlacesJSON(offset, limit, criteriaParam);
                break;
            case THINGS_DATA:
                response = updateHandler.getThingsJSON(offset, limit, criteriaParam);
                break;
            case IMAGES_DATA:
                response = updateHandler.getImagesJSON(parametersMap.get(GUID_PARAM));
                break;
            default:
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
        }
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(response);
    }
    

    private void saveData(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {
        String characterEncoding = StringUtils.defaultString(httpServletRequest.getHeader("charset"), "UTF-8");
        logger.info("Кодировка: " + characterEncoding);
        String requestURI = httpServletRequest.getRequestURI();
        int indexOf = requestURI.indexOf(SEND_TYPE);
        String command = requestURI.substring(indexOf + SEND_TYPE.length());
        String imageGuid = httpServletRequest.getHeader(IMAGE_GUID);
        if (IMAGES_DATA.equals(command) && StringUtils.isNotBlank(imageGuid) && checkUUID(imageGuid)) {
            saveImages(httpServletRequest, imageGuid, httpServletResponse);
        } else {
            saveReferences(httpServletRequest, characterEncoding, 
                    command, httpServletResponse);
        }

    }

    private void saveReferences(HttpServletRequest httpServletRequest, 
            String characterEncoding, String command, 
            HttpServletResponse httpServletResponse) throws Exception {
        String request = getResponseBody(httpServletRequest, characterEncoding);
        logger.info("Тело запроса: " + request);
        JSONObject jSONObject = new JSONObject();
        if (StringUtils.isNotBlank(request) && request.startsWith("{")) {
            jSONObject = new JSONObject(request);
        }
        switch (command) {
            case PLACES_DATA:
                logger.info("Получил запрос на сохранение мест");
                receiveHandler.savePlaces(jSONObject);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                break;
            case THINGS_DATA:
                logger.info("Получил запрос на сохранение объектов энциклопедии");
                receiveHandler.saveThings(jSONObject);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void saveImages(HttpServletRequest httpServletRequest,
            String imageGuid, HttpServletResponse httpServletResponse) throws Exception {
        logger.info("Получил запрос на сохранение изображения");
        String imageSize = httpServletRequest.getHeader(DATA_SIZE);
        int size = Integer.parseInt(imageSize);
        String imageCrc = httpServletRequest.getHeader(CRC_32);
        long inputImageChecksum = Long.parseLong(imageCrc);
        byte[] imageData = IOUtils.toByteArray(httpServletRequest.getInputStream());
        if (imageData.length == size) {
            CRC32 crC32 = new CRC32();
            crC32.update(imageData);
            long calculatedChecksum = crC32.getValue();
            if (calculatedChecksum == inputImageChecksum) {
                receiveHandler.saveImage(imageData, imageGuid);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                        "Data length invalid");
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                    "Checksums not equals");
        }
    }

    private String getResponseBody(HttpServletRequest httpServletRequest, 
            String characterEncoding) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(httpServletRequest.getInputStream(), characterEncoding))) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s);
            }
        }
        String request = sb.toString();
        return request;
    }

    @Override
    public String getServletInfo() {
        return "ClientReferencesReceiver for public client data";
    }

    private boolean checkUUID(String imageHeader) {
        UUID.fromString(imageHeader);
        return true;
    }

}
