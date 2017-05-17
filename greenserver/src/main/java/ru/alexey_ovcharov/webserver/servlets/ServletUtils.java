package ru.alexey_ovcharov.webserver.servlets;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Alexey
 */
public class ServletUtils {

    public static String headersToString(HttpServletRequest httpServletRequest) {
        StringBuilder builder = new StringBuilder();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        boolean firstHeader = true;
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!firstHeader) {
                builder.append(", ");
            } else {
                firstHeader = false;
            }
            builder.append("{").append(headerName).append(", values=[");

            Enumeration<String> headers = httpServletRequest.getHeaders(headerName);
            boolean firstValue = true;
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                if (!firstValue) {
                    builder.append(", ");
                } else {
                    firstValue = false;
                }
                builder.append(headerValue);
            }
            builder.append("]}");
        }
        return builder.toString();
    }

}
