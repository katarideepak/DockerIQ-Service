package com.dockeriq.service.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils(){}
    private static final String DATE_FORMAT = "yyyyMMdd";

    public static String getCurrentDate_YYYYMMDD() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

}
