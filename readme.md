



## 实时监控运行状态

在Netty正常运行时对Netty的一些运行数据进行实时监控，可以帮助我们及时发现可能存在的问题已经对Netty的性能优化



### 依赖

使用metrics这个工具进行数据统计

```xml
<dependency>
  <groupId>io.dropwizard.metrics</groupId>
  <artifactId>metrics-core</artifactId>
  <version>4.2.17</version>
</dependency>
```





### MetricHandler

```java
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
```





### Server

```java
package mao;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Project name(项目名称)：Netty_optimization_real_time_monitoring
 * Package(包名): mao
 * Class(类名): Server
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2023/4/24
 * Time(创建时间)： 14:57
 * Version(版本): 1.0
 * Description(描述)： 监控
 */


public class Server
{
    @SneakyThrows
    public static void main(String[] args)
    {
        MetricHandler metricHandler = new MetricHandler();
        new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .childHandler(new ChannelInitializer<NioSocketChannel>()
                {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception
                    {
                        //监控
                        ch.pipeline().addLast(metricHandler);
                    }
                })
                .bind(8080)
                .sync();
    }
}
```





### 控制台输出

```sh
23-4-24 15:06:53 ===============================================================

-- Gauges ----------------------------------------------------------------------
dataReadNum
             value = 0
totalConnectionNum
             value = 0


23-4-24 15:06:58 ===============================================================

-- Gauges ----------------------------------------------------------------------
dataReadNum
             value = 0
totalConnectionNum
             value = 1
```







### Netty监控数据

|  可视化信息  |             来源              |                备注                 |
| :----------: | :---------------------------: | :---------------------------------: |
| 连接信息统计 | channelActive/channelInactive |                                     |
|  收数据统计  |          channelRead          |                                     |
|  发数据统计  |             write             | ctx.write(msg).addListener() 更准确 |
|   异常统计   | exceptionCaught/ChannelFuture |    ReadTimeoutException.INSTANCE    |



|   可视化信息    |                       来源                       |                   备注                   |
| :-------------: | :----------------------------------------------: | :--------------------------------------: |
|     线程数      |                 根据不同实现计算                 | 例如: nioEventLoopGroup.executorCount(); |
|   待处理任务    |             executor.pendingTasks()              |     例如:Nio Event Loop 的待处理任务     |
|   积累的数据    |      channelOutboundBuffer.totalPendingSize      |               Channel 级别               |
|  可写状态切换   |            channelWritabilityChanged             |                                          |
|  触发事件统计   |                userEventTriggered                |              IdleStateEvent              |
| ByteBuf分配细节 | Pooled/UnpooledByteBufAllocator.DEFAULT.metric() |                                          |



