package test.utils;

import java.io.File;
import java.io.IOException;

import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;

public class Ztzip {

    public static void main(String[] args) throws IOException {
        //org.zeroturnaround.zip.ZipUtil.pack(new File("D:\\tmp"), new File("d:/demo.zip"));
        //org.zeroturnaround.zip.ZipUtil.unexplode(new File("D:\\Recv Files.zip"));
        
        org.zeroturnaround.zip.ZipUtil.addOrReplaceEntries(new File("d:/demo.zip"), new ZipEntrySource[] {new ByteSource("README.md", "readme!!!!!!!!!!!!!!!!!!!".getBytes())});
        //jodd.io.ZipUtil.unzip("D:\\sql script", "d:/demo1.zip");
        //jodd.io.ZipUtil.zip("D:\\sql script");
        //jodd.io.ZipUtil.gzip("D:\\demo.zip");
    }
}
