package com.denidove.Logistics.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class CalendarUtils {

    // Определяем текущую дату
    public static String getCurrentDateAbs() {
        LocalDate currDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-LL-dd");
        return currDate.format(formatter);
    }

    // Определяем завтрашнюю дату
    public static String getNextDateAbs() {
        LocalDate nextDate = LocalDate.now().plusDays(10L);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-LL-dd");
        return nextDate.format(formatter);
    }

    // Определяем дату произвольную дату отправления (плюс 7 дней к текущей дате)
    public static String getDerivalDate() {
        LocalDate nextDate = LocalDate.now().plusDays(7L);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-LL-dd");
        return nextDate.format(formatter);
    }

    // Определяем срок доставки
    public static int getDays(String startDate, String finishDate) {
        LocalDate date1 = LocalDate.parse(startDate);
        LocalDate date2 = LocalDate.parse(finishDate);

        Period period = Period.between(date1, date2);
        return period.getDays();
    }

}
