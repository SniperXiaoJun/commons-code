package test.serial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import bean.TestBean;
import code.ponfee.commons.serial.FstSerializer;
import code.ponfee.commons.serial.HessianSerializer;
import code.ponfee.commons.serial.JavaSerializer;
import code.ponfee.commons.serial.JsonSerializer;
import code.ponfee.commons.serial.KryoSerializer;
import code.ponfee.commons.serial.Serializer;
import code.ponfee.commons.serial.StringSerializer;

public class SerializerTester {
    private String text;

    @Before
    public void setUp() {
        FileInputStream in = null;
        Scanner scan = null;
        try {
            String path = Thread.currentThread().getContextClassLoader().getResource("").getFile();
            path = new File(path).getParentFile().getParentFile().getPath() + "/src/test/java/";
            path += this.getClass().getCanonicalName().replace('.', '/') + ".java";
            in = new FileInputStream(path);
            scan = new Scanner(in);
            StringBuilder builder = new StringBuilder();
            while (scan.hasNext()) {
                builder.append(scan.nextLine());
            }
            builder.setLength(1000);
            text = builder.toString().replace("\r|\n", "");
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scan != null) scan.close();
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testKryo() {
        Serializer serializer = null;
        boolean isCompress;
        byte[] data = null;
        TestBean b = null;
        long start = System.currentTimeMillis();
        System.out.println("\nkryo start =======================================================");

        System.out.println("--------------------no gzip---------------------------------");
        isCompress = false;
        serializer = new KryoSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后不压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("--------------------use gzip---------------------------------");
        isCompress = true;
        serializer = new KryoSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("kryo end =================================================耗时" + (System.currentTimeMillis() - start) + "\n");

    }

    @Test
    public void testJdk() {
        Serializer serializer = null;
        boolean isCompress;
        byte[] data = null;
        TestBean b = null;
        long start = System.currentTimeMillis();
        System.out.println("\njdk start =======================================================");

        System.out.println("--------------------no gzip---------------------------------");
        isCompress = false;
        serializer = new JavaSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后不压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("--------------------use gzip---------------------------------");
        isCompress = true;
        serializer = new JavaSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("jdk end ===============================================耗时" + (System.currentTimeMillis() - start) + "\n");
    }

    @Test
    public void testJson() {
        Serializer serializer = null;
        boolean isCompress;
        byte[] data = null;
        TestBean b = null;
        long start = System.currentTimeMillis();
        System.out.println("\njson start =======================================================");

        System.out.println("--------------------no gzip---------------------------------");
        isCompress = false;
        serializer = new JsonSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后不压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("--------------------use gzip---------------------------------");
        isCompress = true;
        serializer = new JsonSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("json end ==================================================耗时" + (System.currentTimeMillis() - start) + "\n");
    }

    @Test
    public void testHessian() {
        Serializer serializer = null;
        boolean isCompress;
        byte[] data = null;
        TestBean b = null;
        long start = System.currentTimeMillis();
        System.out.println("\nhessian start =======================================================");

        System.out.println("--------------------no gzip---------------------------------");
        isCompress = false;
        serializer = new HessianSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后不压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("--------------------use gzip---------------------------------");
        isCompress = true;
        serializer = new HessianSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("hessian end =================================================耗时" + (System.currentTimeMillis() - start) + "\n");
    }

    @Test
    public void testFst() {
        Serializer serializer = null;
        boolean isCompress;
        byte[] data = null;
        TestBean b = null;
        long start = System.currentTimeMillis();
        System.out.println("\nfst start =======================================================");

        System.out.println("--------------------no gzip---------------------------------");
        isCompress = false;
        serializer = new FstSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后不压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("--------------------use gzip---------------------------------");
        isCompress = true;
        serializer = new FstSerializer();
        data = serializer.serialize(new TestBean(1312321111, 222243222L, text), isCompress);
        System.out.println("序例化后压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, TestBean.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("fst end =================================================耗时" + (System.currentTimeMillis() - start) + "\n");
    }

    @Test
    public void testString() {
        Serializer serializer = null;
        boolean isCompress;
        byte[] data = null;
        String b = null;
        long start = System.currentTimeMillis();
        System.out.println("\nstring start =======================================================");

        System.out.println("--------------------no gzip---------------------------------");
        isCompress = false;
        serializer = new StringSerializer();
        data = serializer.serialize(text, isCompress);
        System.out.println("序例化后不压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, String.class, isCompress);
        System.out.println("反序例化后的对象：" + b);

        System.out.println("--------------------use gzip---------------------------------");
        isCompress = true;
        serializer = new StringSerializer();
        data = serializer.serialize(text, isCompress);
        System.out.println("序例化后压缩的数据大小：" + data.length);
        b = serializer.deserialize(data, String.class, isCompress);
        System.out.println("反序例化后的对象：" + b);
        System.out.println("string end ===============================================耗时" + (System.currentTimeMillis() - start) + "\n");
    }

    @Test
    public void testCompress() {
        long start = System.currentTimeMillis();
        System.out.println("\ncompress start =======================================================");
        System.out.println("压缩前数据：" + text);
        byte[] data = text.getBytes();
        System.out.println("压缩前的数据大小：" + data.length);
        data = Serializer.compress(data);
        System.out.println("压缩后的数据大小：" + data.length);

        data = Serializer.decompress(data);
        System.out.println("解压缩后数据：" + new String(data));
        System.out.println("compress end =======================================================耗时" + (System.currentTimeMillis() - start) + "\n");
    }
}
