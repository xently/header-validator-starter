package co.ke.xently.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ElapsedTimeManagerTest {
    private final ElapsedTimeManager manager = new ElapsedTimeManager();

    @Test
    void shouldExposeHeaderConstants() {
        assertAll(
                () -> assertThat(ElapsedTimeManager.TIMESTAMP_HEADER)
                        .isEqualTo("X-TimeStamp"),
                () -> assertThat(ElapsedTimeManager.ELAPSED_TIME_HEADER)
                        .isEqualTo("X-ElapsedTime")
        );
    }

    @Nested
    class setElapsedTime {
        private ListAppender<ILoggingEvent> loggingEvents;

        @BeforeEach
        public void setup() {
            loggingEvents = new ListAppender<>();
            loggingEvents.start();
            Logger logger = (Logger) LoggerFactory.getLogger(ElapsedTimeManager.class);
            logger.addAppender(loggingEvents);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldSetElapsedTimeWhenHeaderMissingAndTimestampValid(String elapsedHeaderValue) {
            String timeStampSeconds = String.valueOf(System.currentTimeMillis() / 1000);
            var captured = new AtomicReference<String>();

            manager.setElapsedTime(timeStampSeconds, elapsedHeaderValue, captured::set);

            assertThat(captured.get())
                    .as("elapsed time should be set")
                    .isNotNull();
            long elapsed = Long.parseLong(captured.get());
            assertAll(
                    () -> assertThat(elapsed)
                            .isGreaterThanOrEqualTo(0L),
                    () -> assertThat(elapsed)
                            .isLessThan(60_000L) // sanity bound for test runtime
            );
        }

        @Test
        void shouldNotSetWhenElapsedTimeHeaderAlreadyPresent() {
            String timeStampSeconds = String.valueOf(System.currentTimeMillis() / 1000);
            var captured = new AtomicReference<String>();

            manager.setElapsedTime(timeStampSeconds, "123", captured::set);

            assertThat(captured.get())
                    .as("elapsed time should not be overwritten")
                    .isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotSetWhenTimestampIsNull(String elapsedHeaderValue) {
            var captured = new AtomicReference<String>();

            manager.setElapsedTime(null, elapsedHeaderValue, captured::set);

            assertThat(captured.get())
                    .as("no timestamp => no elapsed header")
                    .isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldSwallowErrorsAndNotThrowWhenTimestampIsInvalid(String elapsedTimeHeaderValue) {
            var captured = new AtomicReference<String>();

            assertDoesNotThrow(() -> manager.setElapsedTime("not-a-number", elapsedTimeHeaderValue, captured::set));

            var errorLogMessages = loggingEvents.list
                    .stream()
                    .filter(e -> e.getLevel().equals(Level.ERROR))
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();

            assertAll(
                    () -> assertThat(captured.get())
                            .isNull(),
                    () -> assertThat(errorLogMessages)
                            .containsOnly("""
                                    An error was encountered while setting elapsed time header.
                                    \tStart time: "not-a-number"
                                    \tElapsed time header value: "%s\"""".formatted(elapsedTimeHeaderValue))
            );
        }
    }
}
