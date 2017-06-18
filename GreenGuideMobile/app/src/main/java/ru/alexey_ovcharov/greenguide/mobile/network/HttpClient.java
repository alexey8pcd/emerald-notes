package ru.alexey_ovcharov.greenguide.mobile.network;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

/**
 * Created by Admin on 04.06.2017.
 */

public class HttpClient implements Closeable {
    public static final int TIMEOUT = 25000;
    private String urlToSend;
    private List<HttpURLConnection> openedConnections = new CopyOnWriteArrayList<>();

    public HttpClient(String urlToSend) {
        this.urlToSend = urlToSend;
    }

    public InteractStatus sendBinaryData(@NonNull DataPackage dataConvert) {
        try {
            URL url = new URL(urlToSend);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Android(" + APP_NAME + ")");
            for (Map.Entry<String, String> header : dataConvert.getHeaders()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("charset", "utf-8");
            conn.setDoOutput(true);
            Log.d(APP_NAME, "Отправляю запрос на сервер");
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(dataConvert.getBinary());
            outputStream.flush();
            int responseCode = conn.getResponseCode();
            Log.d(APP_NAME, "Http код ответа: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                openedConnections.add(conn);
                return InteractStatus.SUCCESS;
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return InteractStatus.CLIENT_ERROR;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                return InteractStatus.CORRUPT_DATA;
            } else {
                return InteractStatus.SERVER_ERROR;
            }
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return InteractStatus.UNKNOWN;
        }
    }

    public InteractStatus sendJSON(String json) {
        try {
            URL url = new URL(urlToSend);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Android(" + APP_NAME + ")");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setDoOutput(true);
            Log.d(APP_NAME, "Отправляю запрос на сервер: " + json);
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(json.getBytes("utf-8"));
            outputStream.flush();
            int responseCode = conn.getResponseCode();
            Log.d(APP_NAME, "Http код ответа: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                openedConnections.add(conn);
                return InteractStatus.SUCCESS;
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return InteractStatus.CLIENT_ERROR;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                return InteractStatus.CORRUPT_DATA;
            } else {
                return InteractStatus.SERVER_ERROR;
            }
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return InteractStatus.UNKNOWN;
        }
    }

    @NonNull
    public Response receiveData(Pair<String, String>... params) {
        try {
            URL url = new URL(urlToSend);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Android(" + APP_NAME + ")");
            conn.setDoInput(true);
            if (params != null) {
                conn.setDoOutput(true);
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
                    writer.write(getPostDataString(params));
                    writer.flush();
                }
            }
            Log.d(APP_NAME, "Отправляю запрос на получение данных");
            int responseCode = conn.getResponseCode();
            Log.d(APP_NAME, "Http код ответа: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                openedConnections.add(conn);
                Response response;
                String data = Commons.readStreamToString(conn.getInputStream(), "utf-8");
                response = new Response(InteractStatus.SUCCESS);
                Log.d(APP_NAME, "Получил данные длины: " + data.length());
                response.setData(data);
                return response;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                return new Response(InteractStatus.CORRUPT_DATA);
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return new Response(InteractStatus.CLIENT_ERROR);
            } else {
                return new Response(InteractStatus.SERVER_ERROR);
            }
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return new Response(InteractStatus.UNKNOWN);
        }
    }

    private String getPostDataString(Pair<String, String>... params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Pair<String, String> pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    @Override
    public void close() {
        for (HttpURLConnection connection : openedConnections) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                Log.e(APP_NAME, e.toString(), e);
            }
        }
    }
}
