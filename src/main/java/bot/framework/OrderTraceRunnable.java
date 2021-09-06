package bot.framework;


import static bot.utils.DateUtil.printHighlight;


/**
 * Created by louisyuu on 2021/8/31 2:59 下午
 */
public class OrderTraceRunnable implements Runnable {


    private final String caller;
    private final OrderTrace orderTrace;
    private final SmaTradingExecutor smaTradingExecutor;
    private final boolean dump;

    public OrderTraceRunnable(String caller, OrderTrace orderTrace, SmaTradingExecutor smaTradingExecutor, boolean dump) {
        this.caller = caller;
        this.orderTrace = orderTrace;
        this.smaTradingExecutor = smaTradingExecutor;
        this.dump = dump;
    }

    @Override
    public void run() {
        printHighlight("Snapshot by " + caller);
        OrderTrace snapshot = orderTrace.clone();
        if (!(snapshot.orders.isEmpty())) {
            if (dump) {
                snapshot.dump(smaTradingExecutor);
            }
        }
        snapshot.snapshot(caller);
    }


}
