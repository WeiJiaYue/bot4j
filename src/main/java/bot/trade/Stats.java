package bot.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by louisyuu on 2021/8/27 3:22 下午
 */
public class Stats {
    public static double BALANCE = 100;
    double RETURN_FEE_RATE = 0.2;

    double profit = 0;
    double fee = 0;
    double returnFee = 0;

    double maxProfit = 0;
    double maxLoss = 0;
    double volume = 0;
    double quantity = 0;

    int openCount = 0;
    int closeCount = 0;
    int stopLossCount = 0;
    int succeedCount = 0;
    int failedCount = 0;


    List<OrderRecord> orders = new ArrayList<>();

    public Stats addOrder(OrderRecord order) {
        this.orders.add(order);
        return this;
    }


    public void stats() {
        if (orders.isEmpty()) {
            System.out.println("==> No orders");
            return;
        }
        Map<String, List<OrderRecord>> orderPairMap = orders.stream().collect(Collectors.groupingBy(OrderRecord::getTxid));

        for (Map.Entry<String, List<OrderRecord>> entry : orderPairMap.entrySet()) {
            List<OrderRecord> orderPair = entry.getValue();
            for (OrderRecord order : orderPair) {
                if (OrderRecord.Ops.Long.equals(order.ops) || OrderRecord.Ops.Short.equals(order.ops)) {
                    openCount++;
                } else if (OrderRecord.Ops.CloseLong.equals(order.ops) || OrderRecord.Ops.CloseShort.equals(order.ops)) {
                    closeCount++;
                    if (order.getProfit() > 0) {
                        succeedCount++;
                        maxProfit = Math.max(maxProfit, order.getProfit());
                    } else {
                        failedCount++;
                        maxLoss = Math.min(maxLoss, order.getProfit());
                    }
                } else if (OrderRecord.Ops.StopLossLong.equals(order.ops) || OrderRecord.Ops.StopLossShort.equals(order.ops)) {
                    stopLossCount++;
                    failedCount++;
                    maxLoss = Math.min(maxLoss, order.getProfit());
                }
                volume += order.volume;
                quantity += order.quantity;
                fee += order.fee;
                profit += order.profit;
                returnFee += order.fee * RETURN_FEE_RATE;
            }
        }
    }


    @Override
    public String toString() {
        return "Stats{" +
                "orders=" + orders +
                '}';
    }
}
