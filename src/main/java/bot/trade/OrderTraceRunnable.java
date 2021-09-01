package bot.trade;

import bot.BarLivingStream;
import org.ta4j.core.BarSeries;

import static bot.DateUtil.printHighlight;


/**
 * Created by louisyuu on 2021/8/31 2:59 下午
 */
public class OrderTraceRunnable implements Runnable {


    private final String caller;
    private final OrderTrace orderTrace;
    private final BarLivingStream livingStream;
    private final BarSeries barSeries;
    private final boolean dump;

    public OrderTraceRunnable(String caller, OrderTrace orderTrace, BarLivingStream livingStream, BarSeries barSeries, boolean dump) {
        this.caller = caller;
        this.orderTrace = orderTrace;
        this.livingStream = livingStream;
        this.barSeries = barSeries;
        this.dump = dump;
    }

    @Override
    public void run() {
        printHighlight("Snapshot by " + caller);
        OrderTrace snapshot = orderTrace.clone();
        if (!(snapshot.orders.isEmpty())) {
            if (dump) {
                snapshot.dump(livingStream, barSeries);
            }
        }
        snapshot.snapshot(caller);
    }


}
