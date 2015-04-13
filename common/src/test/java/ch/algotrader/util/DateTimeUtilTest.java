package ch.algotrader.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeUtilTest {

    @Test
    public void testDateTimeFormattingExplicitZone() {

        Instant instant = DateTimeFormatter.ISO_DATE_TIME.parse("2011-12-03T10:15:30+01:00", Instant::from);
        Assert.assertEquals("2011-12-03 09:15:30 Z", DateTimeUtil.formatZoned(instant.atZone(ZoneOffset.UTC)));
        Assert.assertEquals("2011-12-03 11:15:30 +02:00", DateTimeUtil.formatZoned(instant.atZone(ZoneOffset.ofHours(2))));
    }

    @Test
    public void testDateTimeParsingExplicitZone() {

        Instant instant = DateTimeFormatter.ISO_DATE_TIME.parse("2011-12-03T10:15:30+01:00", Instant::from);
        Assert.assertEquals(instant.atZone(ZoneOffset.UTC), DateTimeUtil.parseZoned("2011-12-03 09:15:30 Z"));
        Assert.assertEquals(instant.atZone(ZoneOffset.ofHours(2)), DateTimeUtil.parseZoned("2011-12-03 11:15:30 +02:00"));
        Assert.assertEquals(instant, DateTimeUtil.parseZoned("2011-12-03 11:15:30 +02:00", Instant::from));
    }

    @Test
    public void testDateTimeFormattingImplicitGMT() {

        Instant instant = DateTimeFormatter.ISO_DATE_TIME.parse("2011-12-03T10:15:30+01:00", Instant::from);
        Assert.assertEquals("2011-12-03 09:15:30", DateTimeUtil.formatAsGMT(instant));
    }

    @Test
    public void testDateTimeFormattingLocal() {

        Assert.assertEquals("2011-12-03 09:15:30", DateTimeUtil.formatLocalDateTime(LocalDateTime.of(2011, Month.DECEMBER, 3, 9, 15, 30)));
        Assert.assertEquals("2011-12-03", DateTimeUtil.formatLocalDate(LocalDate.of(2011, Month.DECEMBER, 3)));
        Assert.assertEquals("09:15:30", DateTimeUtil.formatLocalTime(LocalTime.of(9, 15, 30)));
    }

    @Test
    public void testDateTimeParsingLocal() {

        Assert.assertEquals(LocalDateTime.of(2011, Month.DECEMBER, 3, 9, 15, 30), DateTimeUtil.parseLocalDateTime("2011-12-03 09:15:30"));
        Assert.assertEquals(LocalDate.of(2011, Month.DECEMBER, 3), DateTimeUtil.parseLocalDate("2011-12-03"));
        Assert.assertEquals(LocalTime.of(9, 15, 30), DateTimeUtil.parseLocalTime("09:15:30"));
    }

}