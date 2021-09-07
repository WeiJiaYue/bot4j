package bot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by louisyuu on 2021/8/30 5:26 下午
 */
public class DateUtil {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss:SSS";


    public static void main(String[] args) throws Exception {
        Date stopDate = getHistoricalDate("2021", "01", "30", "08");


        List<HistoricalDateTime> historicalTimestamps = getHistoricalDateTimes(Calendar.MINUTE, 1000, new Date(), stopDate);
        System.out.println(historicalTimestamps);


    }


    /**
     * @param unit   Calendar.MINUTE etc
     * @param amount
     * @return
     */
    public static List<HistoricalDateTime> getHistoricalDateTimes(int unit, int amount, Date baseDate, Date stopDate) {
        List<HistoricalDateTime> results = new ArrayList<>();
        HistoricalDateTime historicalDateTime = getHistoricalDateTime(unit, amount, baseDate);
        results.add(historicalDateTime);
        while (true) {
            if (historicalDateTime.date.before(stopDate)) {
                break;
            }
            historicalDateTime = getHistoricalDateTime(unit, amount, historicalDateTime.date);
            results.add(historicalDateTime);
        }
        Collections.reverse(results);
        return results;
    }


    /**
     * @param unit   Calendar.MINUTE etc
     * @param amount
     * @return
     */
    public static HistoricalDateTime getHistoricalDateTime(int unit, int amount, Date baseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        calendar.add(unit, -amount);
        return new HistoricalDateTime(calendar.getTime(), calendar.getTimeInMillis(), baseDate, baseDate.getTime());
    }


    public static long getCurrentTimestamp() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }


    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        return format.format(date);
    }

    public static String convertToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        return format.format(date);
    }


    public static String convertToString(ZonedDateTime zonedDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).withZone(ZoneId.systemDefault());
        return zonedDateTime.format(formatter);
    }


    public static ZonedDateTime convertToZonedDateTime(Date date) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }


    public static String getHistoricalDateString(String year, String month, String day, String hour) {
        return year + "-" + month + "-" + day + " " + hour + ":00:00:000";
    }

    public static Date getHistoricalDate(String year, String month, String day, String hour) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        try {
            return simpleDateFormat.parse(getHistoricalDateString(year, month, day, hour));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    
    public static void print(String text) {
        System.out.println(getCurrentDateTime() + " ==> " + text);
    }

    public static void printHighlight(String text) {
        System.err.println(getCurrentDateTime() + " ==> " + text);
    }


    public static class HistoricalDateTime {
        public Date date;
        public long timestamp;
        public Date baseDate;
        public long baseTimestamp;

        public HistoricalDateTime(Date date, long timestamp, Date baseDate, long baseTimestamp) {
            this.date = date;
            this.timestamp = timestamp;
            this.baseDate = baseDate;
            this.baseTimestamp = baseTimestamp;
        }
    }

}
