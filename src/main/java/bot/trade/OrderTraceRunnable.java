package bot.trade;

import org.ta4j.core.BarSeries;
import static bot.DateUtil.printHighlight;


/**
 * Created by louisyuu on 2021/8/31 2:59 下午
 */
public class OrderTraceRunnable implements Runnable {

    private final String caller;
    private final OrderTrace orderTrace;
    private final BarSeries barSeries;
    private final boolean dump;

    public OrderTraceRunnable(String caller, OrderTrace orderTrace, BarSeries barSeries, boolean dump) {
        this.caller = caller;
        this.orderTrace = orderTrace;
        this.barSeries = barSeries;
        this.dump = dump;
    }

    @Override
    public void run() {
        printHighlight("Snapshot by " + caller);
        OrderTrace snapshot = orderTrace.clone();
        if (!(snapshot.orders.isEmpty())) {
            if(dump){
                snapshot.dump(barSeries);
            }
        }
        snapshot.snapshot(caller);
    }


}
