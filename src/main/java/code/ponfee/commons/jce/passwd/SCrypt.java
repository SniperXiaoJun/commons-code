// Copyright (C) 2011 - Will Glozer. All rights reserved.

package code.ponfee.commons.jce.passwd;

import static code.ponfee.commons.jce.HmacAlgorithm.ALGORITHM_MAPPING;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.arraycopy;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jce.HmacAlgorithm;
import code.ponfee.commons.jce.hash.HmacUtils;
import code.ponfee.commons.math.Maths;
import code.ponfee.commons.util.SecureRandoms;

/**
 * An implementation of the <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt</a> 
 * key derivation function. This class will attempt to load a native
 * library containing the optimized C implementation from 
 * <a href="http://www.tarsnap.com/scrypt.html">http://www.tarsnap.com/scrypt.html</a> 
 * and fall back to the pure Java version if that fails.
 * 
 * @author Will Glozer
 * @author Ponfee
 * 
 * Reference from internet and with optimization
 */
public final class SCrypt {
    private SCrypt() {}

    private static final String SEPARATOR = "$";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    // -------------------------------------pbkdf2------------------------------
    /**
     * Implementation of PBKDF2 (RFC2898).
     * @param alg HMAC algorithm to use.
     * @param P Password.
     * @param S Salt.
     * @param c Iteration count.
     * @param dkLen Intended length, in octets, of the derived key (hash byte size).
     * @return The derived key.
     */
    public static String create(HmacAlgorithm alg, byte[] P, byte[] S, 
                                int c, int dkLen) {
        return encodeBase64(pbkdf2(alg, P, S, c, dkLen));
    }

    public static boolean check(HmacAlgorithm alg, byte[] P, byte[] S,
                                int c, String hashed) {
        byte[] actual = Base64.getUrlDecoder().decode(hashed);
        byte[] except = pbkdf2(alg, P, S, c, actual.length);
        return Arrays.equals(actual, except);
    }

    // -------------------------------------scrypt------------------------------
    /**
     * fix
     *     HmacAlgorithm   HmacSHA256
     *     dkLen           32 byte
     * @param passwd Password.
     * @param N CPU cost parameter.
     * @param r Memory cost parameter.
     * @param p Parallelization parameter.
     * @return
     */
    public static String create(String passwd, int N, int r, int p) {
        return create(HmacAlgorithm.HmacSHA256, passwd, N, r, p, 32);
    }

    /**
     * Hash the supplied plaintext password and generate output 
     * in the format described in {@link SCryp}.
     * @param alg HmacAlgorithm.
     * @param passwd Password.
     * @param N CPU cost parameter.
     * @param r Memory cost parameter.
     * @param p Parallelization parameter.
     * @param dkLen Intended length, in octets, of the derived key.
     * @return The hashed password.
     */
    public static String create(HmacAlgorithm alg, String passwd, 
                                int N, int r, int p, int dkLen) {
        // N max 0x800000 -> 8388608
        //Preconditions.checkArgument(N >= 1 && N <= 8388608, "N must between 1 and 8388608");
        Preconditions.checkArgument(r >= 1 && r <= 0xff, "r must between 1 and 255");
        Preconditions.checkArgument(p >= 1 && p <= 0xff, "r must between 1 and 255");

        long algIdx = ALGORITHM_MAPPING.inverse().get(alg) & 0xf; // maximum is 0xf
        byte[] salt = SecureRandoms.nextBytes(32);
        byte[] derived = scrypt(alg, passwd.getBytes(UTF_8), salt, N, r, p, dkLen);
        long log2 = Math.round(Maths.log2(N));
        String params = Long.toString(algIdx << 32 | log2 << 16 | r << 8 | p, 16);

        return new StringBuilder(15 + (salt.length + derived.length) * 4 / 3 + 4)
                        .append(SEPARATOR).append("s0").append(SEPARATOR)
                        .append(params).append(SEPARATOR)
                        .append(encodeBase64(salt)).append(SEPARATOR)
                        .append(encodeBase64(derived)).toString();
    }

    /**
     * Compare the supplied plaintext password to a hashed password.
     * @param passwd Plaintext password.
     * @param hashed scrypt hashed password.
     * @return true if passwd matches hashed value.
     */
    public static boolean check(String passwd, String hashed) {
        String[] parts = hashed.split("\\" + SEPARATOR);

        if (parts.length != 5 || !"s0".equals(parts[1])) {
            throw new IllegalArgumentException("Invalid hashed value");
        }

        long params = Long.parseLong(parts[2], 16);
        byte[] salt = Base64.getUrlDecoder().decode(parts[3]);
        byte[] actual = Base64.getUrlDecoder().decode(parts[4]);

        HmacAlgorithm alg = ALGORITHM_MAPPING.get((int) (params >> 32 & 0xf));
        int N = (int) Math.pow(2, params >> 16 & 0xffff);
        int r = (int) params >> 8 & 0xff;
        int p = (int) params      & 0xff;

        byte[] except = scrypt(alg, passwd.getBytes(UTF_8), salt, N, r, p, actual.length);

        return Arrays.equals(actual, except);
    }

