package com.utils;

import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private final ScheduledExecutorService scheduledExecutorService;

    private Scheduler(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public static Scheduler scheduleEveryDayAtFixedTime(Runnable command, int hour, int minute, int second) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(second);

        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).getSeconds();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);

        return new Scheduler(scheduledExecutorService);
    }

    public static Scheduler scheduleEveryMinute(Runnable command) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(command , 0, 1, TimeUnit.MINUTES);

        return new Scheduler(scheduledExecutorService);
    }

    public void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
