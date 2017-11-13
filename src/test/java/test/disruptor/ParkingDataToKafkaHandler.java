package test.disruptor;

import com.lmax.disruptor.EventHandler;

public class ParkingDataToKafkaHandler implements EventHandler<InParkingDataEvent> {

    @Override
    public void onEvent(InParkingDataEvent event, long sequence,
        boolean endOfBatch) throws Exception {
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        String carLicense = event.getCarLicense();
        System.out.println(String.format("Thread %s--%s send %s in plaza messsage to kafka...", threadId, threadName, carLicense));
    }
}