    /**
     * Implementation of PBKDF2 (RFC2898).
     * @param alg HmacAlgorithm.
     * @param P password of byte array.
     * @param S Salt.
     * @param c Iteration count.
     * @param dkLen Intended length, in octets, of the derived key.
     * @return the byte array of DK
     */
    private static byte[] pbkdf2(HmacAlgorithm alg, byte[] P, 
                                 byte[] S, int c, int dkLen) {
        Mac mac = HmacUtils.getInitializedMac(alg.name(), P);
        int hLen = mac.getMacLength();

        // (Math.pow(2, 32) - 1) == 4294967295L
        if (dkLen > 4294967295L * hLen) {
            throw new SecurityException("Requested key length too long");
        }

        byte[] U = new byte[hLen];
        byte[] T = new byte[hLen];
        byte[] block = new byte[S.length + 4];

        int n = (int) Math.ceil((double) dkLen / hLen);
        int r = dkLen - (n - 1) * hLen;

        arraycopy(S, 0, block, 0, S.length);

        byte[] DK = new byte[dkLen];
        for (int i = 1; i <= n; i++) {
            block[S.length + 0] = (byte) (i >> 24 & 0xff);
            block[S.length + 1] = (byte) (i >> 16 & 0xff);
            block[S.length + 2] = (byte) (i >> 8  & 0xff);
            block[S.length + 3] = (byte) (i >> 0  & 0xff);

            mac.update(block);
            try {
                mac.doFinal(U, 0);
                arraycopy(U, 0, T, 0, hLen);
                for (int j = 1, k; j < c; j++) {
                    mac.update(U);
                    mac.doFinal(U, 0);
                    for (k = 0; k < hLen; k++) {
                        T[k] ^= U[k];
                    }
                }
            } catch (ShortBufferException | IllegalStateException e) {
                throw new SecurityException(e);
            }

            arraycopy(T, 0, DK, (i - 1) * hLen, (i == n ? r : hLen));
        }
        return DK;
    }

    /**
     * Pure Java implementation of the 
     * <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt KDF</a>.
     * @param alg HmacAlgorithm.
     * @param passwd Password.
     * @param salt Salt.
     * @param N CPU cost parameter.
     * @param r Memory cost parameter.
     * @param p Parallelization parameter.
     * @param dkLen Intended length of the derived key.
     * @return The derived key.
     * @throws GeneralSecurityException when HMAC_SHA256 is not available.
     */
    private static byte[] scrypt(HmacAlgorithm alg, byte[] passwd, byte[] salt, 
                                 int N, int r, int p, int dkLen) {
        if (N < 2 || (N & (N - 1)) != 0) {
            throw new IllegalArgumentException("N must be a power of 2 greater than 1");
        }

        if (N > MAX_VALUE / 128 / r) {
            throw new IllegalArgumentException("Parameter N is too large");
        }

        if (r > MAX_VALUE / 128 / p) {
            throw new IllegalArgumentException("Parameter r is too large");
        }

        byte[] B  = pbkdf2(alg, passwd, salt, 1, p * 128 * r);
        byte[] XY = new byte[256 * r],
               V  = new byte[128 * r * N];

        for (int i = 0; i < p; i++) {
            smix(B, i * 128 * r, r, N, V, XY);
        }

        return pbkdf2(alg, passwd, B, 1, dkLen);
    }

    private static void smix(byte[] B, int Bi, int r, int N, byte[] V, byte[] XY) {
        int i, Xi = 0, Yi = 128 * r;

        arraycopy(B, Bi, XY, Xi, 128 * r);

        for (i = 0; i < N; i++) {
            arraycopy(XY, Xi, V, i * (128 * r), 128 * r);
            blockmix_salsa8(XY, Xi, Yi, r);
        }

        for (i = 0; i < N; i++) {
            int j = integerify(XY, Xi, r) & (N - 1);
            blockxor(V, j * (128 * r), XY, Xi, 128 * r);
            blockmix_salsa8(XY, Xi, Yi, r);
        }

        arraycopy(XY, Xi, B, Bi, 128 * r);
    }

    private static void blockmix_salsa8(byte[] BY, int Bi, int Yi, int r) {
        byte[] X = new byte[64];

        arraycopy(BY, Bi + (2 * r - 1) * 64, X, 0, 64);

        int i;
        for (i = 0; i < 2 * r; i++) {
            blockxor(BY, i * 64, X, 0, 64);
            salsa20_8(X);
            arraycopy(X, 0, BY, Yi + (i * 64), 64);
        }

        for (i = 0; i < r; i++) {
            arraycopy(BY, Yi + (i * 2) * 64, BY, Bi + (i * 64), 64);
        }

        for (i = 0; i < r; i++) {
            arraycopy(BY, Yi + (i * 2 + 1) * 64, BY, Bi + (i + r) * 64, 64);
        }
    }

