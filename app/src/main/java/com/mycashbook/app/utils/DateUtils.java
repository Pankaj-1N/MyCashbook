package com.mycashbook.app.utils;

import com.mycashbook.app.utils.Constants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date formatting and calculations
 * Centralizes all date-related operations
 */
public class DateUtils {

    private static final String DATE_FORMAT = "dd MMM yyyy";
    private static final String DATE_TIME_FORMAT = "dd MMM yyyy HH:mm";
    private static final String TIME_FORMAT = "HH:mm";
    private static final Locale LOCALE = Locale.getDefault();

    /**
     * Format date for display (e.g., "01 Jan 2025")
     * Uses existing Constants.DATE_FORMAT_DISPLAY
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, LOCALE);
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format date with full details
     * Uses existing Constants.DATE_FORMAT_FULL
     */
    public static String formatDateFull(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_FULL, LOCALE);
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format date for file naming
     * Uses existing Constants.DATE_FORMAT_FILE
     */
    public static String formatDateForFile(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_FILE, LOCALE);
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format Date object for display
     */
    public static String formatDateForDisplay(Date date) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, LOCALE);
            return sdf.format(date);
        } catch (Exception e) {
            LogUtils.e("DateUtils", "Error formatting date", e);
            return "";
        }
    }

    /**
     * Format date with time (e.g., "01 Jan 2025 14:30")
     */
    public static String formatDateWithTime(Date date) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, LOCALE);
            return sdf.format(date);
        } catch (Exception e) {
            LogUtils.e("DateUtils", "Error formatting date with time", e);
            return "";
        }
    }

    /**
     * Format only time (e.g., "14:30")
     */
    public static String formatTimeOnly(Date date) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, LOCALE);
            return sdf.format(date);
        } catch (Exception e) {
            LogUtils.e("DateUtils", "Error formatting time", e);
            return "";
        }
    }

    /**
     * Get start of day from timestamp
     * Original method - keeps existing functionality
     */
    public static long getStartOfDay(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
        String dateString = sdf.format(new Date(timestamp));
        try {
            return sdf.parse(dateString).getTime();
        } catch (Exception e) {
            LogUtils.e("DateUtils", "Error getting start of day", e);
            return timestamp;
        }
    }

    /**
     * Get end of day from timestamp
     * Original method - keeps existing functionality
     */
    public static long getEndOfDay(long timestamp) {
        return getStartOfDay(timestamp) + (24 * 60 * 60 * 1000) - 1;
    }

    /**
     * Get start of today
     */
    public static Date getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get end of today
     */
    public static Date getTodayEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Get current date
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * Get current timestamp
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar todayCal = Calendar.getInstance();

        return dateCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                dateCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                dateCal.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(Date date) {
        if (date == null) {
            return false;
        }

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_MONTH, -1);

        return dateCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                dateCal.get(Calendar.MONTH) == yesterdayCal.get(Calendar.MONTH) &&
                dateCal.get(Calendar.DAY_OF_MONTH) == yesterdayCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Check if date is in current month
     */
    public static boolean isCurrentMonth(Date date) {
        if (date == null) {
            return false;
        }

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar nowCal = Calendar.getInstance();

        return dateCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                dateCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH);
    }

    /**
     * Convert timestamp to Date object
     */
    public static Date getDateFromTimestamp(long timestamp) {
        try {
            return new Date(timestamp);
        } catch (Exception e) {
            LogUtils.e("DateUtils", "Error converting timestamp to date", e);
            return null;
        }
    }

    /**
     * Convert Date to timestamp
     */
    public static long getTimestampFromDate(Date date) {
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }

    /**
     * Get difference in days between two dates
     */
    public static int getDaysDifference(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return 0;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        long diff = cal1.getTimeInMillis() - cal2.getTimeInMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * Add days to a date
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * Get display text for date (e.g., "Today", "Yesterday", or actual date)
     */
    public static String getDateDisplayText(Date date) {
        if (date == null) {
            return "";
        }

        if (isToday(date)) {
            return "Today";
        } else if (isYesterday(date)) {
            return "Yesterday";
        } else {
            return formatDateForDisplay(date);
        }
    }

    /**
     * Parse date string in format (dd MMM yyyy)
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, LOCALE);
            return sdf.parse(dateString);
        } catch (Exception e) {
            LogUtils.e("DateUtils", "Error parsing date string: " + dateString, e);
            return null;
        }
    }

    /**
     * Get start of month for a given date
     */
    public static Date getMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get end of month for a given date
     */
    public static Date getMonthEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }
}