package bot.trade;

import com.alibaba.fastjson.JSON;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by louisyuu on 2021/8/27 3:22 下午
 */
public class Stats implements Cloneable {
    public static volatile double BALANCE = 100;
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


    boolean cloned;


    List<OrderRecord> orders = new ArrayList<>();

    public Stats addOrder(OrderRecord order) {
        this.orders.add(order);
        return this;
    }


    public OrderRecord getOrderByDate(ZonedDateTime zone) {
        for (OrderRecord order : orders) {
            if (zone.equals(order.getBar().getEndTime())) {
                return order;
            }
        }
        return null;
    }


    public void stats(String caller) {


        Stats snapshot = clone();

        if (snapshot == null) {
            System.out.println("Snapshot is null");
            return;
        }

        if (snapshot.orders.isEmpty()) {
            System.out.println("==> No orders");
            return;
        }
        Map<String, List<OrderRecord>> orderPairMap = snapshot.orders.stream().collect(Collectors.groupingBy(OrderRecord::getTxid));

        for (Map.Entry<String, List<OrderRecord>> entry : orderPairMap.entrySet()) {
            List<OrderRecord> orderPair = entry.getValue();
            for (OrderRecord order : orderPair) {
                if (OrderRecord.Ops.Long.equals(order.ops) || OrderRecord.Ops.Short.equals(order.ops)) {
                    snapshot.openCount++;
                } else if (OrderRecord.Ops.CloseLong.equals(order.ops) || OrderRecord.Ops.CloseShort.equals(order.ops)) {
                    snapshot.closeCount++;
                    if (order.getProfit() > 0) {
                        snapshot.succeedCount++;
                        snapshot.maxProfit = Math.max(snapshot.maxProfit, order.getProfit());
                    } else {
                        snapshot.failedCount++;
                        snapshot.maxLoss = Math.min(snapshot.maxLoss, order.getProfit());
                    }
                } else if (OrderRecord.Ops.StopLossLong.equals(order.ops) || OrderRecord.Ops.StopLossShort.equals(order.ops)) {
                    snapshot.stopLossCount++;
                    snapshot.failedCount++;
                    snapshot.maxLoss = Math.min(snapshot.maxLoss, order.getProfit());
                }
                snapshot.volume += order.volume;
                snapshot.quantity += order.quantity;
                snapshot.fee += order.fee;
                snapshot.profit += order.profit;
                snapshot.returnFee += order.fee * snapshot.RETURN_FEE_RATE;
            }
        }

        System.out.println(caller + "==> " + snapshot);
    }


    @Override
    public String toString() {
        return "Stats{" +
                "Balance=" + BALANCE +
                ", profit=" + profit +
                ", fee=" + fee +
                ", returnFee=" + returnFee +
                ", maxProfit=" + maxProfit +
                ", maxLoss=" + maxLoss +
                ", volume=" + volume +
                ", quantity=" + quantity +
                ", openCount=" + openCount +
                ", closeCount=" + closeCount +
                ", stopLossCount=" + stopLossCount +
                ", succeedCount=" + succeedCount +
                ", failedCount=" + failedCount +
                ", succeedRatio=" + ((succeedCount * 1.00) / (openCount * 1.00)) * 100 + "%" +
                "}";
    }

    @Override
    protected Stats clone() {
        String json = JSON.toJSONString(this);
        Stats stats = JSON.parseObject(json, Stats.class);
        stats.cloned = true;

        return stats;
    }


    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public double getReturnFee() {
        return returnFee;
    }

    public void setReturnFee(double returnFee) {
        this.returnFee = returnFee;
    }

    public double getMaxProfit() {
        return maxProfit;
    }

    public void setMaxProfit(double maxProfit) {
        this.maxProfit = maxProfit;
    }

    public double getMaxLoss() {
        return maxLoss;
    }

    public void setMaxLoss(double maxLoss) {
        this.maxLoss = maxLoss;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getOpenCount() {
        return openCount;
    }

    public void setOpenCount(int openCount) {
        this.openCount = openCount;
    }

    public int getCloseCount() {
        return closeCount;
    }

    public void setCloseCount(int closeCount) {
        this.closeCount = closeCount;
    }

    public int getStopLossCount() {
        return stopLossCount;
    }

    public void setStopLossCount(int stopLossCount) {
        this.stopLossCount = stopLossCount;
    }

    public int getSucceedCount() {
        return succeedCount;
    }

    public void setSucceedCount(int succeedCount) {
        this.succeedCount = succeedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<OrderRecord> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderRecord> orders) {
        this.orders = orders;
    }
}
