package bot.trade;

import com.binance.client.model.event.CandlestickEvent;

/**
 * Created by louisyuu on 2021/8/27 4:12 下午
 */
public interface LivingStream {

    void onLiving(CandlestickEvent event);
}
