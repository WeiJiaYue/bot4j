package bot.trade;


import java.time.ZonedDateTime;

/**
 * Created by louisyuu on 2021/8/30 3:30 下午
 */
public class OrderRecord {
    double balance = -1;

    String txid;
    Ops ops;
    double point;
    double lastPrice;
    double stopLoss = -1;
    double volume = 0;
    double quantity = 0;
    double profit = 0;
    double fee = 0;
    String time;
    long timestamp;

    ZonedDateTime bar;
    double ma5 = -1;
    double ma10 = -1;

    private OrderRecord() {
    }

    public static OrderRecord build() {
        return new OrderRecord();
    }

    public OrderRecord balance(double balance) {
        this.balance = balance;
        return this;
    }

    public OrderRecord txid(String txid) {
        this.txid = txid;
        return this;

    }

    public OrderRecord ops(Ops ops) {
        this.ops = ops;
        return this;

    }

    public OrderRecord point(double point) {
        this.point = point;
        return this;
    }

    public OrderRecord lastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
        return this;
    }

    public OrderRecord stopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
        return this;

    }

    public OrderRecord volume(double volume) {
        this.volume = volume;
        return this;
    }

    public OrderRecord quantity(double quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderRecord profit(double profit) {
        this.profit = profit;
        return this;
    }

    public OrderRecord fee(double fee) {
        this.fee = fee;
        return this;
    }

    public OrderRecord time(String time) {
        this.time = time;
        return this;
    }

    public OrderRecord timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public OrderRecord bar(ZonedDateTime bar) {
        this.bar = bar;
        return this;
    }

    public OrderRecord ma5(double ma5) {
        this.ma5 = ma5;
        return this;
    }

    public OrderRecord ma10(double ma10) {
        this.ma10 = ma10;
        return this;
    }


    @Override
    public String toString() {
        return "OrderRecord{" +
                "balance:" + balance +
                ", txid:'" + txid + '\'' +
                ", ops:" + ops +
                ", point:" + point +
                ", lastPrice:" + lastPrice +
                ", stopLoss:" + stopLoss +
                ", volume:" + volume +
                ", quantity:" + quantity +
                ", profit:" + profit +
                ", fee:" + fee +
                ", time:'" + time + '\'' +
                ", timestamp:" + timestamp +
                ", ma5:" + ma5 +
                ", ma10:" + ma10 +
                " \nAt kline=" + bar +
                '}';
    }

    enum Ops {
        //多
        Long,
        //空
        Short,
        //平多
        CloseLong,
        //平空
        CloseShort,
        //多单止损
        StopLossLong,
        //空单止损
        StopLossShort
    }


    public double getBalance() {
        return balance;
    }

    public String getTxid() {
        return txid;
    }

    public Ops getOps() {
        return ops;
    }

    public double getPoint() {
        return point;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public double getVolume() {
        return volume;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getProfit() {
        return profit;
    }

    public double getFee() {
        return fee;
    }

    public String getTime() {
        return time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ZonedDateTime getBar() {
        return bar;
    }

    public double getMa5() {
        return ma5;
    }

    public double getMa10() {
        return ma10;
    }


}
