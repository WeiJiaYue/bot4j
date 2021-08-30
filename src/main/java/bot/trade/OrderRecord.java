package bot.trade;

/**
 * Created by louisyuu on 2021/8/30 3:30 下午
 */
public class OrderRecord {
    double balance = -1;

    String txid;
    Ops ops;
    double point;
    double stopLoss = -1;
    double volume = 0;
    double quantity = 0;
    double profit = 0;
    double fee = 0;

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

}
