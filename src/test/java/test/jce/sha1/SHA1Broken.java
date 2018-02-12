package test.jce.sha1;

import java.io.File;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.jce.HashAlgorithms;
import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.hash.HashUtils;
import code.ponfee.commons.jce.hash.HmacUtils;
import code.ponfee.commons.jce.hash.SHA1Digest;
import code.ponfee.commons.util.MavenProjects;

public class SHA1Broken {

    public static void main(String[] args) {
        byte[] pdf1 = Files.toByteArray(new File(MavenProjects.getTestJavaPath("test.jce.sha1", "shattered-1.pdf")));
        byte[] pdf2 = Files.toByteArray(new File(MavenProjects.getTestJavaPath("test.jce.sha1", "shattered-2.pdf")));
        System.out.println(Hex.encodeHexString(SHA1Digest.getInstance().doFinal(pdf1)));
        System.out.println(Hex.encodeHexString(SHA1Digest.getInstance().doFinal(pdf2)));
        System.out.println(Hex.encodeHexString(HashUtils.sha1(pdf1)));
        System.out.println(Hex.encodeHexString(HashUtils.sha1(pdf2)));

        System.out.println(Hex.encodeHexString(HashUtils.sha256(pdf1)));
        System.out.println(Hex.encodeHexString(HashUtils.sha256(pdf2)));

        System.out.println(Hex.encodeHexString(HashUtils.digest(HashAlgorithms.KECCAK256, Providers.BC, pdf2)));
        System.out.println(Hex.encodeHexString(HashUtils.digest(HashAlgorithms.KECCAK512, Providers.BC, pdf2)));

        System.out.println(Hex.encodeHexString(HmacUtils.crypt("1234".getBytes(), pdf2, HmacAlgorithms.HmacKECCAK512)));
    }
}
