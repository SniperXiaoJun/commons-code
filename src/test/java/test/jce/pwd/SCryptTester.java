// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package test.jce.pwd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;

import code.ponfee.commons.jce.pwd.SCrypt;

public class SCryptTester {
    String passwd = "secret";

    @Test
    public void scrypt() {
        int N = 16384;
        int r = 8;
        int p = 1;

        String hashed = SCrypt.create(passwd, N, r, p);
        String[] parts = hashed.split("\\$");

        assertEquals(5, parts.length);
        assertEquals("s0", parts[1]);
        Assert.assertEquals(16, Base64.getUrlDecoder().decode(parts[3]).length);
        assertEquals(32, Base64.getUrlDecoder().decode(parts[4]).length);

        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >> 16 & 0xffff));
        assertEquals(r, params >> 8 & 0xff);
        assertEquals(p, params >> 0 & 0xff);
    }

    @Test
    public void check() {
        String hashed = SCrypt.create(passwd, 16384, 8, 1);

        assertTrue(SCrypt.check(passwd, hashed));
        assertFalse(SCrypt.check("s3cr3t", hashed));
    }

    @Test
    public void format_0_rp_max() throws Exception {
        int N = 2;
        int r = 255;
        int p = 255;

        String hashed = SCrypt.create(passwd, N, r, p);
        assertTrue(SCrypt.check(passwd, hashed));

        String[] parts = hashed.split("\\$");
        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >>> 16 & 0xffff));
        assertEquals(r, params >> 8 & 0xff);
        assertEquals(p, params >> 0 & 0xff);
    }
}
