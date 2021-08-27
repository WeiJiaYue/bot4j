package bot.trade;

/**
 * Created by louisyuu on 2021/8/27 3:22 下午
 */
public class Stats {
    double RETURN_FEE_RATE = 0.2;

    double balance = 10000;
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


    @Override
    public String toString() {
        return "Stats{" +
                "balance=" + balance +
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
                ", successCount=" + successCount +
                ", lossCount=" + lossCount +
                '}';
    }
}
