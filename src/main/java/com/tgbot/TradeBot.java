package com.tgbot;

import com.futures.Amount;
import com.futures.Coin;
import com.futures.GlobalVariables;
import com.log.TradeLogger;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

public class TradeBot extends AbilityBot {
    private static final String SUCCESS_STR = "✅ Success";
    private static final String ERROR_STR = "❌ Something went wrong...";

    private final long creatorId;

    public TradeBot(String botToken, String botUsername, long creatorId) {
        super(botToken, botUsername);
        this.creatorId = creatorId;
        TradeLogger.silentSender = silent;
        TradeLogger.chatId = creatorId;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability addCoin() {
        return Ability.builder()
                .name("addcoin")
                .info("/addcoin SYMBOL AMOUNT ({amount}$ or {amount}%) LEVERAGE")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(3)
                .action(ctx -> {
                    Amount amount = null;
                    int leverage = 1;
                    boolean isOk = true;

                    try {
                        amount = new Amount(ctx.secondArg());
                        leverage = Integer.parseInt(ctx.thirdArg());
                    } catch (IllegalArgumentException illegalArgumentException) {
                        isOk = false;
                    }

                    if (isOk) {
                        GlobalVariables.enabledCoins.put(ctx.firstArg(), new Coin(ctx.firstArg(), amount, leverage));
                    }

                    silent.send(isOk ? SUCCESS_STR : ERROR_STR, ctx.chatId());
                })
                .build();
    }

    public Ability getCoin() {
        return Ability.builder()
                .name("getcoin")
                .info("/getcoin SYMBOL")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> {
                    Coin coin = GlobalVariables.enabledCoins.get(ctx.firstArg());

                    silent.send(coin != null ? coin.toString() : ERROR_STR, ctx.chatId());
                })
                .build();
    }

    public Ability getCoins() {
        return Ability.builder()
                .name("getcoins")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> silent.send(GlobalVariables.enabledCoins.toString(), ctx.chatId()))
                .build();
    }

    public Ability removeCoin() {
        return Ability.builder()
                .name("rmcoin")
                .info("/rmcoin SYMBOL")
                .privacy(Privacy.CREATOR)
                .locality(Locality.USER)
                .input(1)
                .action(ctx -> {
                    silent.send(GlobalVariables.enabledCoins.remove(ctx.firstArg()) != null ? SUCCESS_STR : ERROR_STR, ctx.chatId());
                })
                .build();
    }
}
