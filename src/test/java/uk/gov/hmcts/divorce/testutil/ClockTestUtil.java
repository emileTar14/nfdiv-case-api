package uk.gov.hmcts.divorce.testutil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.when;

public final class ClockTestUtil {

    public static final Instant NOW = Instant.now();
    public static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private ClockTestUtil() {
    }

    public static void setMockClock(final Clock clock) {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZONE_ID);
    }

    public static LocalDate getExpectedLocalDate() {
        return LocalDate.ofInstant(NOW, ZONE_ID);
    }

    public static LocalDateTime getExpectedLocalDateTime() {
        return LocalDateTime.ofInstant(NOW, ZONE_ID);
    }
}
