// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package test.jce.pwd;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import code.ponfee.commons.jce.pwd.PBKDF2;

public class PBKDFTester {

    @Test
    public void pbkdf2_hmac_sha256_scrypt() throws Exception {
        String alg = "HmacSHA256";
        byte[] P, S;
        int c, dkLen;
        String DK;

        P = "password".getBytes("UTF-8");
        S = "salt".getBytes("UTF-8");
        c = 4096;
        dkLen = 32;
        DK = "xeR41ZKIyEGqUw22hFxMjZYok6ABzk4RpJY4c6qYE0o";

        assertTrue(DK.equals(PBKDF2.create(alg, P, S, c, dkLen)));
    }
}
