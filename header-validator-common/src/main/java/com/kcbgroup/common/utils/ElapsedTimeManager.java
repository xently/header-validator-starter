package com.kcbgroup.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

@Slf4j
public class ElapsedTimeManager {
    public static final String TIMESTAMP_HEADER = "X-TimeStamp";
    public static final String ELAPSED_TIME_HEADER = "X-ElapsedTime";

    public void setElapsedTime(String timeStamp, String elapsedTimeHeaderValue, Consumer<String> elapsedTimeConsumer) {
        try {
            if (!StringUtils.hasText(elapsedTimeHeaderValue) && timeStamp != null) {
                var elapsedTime = System.currentTimeMillis() - Long.parseLong(timeStamp) * 1_000;
                elapsedTimeConsumer.accept(String.valueOf(elapsedTime));
            }
        } catch (Exception e) {
            log.error(
                    """
                            An error was encountered while setting elapsed time header.
                            \tStart time: "{}"
                            \tElapsed time header value: "{}\"""", timeStamp, elapsedTimeHeaderValue, e);
        }
    }
}
