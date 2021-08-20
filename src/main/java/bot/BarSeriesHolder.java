package bot;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu on 2021/8/20 1:08 下午
 */
public class BarSeriesHolder {
    public final static BarSeries BAR_SERIES = new BaseBarSeriesBuilder().build();
    public final static List<Double> SMA5_SERIES = new ArrayList<>();
    public final static List<Double> SMA10_SERIES = new ArrayList<>();


    public static void load(Map<String,Object> row){
        BigDecimal open = new BigDecimal(String.valueOf(row.get("Open")));
        BigDecimal high = new BigDecimal(String.valueOf(row.get("High")));
        BigDecimal low = new BigDecimal(String.valueOf(row.get("Low")));
        BigDecimal close = new BigDecimal(String.valueOf(row.get("Close")));
        BigDecimal volume = new BigDecimal(String.valueOf(row.get("Volume")));
        String timestamp = String.valueOf(row.get("Timestamp"));
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(Long.parseLong(timestamp)).toInstant(), ZoneId.systemDefault());
        BAR_SERIES.addBar(zonedDateTime, open, high, low, close, volume);
    }


}
