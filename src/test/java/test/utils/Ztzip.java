package test.utils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;

import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.UrlCoder;

public class Ztzip {

    private static Logger logger = LoggerFactory.getLogger(Ztzip.class);
    public static void main(String[] args) throws IOException {
        //org.zeroturnaround.zip.ZipUtil.pack(new File("D:\\tmp"), new File("d:/demo.zip"));
        //org.zeroturnaround.zip.ZipUtil.unexplode(new File("D:\\Recv Files.zip"));
        
        //org.zeroturnaround.zip.ZipUtil.addOrReplaceEntries(new File("d:/demo.zip"), new ZipEntrySource[] {new ByteSource("README.md", "readme!!!!!!!!!!!!!!!!!!!".getBytes())});
        //jodd.io.ZipUtil.unzip("D:\\sql script", "d:/demo1.zip");
        //jodd.io.ZipUtil.zip("D:\\sql script");
        //jodd.io.ZipUtil.gzip("D:\\demo.zip");
        
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.info("abcd");
        logger.warn("abcd");
        logger.error("abcd");
        
        
        
        System.out.println(Bytes.hexDump(UrlCoder.decodeURI("EQAVTggIStNJKr7aEHFff8qyUlw%3D").getBytes()));
    }
}
