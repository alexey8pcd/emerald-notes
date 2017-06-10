package ru.alexey_ovcharov.webserver.servlets;

import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import ru.alexey_ovcharov.webserver.common.util.Nullable;

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

    public static String paramsToString(Map<String, String[]> parametersMap) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstKey = true;
        for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (!firstKey) {
                stringBuilder.append(",");
            } else {
                firstKey = false;
            }
            stringBuilder.append(key).append("=>").append("{");
            boolean firstValue = true;
            for (String string : value) {
                if (!firstValue) {
                    stringBuilder.append(",");
                } else {
                    firstValue = false;
                }
                stringBuilder.append(string);
            }
            stringBuilder.append("}");
        }

        return stringBuilder.toString();
    }

    @Nullable
    public static String getFirstParameter(Map<String, String[]> parametersMap, String name) {
        if (parametersMap == null || parametersMap.isEmpty()) {
            return null;
        } else {
            String[] strings = parametersMap.get(name);
            if (strings == null || strings.length == 0) {
                return null;
            } else {
                return strings[0];
            }
        }
    }

}
