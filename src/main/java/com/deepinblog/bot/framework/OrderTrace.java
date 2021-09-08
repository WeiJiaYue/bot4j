package com.deepinblog.bot.framework;

import com.deepinblog.bot.utils.Constants;
import com.deepinblog.bot.utils.excel.ExcelProcessor;
import com.deepinblog.bot.utils.excel.ExcelTable;
import com.alibaba.fastjson.JSONObject;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.deepinblog.bot.utils.DateUtil.printHighlight;

/**
 * Created by louisyuu on 2021/8/27 3:22 下午
 */
public class OrderTrace implements Cloneable {
    public double balance;


    public double RETURN_FEE_RATE = 0.2;
    public double profit = 0;
    public double fee = 0;
    public double returnFee = 0;

    public double maxProfit = 0;
    public double maxLoss = 0;
    public double volume = 0;
    public double quantity = 0;

    public int openCount = 0;
    public int closeCount = 0;
    public int stopLossCount = 0;
    public int succeedCount = 0;
    public int failedCount = 0;
    public boolean cloned;
    List<OrderRecord> orders = new ArrayList<>();

    public OrderTrace(double balance) {
        this.balance = balance;
    }

    public void addOrder(OrderRecord order) {
        this.orders.add(order);
    }


    /**
     * 有可能获取到多个
     * 就MA策略而言，因为在同一根K线周期内会存在开单然后根据实时成交价止损的情况
     */
    public List<OrderRecord> getOrdersByDate(ZonedDateTime zone) {
        List<OrderRecord> results = new ArrayList<>();
        for (OrderRecord order : orders) {
            if (zone.equals(order.getBar())) {
                results.add(order);
            }
        }
        return results;
    }


    public void snapshot(String caller) {
        OrderTrace snapshot = snapshot();
        if (snapshot.orders.isEmpty()) {
            printHighlight("No orders");
            return;
        }
        Map<String, List<OrderRecord>> orderPairMap = snapshot.orders.stream().collect(Collectors.groupingBy(OrderRecord::getTxid));
        if (orderPairMap.isEmpty()) {
            printHighlight("OrderPairMap is empty");
            return;
        }
        SortedMap<String, List<OrderRecord>> sortedMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    return Integer.parseInt(o1) - Integer.parseInt(o2);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });
        sortedMap.putAll(orderPairMap);
        for (Map.Entry<String, List<OrderRecord>> entry : sortedMap.entrySet()) {
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
        if ("ShutdownMonitor".equals(caller)) {
            printHighlight("Total trading order size :" + snapshot.getOrders().size());
            printHighlight("Total trading order overview (sum) " + snapshot + " by " + caller);
        } else {
            printHighlight("Current trading order overview (sum) " + snapshot + " by " + caller);
        }

    }


    public void dump(TradingExecutor tradingExecutor) {
        OrderTrace snapshot = snapshot();
        new ExcelProcessor(Constants.FILE_PATH) {
            @Override
            protected ExcelTable getExcelTable() {
                ExcelTable table = new ExcelTable();
                table.addColumn("Date").addColumn("O").addColumn("H").addColumn("C").addColumn("L").addColumn("V")
                        .addColumn("LastPrice")
                        .addColumn("Balance").addColumn("Txid").addColumn("Ops").addColumn("Time")
                        .addColumn("Point").addColumn("StopLoss").addColumn("TV")
                        .addColumn("Quantity").addColumn("Fee").addColumn("Profit")
                        .addColumn("OrderDetail");
                return table;
            }

            @Override
            public void doProcess(ExcelTable table) throws Exception {
                BarSeries barSeries = tradingExecutor.getSource().getBarSeries();

                for (int i = 0; i <= tradingExecutor.getSource().getBarSeries().getEndIndex(); i++) {
                    Bar bar = barSeries.getBar(i);
                    Map<String, Object> row = table.createEmptyRow();
                    row.put("Date", bar.getEndTime());
                    row.put("O", String.valueOf(bar.getOpenPrice()));
                    row.put("H", String.valueOf(bar.getHighPrice()));
                    row.put("C", String.valueOf(bar.getClosePrice()));
                    row.put("L", String.valueOf(bar.getLowPrice()));
                    row.put("V", String.valueOf(bar.getVolume()));
                    table.addRow(row);
                    List<OrderRecord> orders = snapshot.getOrdersByDate(bar.getEndTime());
                    if (orders != null && !orders.isEmpty()) {
                        OrderRecord order = orders.get(0);
                        snapshotOrderRecordToExcelRow(row, order);
                        for (int j = 1; j < orders.size(); j++) {
                            Map<String, Object> extraRow = table.createEmptyRow();
                            table.addRow(extraRow);
                            snapshotOrderRecordToExcelRow(extraRow, orders.get(j));
                        }
                    }

                }
            }
        }.process();
    }

    private void snapshotOrderRecordToExcelRow(Map<String, Object> row, OrderRecord order) {
        row.put("LastPrice", String.valueOf(order.getLastPrice()));
        row.put("Balance", String.valueOf(order.getBalance()));
        row.put("Txid", String.valueOf(order.getTxid()));
        row.put("Ops", String.valueOf(order.getOps()));
        row.put("Time", String.valueOf(order.getTime()));
        row.put("Point", String.valueOf(order.getPoint()));
        row.put("StopLoss", String.valueOf(order.getStopLoss()));
        row.put("TV", String.valueOf(order.getVolume()));
        row.put("Quantity", String.valueOf(order.getQuantity()));
        row.put("Fee", String.valueOf(order.getFee()));
        row.put("Profit", String.valueOf(order.getProfit()));
        row.put("OrderDetail", String.valueOf(order.toString()));
    }


    private OrderTrace snapshot() {
        OrderTrace snapshot = this;
        if (!this.cloned) {
            snapshot = clone();
        }
        return snapshot;
    }


    @Override
    protected OrderTrace clone() {
        String json = JSONObject.toJSONString(this);
        List<OrderRecord> srcOrders = this.getOrders();
        OrderTrace trace = JSONObject.parseObject(json, OrderTrace.class);
        trace.cloned = true;
        trace.setOrders(srcOrders);
        return trace;
    }

    public double getBalance() {
        return balance;
    }

    public synchronized void setBalance(double balance) {
        this.balance = balance;
    }

    public synchronized void balanceChange(double amount) {
        this.balance += amount;
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


    @Override
    public String toString() {
        return "\n{" + "\n" +
                "CurrentBalance=" + balance + "\n" +
                ", Profit=" + profit + "\n" +
                ", Fee=" + fee + "\n" +
                ", ReturnFee=" + returnFee + "\n" +
                ", MaxProfit=" + maxProfit + "\n" +
                ", MaxLoss=" + maxLoss + "\n" +
                ", Volume=" + volume + "\n" +
                ", Quantity=" + quantity + "\n" +
                ", OpenedCount=" + openCount + "\n" +
                ", ClosedCount=" + closeCount + "\n" +
                ", StopLossCount=" + stopLossCount + "\n" +
                ", SucceedCount=" + succeedCount + "\n" +
                ", FailedCount=" + failedCount + "\n" +
                ", SucceedRatio=" + ((succeedCount * 1.00) / (openCount * 1.00)) * 100 + "%" + "\n" +
                "}\n";
    }
}
