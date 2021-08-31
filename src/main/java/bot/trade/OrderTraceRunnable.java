package bot.trade;

import bot.SnapshotGenerator;
import bot.excel.ExcelProcessor;
import bot.excel.ExcelTable;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.Map;

/**
 * Created by louisyuu on 2021/8/31 2:59 下午
 */
public class OrderTraceRunnable implements Runnable {

    private final String caller;
    private final OrderTrace stats;
    private final BarSeries barSeries;


    public OrderTraceRunnable(String caller, OrderTrace stats, BarSeries barSeries) {
        this.caller = caller;
        this.stats = stats;
        this.barSeries = barSeries;
    }

    @Override
    public void run() {
        System.out.println("Start run shutdown");
        new ExcelProcessor(SnapshotGenerator.FILE_PATH) {
            @Override
            protected ExcelTable getExcelTable() {
                ExcelTable table = new ExcelTable();
                table.addColumn("Date")
                        .addColumn("O")
                        .addColumn("H")
                        .addColumn("C")
                        .addColumn("L")
                        .addColumn("V")
                        .addColumn("MA5")
                        .addColumn("MA10")
                        .addColumn("Balance")
                        .addColumn("Txid")
                        .addColumn("Ops")
                        .addColumn("Point")
                        .addColumn("StopLoss")
                        .addColumn("TV")
                        .addColumn("Quantity")
                        .addColumn("Fee")
                        .addColumn("Profit")
                        .addColumn("OrderDetail");

                return table;
            }

            @Override
            public void doProcess(ExcelTable table) throws Exception {
                for (int i = 0; i <= barSeries.getEndIndex(); i++) {
                    Bar bar = barSeries.getBar(i);
                    Map<String, Object> row = table.createEmptyRow();
                    row.put("Date", bar.getEndTime());
                    row.put("O", String.valueOf(bar.getOpenPrice()));
                    row.put("H", String.valueOf(bar.getHighPrice()));
                    row.put("C", String.valueOf(bar.getClosePrice()));
                    row.put("L", String.valueOf(bar.getLowPrice()));
                    row.put("V", String.valueOf(bar.getVolume()));
                    OrderTrace snapshot = stats.clone();
                    OrderRecord order = snapshot.getOrderByDate(bar.getEndTime());
                    if (order != null) {
                        row.put("MA5", String.valueOf(order.getMa5()));
                        row.put("MA10", String.valueOf(order.getMa10()));
                        row.put("Balance", String.valueOf(order.getBalance()));
                        row.put("Txid", String.valueOf(order.getTxid()));
                        row.put("Ops", String.valueOf(order.getOps()));
                        row.put("Point", String.valueOf(order.getPoint()));
                        row.put("StopLoss", String.valueOf(order.getStopLoss()));
                        row.put("TV", String.valueOf(order.getVolume()));
                        row.put("Quantity", String.valueOf(order.getQuantity()));
                        row.put("Fee", String.valueOf(order.getFee()));
                        row.put("Profit", String.valueOf(order.getProfit()));
                        row.put("OrderDetail", order.toString());
                    }
                    table.addRow(row);
                }
            }
        }.process();


        stats.snapshotForCurrentOrderTrace(caller);

    }
}
