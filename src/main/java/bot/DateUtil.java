package bot;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by louisyuu on 2021/8/30 5:26 下午
 */
public class DateUtil {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";


    public static String getCurrentDateTime(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        return format.format(date);

    }
}
