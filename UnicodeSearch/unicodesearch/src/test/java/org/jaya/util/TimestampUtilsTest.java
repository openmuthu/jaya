package org.jaya.util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TimestampUtils}.
 */
public class TimestampUtilsTest {

    // tu01 — ISO8601 round-trip: format then parse returns the same epoch millis
    @Test
    public void tu01_iso8601RoundTrip() {
        Date original = new Date(1_500_000_000_000L); // 2017-07-14
        String formatted = TimestampUtils.getISO8601StringForDate(original);
        Date parsed = TimestampUtils.getDateFromISO8601String(formatted);
        assertEquals("tu01: round-trip must preserve epoch millis", original.getTime(), parsed.getTime());
    }

    // tu02 — getDateFromISO8601String handles null → epoch 0
    @Test
    public void tu02_parseNull_returnsEpoch() {
        Date d = TimestampUtils.getDateFromISO8601String(null);
        assertEquals("tu02: null input must return epoch 0", 0L, d.getTime());
    }

    // tu03 — getDateFromISO8601String handles unparseable string → epoch 0
    @Test
    public void tu03_parseInvalid_returnsEpoch() {
        Date d = TimestampUtils.getDateFromISO8601String("not-a-date");
        assertEquals("tu03: unparseable string must return epoch 0", 0L, d.getTime());
    }

    // tu04 — getDateFromISO8601String accepts short format (no millis)
    @Test
    public void tu04_parseShortFormat() {
        Date d = TimestampUtils.getDateFromISO8601String("2020-01-01T00:00:00Z");
        assertTrue("tu04: short ISO8601 must parse to a non-epoch date", d.getTime() > 0);
    }

    // tu05 — diffInSeconds: d1 > d2 is positive
    @Test
    public void tu05_diffInSeconds_positive() {
        Date d1 = new Date(10_000L);
        Date d2 = new Date(0L);
        assertEquals("tu05: diff must be 10", 10L, TimestampUtils.diffInSeconds(d1, d2));
    }

    // tu06 — diffInSeconds: d1 < d2 is negative
    @Test
    public void tu06_diffInSeconds_negative() {
        Date d1 = new Date(0L);
        Date d2 = new Date(5_000L);
        assertEquals("tu06: diff must be -5", -5L, TimestampUtils.diffInSeconds(d1, d2));
    }

    // tu07 — getHumanReadableElapsedTimeFromNow: recent past contains "seconds ago"
    @Test
    public void tu07_humanReadable_recentPast_containsSecondsAgo() {
        Date past = new Date(System.currentTimeMillis() - 30_000); // 30 s ago
        String result = TimestampUtils.getHumanReadableElapsedTimeFromNow(past);
        assertTrue("tu07: recent past must mention 'seconds ago'", result.contains("seconds ago"));
    }

    // tu08 — getHumanReadableElapsedTimeFromNow: a date 2 hours ago contains "hours ago"
    @Test
    public void tu08_humanReadable_hoursAgo() {
        Date past = new Date(System.currentTimeMillis() - 2 * TimestampUtils.SECONDS_IN_HOUR * 1000L);
        String result = TimestampUtils.getHumanReadableElapsedTimeFromNow(past);
        assertTrue("tu08: 2h ago must say 'hours ago'", result.contains("hours ago"));
    }

    // tu09 — getHumanReadableElapsedTimeFromNow: 3 days ago contains "days ago"
    @Test
    public void tu09_humanReadable_daysAgo() {
        Date past = new Date(System.currentTimeMillis() - 3L * TimestampUtils.SECONDS_IN_DAY * 1000L);
        String result = TimestampUtils.getHumanReadableElapsedTimeFromNow(past);
        assertTrue("tu09: 3 days ago must say 'day'", result.contains("day"));
    }

    // tu10 — getHumanReadableElapsedTimeFromNow: future date contains "ahead"
    @Test
    public void tu10_humanReadable_futureContainsAhead() {
        Date future = new Date(System.currentTimeMillis() + 120_000); // 2 minutes ahead
        String result = TimestampUtils.getHumanReadableElapsedTimeFromNow(future);
        assertTrue("tu10: future date must contain 'ahead'", result.contains("ahead"));
    }

    // tu11 — nowAsString returns a parseable ISO8601 string
    @Test
    public void tu11_nowAsString_isParseable() {
        String now = TimestampUtils.nowAsString();
        assertNotNull("tu11a: nowAsString must not be null", now);
        Date parsed = TimestampUtils.getDateFromISO8601String(now);
        assertTrue("tu11b: nowAsString must parse to a recent date", parsed.getTime() > 0);
    }
}
