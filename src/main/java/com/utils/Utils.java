package com.utils;

import java.io.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

public class Utils {
    public static String readAllFromInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            return "";
        }

        return stringBuilder.toString();
    }

    public static void answerOkToHttpsRequest(HttpExchange httpExchange) {
        HttpsExchange httpsExchange = (HttpsExchange) httpExchange;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpsExchange.getResponseBody()))) {
            String response = "OK";
            httpsExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            httpsExchange.sendResponseHeaders(200, response.getBytes().length);
            writer.write(response);
        } catch (IOException ignored) {

        }
    }
}