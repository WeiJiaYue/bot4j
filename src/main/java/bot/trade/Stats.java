package bot.trade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by louisyuu on 2021/8/27 3:22 下午
 */
public class Stats {
    double RETURN_FEE_RATE = 0.2;

    double profit = 0;
    double fee = 0;
    double returnFee = fee * RETURN_FEE_RATE;

    double maxProfit = 0;
    double maxLoss = 0;
    double volume = 0;
    double quantity = 0;

    int openCount = 0;
    int closeCount = 0;
    int stopLossCount = 0;
    int successCount = 0;
    int lossCount = 0;


    List<OrderRecord> orders = new ArrayList<>();

    public Stats addOrder(OrderRecord order) {
        this.orders.add(order);
        return this;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "orders=" + orders +
                '}';
    }
}
