package bot.trade;

import org.ta4j.core.Bar;

import java.util.Date;
import java.util.Scanner;

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
    String time;
    long timestamp;

    Bar bar;
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

    public OrderRecord bar(Bar bar) {
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
        return "==> OrderRecord{" +
                "balance=" + balance +
                ", txid='" + txid + '\'' +
                ", ops=" + ops +
                ", point=" + point +
                ", stopLoss=" + stopLoss +
                ", volume=" + volume +
                ", quantity=" + quantity +
                ", profit=" + profit +
                ", fee=" + fee +
                ", time='" + time + '\'' +
                ", timestamp=" + timestamp +
                ", ma5=" + ma5 +
                ", ma10=" + ma10 +
                " \n==> OrderedBar=" + bar +
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

    public Bar getBar() {
        return bar;
    }

    public double getMa5() {
        return ma5;
    }

    public double getMa10() {
        return ma10;
    }


    static String Hello = "Hello-World";

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);    //构造Scanner类的对象scan，接收从控制台输入的信息


        while(scan.hasNextLine()){
            String s = scan.nextLine();

            System.out.println("Enter "+s);

            if(s.equals("1")){
                System.out.println(Hello);

            }

        }

        System.out.println("End");

    }
}
