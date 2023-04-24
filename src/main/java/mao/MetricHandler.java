package mao;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Project name(项目名称)：Netty_optimization_real_time_monitoring
 * Package(包名): mao
 * Class(类名): MetricHandler
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2023/4/24
 * Time(创建时间)： 14:53
 * Version(版本): 1.0
 * Description(描述)： netty监控
 */

@ChannelHandler.Sharable
public class MetricHandler extends ChannelDuplexHandler
{
    /**
     * 连接信息
     */
    private final AtomicLong totalConnectionNum = new AtomicLong(0);

    private final AtomicLong dataReadNum = new AtomicLong(0);

    public MetricHandler()
    {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("totalConnectionNum", (Gauge<Long>) totalConnectionNum::get);
        metricRegistry.register("dataReadNum", (Gauge<Long>) dataReadNum::get);

        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        //设置为5秒打印一次
        consoleReporter.start(5, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // 连接数量加一
        totalConnectionNum.incrementAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // 连接数量减一
        totalConnectionNum.decrementAndGet();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        //数据读取量减一
        dataReadNum.incrementAndGet();
        super.channelRead(ctx, msg);
    }
}
