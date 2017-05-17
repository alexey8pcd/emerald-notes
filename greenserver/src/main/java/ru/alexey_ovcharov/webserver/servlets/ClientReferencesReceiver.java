package ru.alexey_ovcharov.webserver.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import ru.alexey_ovcharov.webserver.logic.ClientReferencesHandler;
import ru.alexey_ovcharov.webserver.util.LoggerFactory;

/**
 *
 * @author Алексей
 */
public class ClientReferencesReceiver extends HttpServlet {

    private static final long MAX_REQUEST_LENGTH = 4_000_000_000L;//~500мб
    @EJB
    private ClientReferencesHandler handler;
    
    private Logger logger = LoggerFactory.createConsoleLogger(ClientReferencesReceiver.class.getSimpleName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.err.println("Принял GET запрос от " + request.getRemoteHost());
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ClientReferencesReceiver</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ClientReferencesReceiver at " + request.getContextPath() + ", please use POST public client data </h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        long contentLength = httpServletRequest.getContentLengthLong();
        logger.info("Принял POST запрос от " + httpServletRequest.getRemoteHost()
                + ", длина содержимого: " + contentLength);
        logger.info("Заголовки: " + ServletUtils.headersToString(httpServletRequest));
        if (contentLength <= MAX_REQUEST_LENGTH) {
            try {
                StringBuilder sb = new StringBuilder();
                String characterEncoding = StringUtils.defaultString(httpServletRequest.getHeader("charset"), "UTF-8");
                logger.info("Кодировка: " + characterEncoding);
                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(httpServletRequest.getInputStream(), characterEncoding))) {
                    String s;
                    while ((s = bufferedReader.readLine()) != null) {
                        sb.append(s);
                    }
                }
                String request = sb.toString();
                logger.info("Тело запроса: " + request);
                JSONObject jSONObject = new JSONObject(request);
                handler.handle(jSONObject);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                logger.error(e, e);
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }

        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Превышено ограничение на длину запроса");
        }

    }

    @Override
    public String getServletInfo() {
        return "ClientReferencesReceiver for public client data";
    }

}
