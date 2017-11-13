package test.disruptor;

import java.util.concurrent.CountDownLatch;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;

import code.ponfee.commons.concurrent.NamedThreadFactory;

/**
 * 测试 P1生产消息，C1，C2消费消息，C1和C2会共享所有的event元素! C3依赖C1，C2处理结果
 */
public class TestP1c1c2c3A {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException {
        long beginTime = System.currentTimeMillis();

        int bufferSize = 1024;
        //构造缓冲区与事件生成
        Disruptor<InParkingDataEvent> disruptor = new Disruptor<>(() -> new InParkingDataEvent(), bufferSize, 
                                                                  new NamedThreadFactory("aaaaaaa"), ProducerType.SINGLE, new YieldingWaitStrategy());

        //使用disruptor创建消费者组C1,C2  
        EventHandlerGroup<InParkingDataEvent> handlerGroup = disruptor.handleEventsWith(new ParkingDataToKafkaHandler(), 
                                                                                        new ParkingDataInDbHandler());

        //声明在C1,C2完事之后执行JMS消息发送操作 也就是流程走到C3  
        handlerGroup.then(new ParkingDataSmsHandler());

        disruptor.start();//启动  
        CountDownLatch latch = new CountDownLatch(1);
        //生产者准备  
        new InParkingDataEventPublisher(latch, disruptor).produce();

        latch.await();//等待生产者结束
        disruptor.shutdown();

        System.out.println("总耗时:" + (System.currentTimeMillis() - beginTime));
    }
}