    private static int R(int a, int b) {
        return (a << b) | (a >>> (32 - b));
    }

    private static void salsa20_8(byte[] B) {
        int[] B32 = new int[16], x = new int[16];

        int i;
        for (i = 0; i < 16; i++) {
            B32[i]  = (B[i * 4 + 0] & 0xff) << 0;
            B32[i] |= (B[i * 4 + 1] & 0xff) << 8;
            B32[i] |= (B[i * 4 + 2] & 0xff) << 16;
            B32[i] |= (B[i * 4 + 3] & 0xff) << 24;
        }

        arraycopy(B32, 0, x, 0, 16);

        for (i = 8; i > 0; i -= 2) {
             x[4] ^=  R(x[0] + x[12],  7);
             x[8] ^=  R(x[4] +  x[0],  9);
            x[12] ^=  R(x[8] +  x[4], 13);
             x[0] ^= R(x[12] +  x[8], 18);
             x[9] ^=  R(x[5] +  x[1],  7);
            x[13] ^=  R(x[9] +  x[5],  9);
             x[1] ^= R(x[13] +  x[9], 13);
             x[5] ^=  R(x[1] + x[13], 18);
            x[14] ^= R(x[10] +  x[6],  7);
             x[2] ^= R(x[14] + x[10],  9);
             x[6] ^=  R(x[2] + x[14], 13);
            x[10] ^=  R(x[6] +  x[2], 18);
             x[3] ^= R(x[15] + x[11],  7);
             x[7] ^=  R(x[3] + x[15],  9);
            x[11] ^=  R(x[7] +  x[3], 13);
            x[15] ^= R(x[11] +  x[7], 18);
             x[1] ^=  R(x[0] +  x[3],  7);
             x[2] ^=  R(x[1] +  x[0],  9);
             x[3] ^=  R(x[2] +  x[1], 13);
             x[0] ^=  R(x[3] +  x[2], 18);
             x[6] ^=  R(x[5] +  x[4],  7);
             x[7] ^=  R(x[6] +  x[5],  9);
             x[4] ^=  R(x[7] +  x[6], 13);
             x[5] ^=  R(x[4] +  x[7], 18);
            x[11] ^= R(x[10] +  x[9],  7);
             x[8] ^= R(x[11] + x[10],  9);
             x[9] ^=  R(x[8] + x[11], 13);
            x[10] ^=  R(x[9] +  x[8], 18);
            x[12] ^= R(x[15] + x[14],  7);
            x[13] ^= R(x[12] + x[15],  9);
            x[14] ^= R(x[13] + x[12], 13);
            x[15] ^= R(x[14] + x[13], 18);
        }

        for (i = 0; i < 16; ++i) {
            B32[i] = x[i] + B32[i];
        }

        for (i = 0; i < 16; i++) {
            B[i * 4 + 0] = (byte) (B32[i] >> 0  & 0xff);
            B[i * 4 + 1] = (byte) (B32[i] >> 8  & 0xff);
            B[i * 4 + 2] = (byte) (B32[i] >> 16 & 0xff);
            B[i * 4 + 3] = (byte) (B32[i] >> 24 & 0xff);
        }
    }

    private static void blockxor(byte[] S, int Si, byte[] D, int Di, int len) {
        for (int i = 0; i < len; i++) {
            D[Di + i] ^= S[Si + i];
        }
    }

    private static int integerify(byte[] B, int Bi, int r) {
        Bi += (2 * r - 1) * 64;
        return ((B[Bi + 0] & 0xff) <<  0)
             | ((B[Bi + 1] & 0xff) <<  8)
             | ((B[Bi + 2] & 0xff) << 16)
             | ((B[Bi + 3] & 0xff) << 24);
    }

    private static String encodeBase64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static void main(String[] args) {
        System.out.println("=====================PBKDF2=============================");
        byte[] pwd = "password".getBytes(UTF_8);
        byte[] salt = "salt".getBytes(UTF_8);
        String hashed = create(HmacAlgorithm.HmacSHA256, pwd, salt, 1024, 32);
        System.out.println(hashed);
        System.out.println(check(HmacAlgorithm.HmacSHA256, pwd, salt, 1024, hashed));

        System.out.println("\n=====================Scrypt=============================");
        String password = "passwd";
        hashed = create(password, 2, 255, 255);
        System.out.println(check(password, hashed));
        hashed = create(password, 2, 4, 4);
        System.out.println(hashed);
        System.out.print("Test begin");
        boolean flag = true;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) { // 20 seconds
            if (!check(password, hashed)) {
                System.err.println("fail!");
                flag = false;
                break;
            }
        }
        System.out.println("cost: "+(System.currentTimeMillis()-start)/1000);
        if (flag) {
            System.out.println("\nTest success!");
        }
    }

}
