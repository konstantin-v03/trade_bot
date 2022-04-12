package com.utils;

import java.io.*;
import java.util.*;

import com.strategies.Strategy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

public class Utils {
    public static void appendStrToFile(String fileName, String str) throws IOException{
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
        out.write(str);
        out.close();
    }

    public static List<String> getLogFileNames(Strategy strategy, String ticker) {
        List<String> logFileNames = new ArrayList<>();
        logFileNames.add(strategy + "_" + ticker + ".txt");
        return logFileNames;
    }

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

    public static Properties readPropertiesFile(String fileName) {
        Properties properties = null;

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException ignored) {

        }

        return properties;
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

    public static int getCandlestickIndex(Date date, int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return (calendar.get(Calendar.HOUR_OF_DAY) *
                DateUtils.MINUTES_IN_HOUR +
                calendar.get(Calendar.MINUTE)) / interval;
    }

    public static int intervalToInt(String intervalStr) {
        try {
            return Integer.parseInt(intervalStr);
        } catch (IllegalArgumentException illegalArgumentException) {
            if (intervalStr.equalsIgnoreCase("D")) {
                return 24 * 60;
            } else if (intervalStr.equalsIgnoreCase("12h")) {
                return 12 * 60;
            } else if (intervalStr.equalsIgnoreCase("4h")) {
                return 4 * 60;
            } else if (intervalStr.equalsIgnoreCase("h")) {
                return 60;
            }
        }

        return -1;
    }

    public static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {

        }
    }
}
