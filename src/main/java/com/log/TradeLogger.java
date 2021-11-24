package com.log;

import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import org.telegram.abilitybots.api.sender.SilentSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TradeLogger {
    public static SilentSender silentSender = null;
    public static Long chatId = null;

    public static void logOpenOrder(Position position) {
        try {
            logFile(position.getSymbol() + ".txt", String.format("%s OPEN %s %s$",
                    getFormatDate(),
                    position.getPositionSide(),
                    position.getEntryPrice()));
            logTgBot(String.format("%s %s OPEN %s %s$", position.getPositionSide().equals("LONG") ? "\uD83D\uDCC8" : "\uD83D\uDCC9", position.getSymbol(), position.getPositionSide(), position.getEntryPrice()));
        } catch (IOException ignored) {

        }
    }

    public static void logCloseOrder(MyTrade myTrade) {
        try {
            logFile(myTrade.getSymbol() + ".txt", String.format("%s CLOSE %s %s$ %s$",
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
            logFile("exceptions.txt",
                    getFormatDate() + " " + exception);
            logTgBot(String.format("⛔ Error occured: \"%s\"", exception));
        } catch (IOException ignored) {

        }
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

    private static String getFormatDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }
}
