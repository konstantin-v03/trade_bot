package com.log;

import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import org.telegram.abilitybots.api.sender.SilentSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Bidi;
import java.text.SimpleDateFormat;
import java.util.*;

public class TradeLogger {
    private static final String LOG_ORDER_DIR = "orders/";
    private static final String EXCEPTION_FILE_NAME = "exception.txt";

    public static SilentSender silentSender = null;
    public static Long chatId = null;

    static {
        if (!new File(LOG_ORDER_DIR).mkdir()) {
            System.err.println("Orders directory was now created!");
        }
    }

    public static void logOpenOrder(Position position) {
        try {
            logFile(getLogOrderPath(position.getSymbol()), String.format("%s OPEN %s %s$",
                    getFormatDate(),
                    position.getPositionSide(),
                    position.getEntryPrice()));
            logTgBot(String.format("%s %s OPEN %s %s$", position.getPositionSide().equals("LONG") ? "\uD83D\uDCC8" : "\uD83D\uDCC9",
                    position.getSymbol(),
                    position.getPositionSide(),
                    position.getEntryPrice()));
        } catch (IOException ignored) {

        }
    }

    public static void logCloseOrder(MyTrade myTrade) {
        try {
            logFile(getLogOrderPath(myTrade.getSymbol()), String.format("%s CLOSE %s %s$ %s$",
                    getFormatDate(),
                    myTrade.getPositionSide(),
                    myTrade.getPrice(),
                    myTrade.getRealizedPnl()));
            logTgBot(String.format("❌ %s CLOSE %s %s$ \nProfit: %s$", myTrade.getSymbol(), myTrade.getPositionSide(), myTrade.getPrice(), myTrade.getRealizedPnl()));
        } catch (IOException ignored) {

        }
    }

    public static void logException(Exception exception) {
        try {
            logFile(EXCEPTION_FILE_NAME,
                    getFormatDate() + " " + exception);
            logTgBot(String.format("⛔ Error occured: \"%s\"", exception));
        } catch (IOException ignored) {

        }
    }

    public static File getExceptionFile() {
        return new File(EXCEPTION_FILE_NAME);
    }

    public static File getOrderLogFile(String symbol) {
        return new File(getLogOrderPath(symbol));
    }

    public static void logFile(String fileName, String log) throws IOException {
        File file = new File(fileName);

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException();
            }
        }

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(log).append("\n");
        fileWriter.close();
    }

    public static void logTgBot(String log) {
        if (silentSender != null && chatId != null) {
            silentSender.send(log, chatId);
        }
    }

    public static BigDecimal getTotalProfit(String symbol) {
        List<List<String>> lines;
        BigDecimal totalProfit = new BigDecimal(BigInteger.ZERO);

        try {
            Scanner scanner = new Scanner(TradeLogger.getOrderLogFile(symbol));

            lines = new ArrayList<>();

            while (scanner.hasNextLine()) {
                lines.add(new ArrayList<>(Arrays.asList(scanner.nextLine().split(" "))));
            }

            scanner.close();
        } catch (IOException ioException) {
            return null;
        }

        String word;

        try {
            for (List<String> line : lines) {
                if (line.size() > 5 && line.get(2).equals("CLOSE")) {
                    word = line.get(5);
                    totalProfit = totalProfit.add(new BigDecimal(word.substring(0, word.length() - 1)));
                }
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }

        return totalProfit;
    }

    private static String getLogOrderPath(String symbol) {
        return LOG_ORDER_DIR + symbol + ".txt";
    }

    private static String getFormatDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }
}
