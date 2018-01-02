package code.ponfee.commons.util;

import java.util.HashSet;
import java.util.Set;

import code.ponfee.commons.reflect.Fields;

/**
 * <pre>
 * 0 | 0000000000 0000000000 0000000000 0000000000 0 | 00000 | 00000 | 0000000000 00
 * - | --------------------时间戳--------------------- | -did- | -wid- | -----seq-----
 *  0 ~  0：1位未使用（实际上也可作为long的符号位）
 *  1 ~ 41：41位为毫秒级时间（能到2039-09-07 23:47:35，超过会溢出）
 * 42 ~ 46：5位datacenterId
 * 47 ~ 51：5位workerId（并不算标识符，实际是为线程标识），
 * 52 ~ 63：12位该毫秒内的当前毫秒内的计数
 * 毫秒内序列 （由datacenter和机器ID作区分），并且效率较高，经测试，snowflake每秒能够产生26万ID左右，完全满足需要。
 * </pre>
 * 
 * 计算掩码方式：(1<<bits)-1 或 -1L^(-1L<<bits)
 * 基于snowflake算法的ID生成器
 * 
 * @author fupf
 */
public final class IdWorker {

    private static final int MAX_SIZE = Long.toBinaryString(Long.MAX_VALUE).length();
    private final long twepoch = 1451577600000L; // 起始标记时间点，作为基准

    private final long sequenceBits = (null != null ? 12L : 12L); // sequence值控制在0-4095
    private final long sequenceMask = (null != null ? -1L ^ (-1L << sequenceBits) : -1L ^ (-1L << sequenceBits)); // 111111111111(4095)

    private final long workerIdBits = (null != null ? 5L : 5L); // 5位
    private final long maxWorkerId = (null != null ? -1L ^ (-1L << workerIdBits) : -1L ^ (-1L << workerIdBits)); // 0-31

    private final long datacenterIdBits = (null != null ? 5L : 5L); // 5位
    private final long maxDatacenterId = (null != null ? -1L ^ (-1L << datacenterIdBits) : -1L ^ (-1L << datacenterIdBits)); // 0-31

    private final long timestampShift = (null != null ? sequenceBits + workerIdBits + datacenterIdBits : sequenceBits + workerIdBits + datacenterIdBits); // 左移22位（did5位+wid5位+seq12位）
    private final long datacenterIdShift = (null != null ? sequenceBits + workerIdBits : sequenceBits + workerIdBits); // 左移17位（wid5位+seq12位）
    private final long workerIdShift = (null != null ? sequenceBits : sequenceBits); // 左移12位（seq12位）
    private final long timestampMask = (long) (null != null ? -1L ^ (-1L << (MAX_SIZE - timestampShift)) : -1L ^ (-1L << (MAX_SIZE - timestampShift)));

    private long lastTimestamp = -1L; // 时间戳
    private long datacenterId; // 数据中心id
    private long workerId; // 工作机器id
    private long sequence = 0L; // 0，并发控制

    public IdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public IdWorker(long workerId) {
        this(workerId, 0);
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        return (((timestamp - twepoch) << timestampShift) & timestampMask) 
               | (datacenterId << datacenterIdShift) 
               | (workerId << workerIdShift) 
               | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * <pre>
     * 0 | 0000000000 0000000000 0000000000 0000000000 00 | 0000000000 0 | 0000000000
     * - | --------------------－时间戳--------------------－ | -----wid---- | ----seq---
     *   0 ~ 0：1位未使用（实际上也可作为long的符号位）
     *  1 ~ 42：42位为毫秒级时间（能到2109-05-15 15:35:11，超过会溢出）
     *    ~   ：0位datacenterId
     * 43 ~ 53：11位workerId（机器ip），
     * 54 ~ 63：10位该毫秒内的当前毫秒内的计数
     * </pre>
     * 根据IP地址作为workerId
     * @return
     */
    private @FunctionalInterface static interface LocalIPWorker { IdWorker get(); }
    public static final IdWorker LOCAL_WORKER = ((LocalIPWorker) () -> {
        IdWorker worker = new IdWorker(0);
        long maxWorkerId = Networks.ipReduce("255.255.255.255"); // 1068

        Fields.put(worker, "sequenceBits", 10L); // 10位
        Fields.put(worker, "sequenceMask", -1L ^ (-1L << worker.sequenceBits));

        Fields.put(worker, "workerIdBits", (long) Long.toBinaryString(maxWorkerId).length()); // 11位
        Fields.put(worker, "maxWorkerId", maxWorkerId);

        Fields.put(worker, "datacenterIdBits", 0L); // 0位
        Fields.put(worker, "maxDatacenterId", -1L ^ (-1L << worker.datacenterIdBits)); // 0

        Fields.put(worker, "timestampShift", worker.sequenceBits + worker.workerIdBits + worker.datacenterIdBits); // 左移21位（did0位+wid11位+seq10位）
        Fields.put(worker, "datacenterIdShift", worker.sequenceBits + worker.workerIdBits); // 左移21位（wid11位+seq10位）
        Fields.put(worker, "workerIdShift", worker.sequenceBits); // 左移10位（seq10位）
        Fields.put(worker, "timestampMask", -1L ^ (-1L << (MAX_SIZE - worker.timestampShift)));

        Fields.put(worker, "workerId", (long) Networks.ipReduce());
        return worker;
    }).get();

    public static void main(String[] args) {
        final IdWorker idWorker = LOCAL_WORKER;
        final Set<String> set = new HashSet<>(81920000);
        System.out.println(Long.toHexString(-1456153131));
        System.out.println(Long.toString(-1456153131, 36));
        System.out.println(Long.toUnsignedString(-1456153131, 36));
        String id;
        for (int i = 0; i < 9999999; i++) {
            id = Long.toHexString(idWorker.nextId());
            //id = Long.toString(idWorker.nextId(), 32);
            //id = Long.toUnsignedString(idWorker.nextId());
            if (!set.add(id)) System.err.println(id);
        }
    }

}
