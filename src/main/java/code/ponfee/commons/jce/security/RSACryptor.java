package code.ponfee.commons.jce.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.RSASignAlgorithm;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.Streams;

/**
 * RSA加/解密，签名/验签
 * 
 * PKCS#1格式：BEGIN RSA PRIVATE KEY
 * PKCS#8格式：BEGIN PRIVATE KEY
 * @author fupf
 */
public final class RSACryptor {

    private static final String ALG_RSA = "RSA";

    public static RSAKeyPair genRsaKeyPair() {
        return genRSAKeyPair(1024);
    }

    /**
     * 密钥生成
     * @param keySize 密钥长度（位）
     * @return
     */
    public static RSAKeyPair genRSAKeyPair(int keySize) {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(ALG_RSA);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
        keyPairGen.initialize(keySize);
        KeyPair pair = keyPairGen.generateKeyPair();
        return new RSAKeyPair((RSAPrivateKey) pair.getPrivate(), (RSAPublicKey) pair.getPublic());
    }

    // ----------------------------------PRIVATE KEY PKCS1 FORMAT-----------------------------------
    /**
     * MIICXAIBAAKBgQCo20qAU4iyZIInpu2XzNXYHhFv6FVC/N1vsfz4ZrwX3VQaFsXf720QBkuP34Y31jy/6B+OB7DzklDBTnJXltCX2XdHyBY5WQYMX9rsQrfbvUL47u676FD1T8o1/e+cEOGS75mKQIQjyt1zCZOl26Hy6x4TPeBSdVzFNYSr7KNjLQIDAQABAoGALFd51v0YtpACRdtmJSjbNyeeOJ7wVOkGVWCOJ8UCu9mZTkiQqd+76itdCGkQW/VceqDAOJH4e93+auTozeuC1w/srrUuPASUsE/5VLwPBvR90kToC28B59wAdl31nD0KM8COq/9EdrkVkz6XO7KAik9gr3PLHCXu4i7tzf9djlkCQQDhagX7hsjJZ554Pr0uBhXHwMmhiLPOK1b3884Wc1rHTMShVGF3DJH6stJV5hXwzjXBwSA8zCbxGDsqVdmbQBkPAkEAv8Sv4GtdXCucN0GsZcRhvOmGhNkhQU7W3qkPqLaAvBzfCzT/Kty4YEWTlF+sCP1+/Chl7AHf4FQ+3ivNkftoAwJAEZ0YRJQ+okY/gsPcQnllQEuXNdEZw7VtQUjCxMxUvpgIEVcnmobX7VAF0YJ+GmfymWY+36FQNaygCunUbCYxDwJAQaKxS8+Tmbt3cVYyCnbnuP/4wbmLb03rrzQQHv+wGjKLiMtv1pzLInBN7ce9Gyqgbu/oypltpdtP1T0K1D9HPwJBAKskq4+amIGnJ7FxGiPXAi0+Y96QPbAR/WjXiIaLRvwRa4Jwy8U6E6HHfYYTeuuB7h1ga6kyzfB7nUeGyeWSSkI=
     * 
     * convert private key to base64 pkcs1 format
     * @param privateKey
     * @return pkcs1  encoded base64 pkcs1 format private key
     */
    public static String toPkcs1PrivateKey(RSAPrivateKey privateKey) {
        PrivateKeyInfo privKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        try {
            byte[] bytes = privKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse private key from pkcs1 format
     * @param pkcs1PrivateKey
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromPkcs1PrivateKey(String pkcs1PrivateKey) {
        ASN1EncodableVector v1 = new ASN1EncodableVector();
        v1.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
        v1.add(DERNull.INSTANCE);

        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(new ASN1Integer(0));
        v2.add(new DERSequence(v1));
        v2.add(new DEROctetString(Base64.getDecoder().decode(pkcs1PrivateKey)));
        ASN1Sequence seq = new DERSequence(v2);
        try {
            //return fromPkcs8PrivateKey(seq.getEncoded(ASN1Sequence.DER)); // bcmail-jdk16
            return fromPkcs8PrivateKey(Base64.getEncoder().encodeToString(seq.getEncoded())); // bcmail-jdk15on
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PRIVATE KEY PKCS1 PEM FORMAT-----------------------------------
    /**
     * -----BEGIN RSA PRIVATE KEY-----
     * MIICXAIBAAKBgQCo20qAU4iyZIInpu2XzNXYHhFv6FVC/N1vsfz4ZrwX3VQaFsXf
     * 720QBkuP34Y31jy/6B+OB7DzklDBTnJXltCX2XdHyBY5WQYMX9rsQrfbvUL47u67
     * 6FD1T8o1/e+cEOGS75mKQIQjyt1zCZOl26Hy6x4TPeBSdVzFNYSr7KNjLQIDAQAB
     * AoGALFd51v0YtpACRdtmJSjbNyeeOJ7wVOkGVWCOJ8UCu9mZTkiQqd+76itdCGkQ
     * W/VceqDAOJH4e93+auTozeuC1w/srrUuPASUsE/5VLwPBvR90kToC28B59wAdl31
     * nD0KM8COq/9EdrkVkz6XO7KAik9gr3PLHCXu4i7tzf9djlkCQQDhagX7hsjJZ554
     * Pr0uBhXHwMmhiLPOK1b3884Wc1rHTMShVGF3DJH6stJV5hXwzjXBwSA8zCbxGDsq
     * VdmbQBkPAkEAv8Sv4GtdXCucN0GsZcRhvOmGhNkhQU7W3qkPqLaAvBzfCzT/Kty4
     * YEWTlF+sCP1+/Chl7AHf4FQ+3ivNkftoAwJAEZ0YRJQ+okY/gsPcQnllQEuXNdEZ
     * w7VtQUjCxMxUvpgIEVcnmobX7VAF0YJ+GmfymWY+36FQNaygCunUbCYxDwJAQaKx
     * S8+Tmbt3cVYyCnbnuP/4wbmLb03rrzQQHv+wGjKLiMtv1pzLInBN7ce9Gyqgbu/o
     * ypltpdtP1T0K1D9HPwJBAKskq4+amIGnJ7FxGiPXAi0+Y96QPbAR/WjXiIaLRvwR
     * a4Jwy8U6E6HHfYYTeuuB7h1ga6kyzfB7nUeGyeWSSkI=
     * -----END RSA PRIVATE KEY-----
     * <p>
     * 
     * new PemObject("RSA PRIVATE KEY", toPkcs1Encode(privateKey))
     * 
     * convert private key to pem format
     * @param privateKey
     * @return  encoded base64 pkcs1 pem fromat private key
     */
    public static String toPkcs1PemPrivateKey(RSAPrivateKey privateKey) {
        try (StringWriter stringWriter = new StringWriter(); 
            JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        ) {
            pemWriter.writeObject(privateKey);
            pemWriter.flush();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse private key from pem format
     * @param pemPrivateKey  encoded pem format private key
     * @return
     */
    public static RSAPrivateKey fromPkcs1PemPrivateKey(String pemPrivateKey) {
        try (Reader reader = new StringReader(pemPrivateKey); 
            PEMParser pemParser = new PEMParser(reader);
        ) {
            PEMKeyPair keyPair = (PEMKeyPair) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(Providers.BC.get().getName());
            //PublicKey publicKey = converter.getPublicKey(keyPair.getPublicKeyInfo());
            return (RSAPrivateKey) converter.getPrivateKey(keyPair.getPrivateKeyInfo());
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PRIVATE KEY PKCS8 FORMAT-----------------------------------
    /**
     * MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKjbSoBTiLJkgiem7ZfM1dgeEW/oVUL83W+x/PhmvBfdVBoWxd/vbRAGS4/fhjfWPL/oH44HsPOSUMFOcleW0JfZd0fIFjlZBgxf2uxCt9u9Qvju7rvoUPVPyjX975wQ4ZLvmYpAhCPK3XMJk6XbofLrHhM94FJ1XMU1hKvso2MtAgMBAAECgYAsV3nW/Ri2kAJF22YlKNs3J544nvBU6QZVYI4nxQK72ZlOSJCp37vqK10IaRBb9Vx6oMA4kfh73f5q5OjN64LXD+yutS48BJSwT/lUvA8G9H3SROgLbwHn3AB2XfWcPQozwI6r/0R2uRWTPpc7soCKT2Cvc8scJe7iLu3N/12OWQJBAOFqBfuGyMlnnng+vS4GFcfAyaGIs84rVvfzzhZzWsdMxKFUYXcMkfqy0lXmFfDONcHBIDzMJvEYOypV2ZtAGQ8CQQC/xK/ga11cK5w3QaxlxGG86YaE2SFBTtbeqQ+otoC8HN8LNP8q3LhgRZOUX6wI/X78KGXsAd/gVD7eK82R+2gDAkARnRhElD6iRj+Cw9xCeWVAS5c10RnDtW1BSMLEzFS+mAgRVyeahtftUAXRgn4aZ/KZZj7foVA1rKAK6dRsJjEPAkBBorFLz5OZu3dxVjIKdue4//jBuYtvTeuvNBAe/7AaMouIy2/WnMsicE3tx70bKqBu7+jKmW2l20/VPQrUP0c/AkEAqySrj5qYgacnsXEaI9cCLT5j3pA9sBH9aNeIhotG/BFrgnDLxToTocd9hhN664HuHWBrqTLN8HudR4bJ5ZJKQg==
     * 
     * convert private key to pkcs8 format
     * @param privateKey
     * @return pkcs8      base64 pkcs8 format private key
     */
    public static String toPkcs8PrivateKey(RSAPrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * parse private key from pkcs8 format
     * @param pkcs8PrivateKey   encoded base64 pkcs8 fromat private key
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromPkcs8PrivateKey(String pkcs8PrivateKey) {
        byte[] bytes = Base64.getDecoder().decode(pkcs8PrivateKey);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PRIVATE KEY ENCRYPTED PKCS8 PEM FORMAT-----------------------------------
    /**
     * BEGIN ENCRYPTED PRIVATE KEY
     * 
     * convert private key to encrypted pem format
     * @param privateKey
     * @param password      encrypt password
     * @param outEncryptor  encrypt alg
     * @return
     */
    public static String toEncryptedPkcs8PemPrivateKey(RSAPrivateKey privateKey, String password, 
                                                       OutputEncryptor outEncryptor) {
        try ( StringWriter stringWriter = new StringWriter(); 
              JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        ) {
            PrivateKeyInfo privKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
            pemWriter.writeObject(new PKCS8Generator(privKeyInfo, outEncryptor));
            pemWriter.flush();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * -----BEGIN ENCRYPTED PRIVATE KEY-----
     * MIICrjAoBgoqhkiG9w0BDAEDMBoEFA4ymXPHyGS9n5BRIibZHRkJ+idqAgIEAASC
     * AoA5TutO4A/F9MX4h3278DBIihEEetPGU7GxbmCySVsJraL8tXueMEZrLSDWC9rl
     * StJR82Umv0H8fpiMKlzYyHQjmqclY7e367+tQ87EqEenZNCCC1uveYLfBM7kZ+4/
     * m276IY6sNuJqYp3k8RrIBfYG9KCCQ7ywWiORuKvGbNAytFW9H+Z9JAyO5E70ysWe
     * pvfeLCFbRgJ0BOkm1aSyk81MN/alZ9d6a3d/UJLnnV42u7dS++mONVi66y6gKoON
     * Y5xVX2ICPRGoIvZmXeeQYzH02XFpTn9+vQ//KG4iGErXjNaWLSWpLyCY2qHQWwQ1
     * YB43aX7xYOiKUN24PMiwGKwjhBaKzakMt4lcmGNd6ZcwPC+ghhkmpPcCu4gSabQk
     * Etv5tmkChBLIjRgxmmnlEYLDl68e8vth5RquJvwB4zBOQkDo9tPcwWnOk4vbvGJP
     * w1WXfwHU4X4oy+FOiOTe7+lOTN6CeXxfx8a91h5zS1tA16bQLAgTA7oJGPD3yHpF
     * aBYXPNzwIOpAUkEqCSbZuuYZ5uidjrm7rV8nSjXu0fkYxzpXbQAps+NBtlKHFXNC
     * JWvj2g6UFtk/RoT4ghgtTx11DZUh+GsKbCjLM52omDDNLK8K+GOEFzElWiZIEWfh
     * yoUAN9KuuCprC3RsqV4K70nQewuiX5NBt9xYcaKVBQ5jRgL0xnVpquyFeFXrY0Ge
     * EDgOZTSUVVxlbNQ+iwBb52cD2cFmPcIszSNpQ85cS8eISdYiwaW42yUa7LYpO98S
     * jyTNnLzOMRt04gcBcp71EOWEheE9Ui6AqweSA6LUHulSbOK4+4oKtdKH5KjdcWYI
     * WDgTEoIGOC3se36z3v5Mlr8h
     * -----END ENCRYPTED PRIVATE KEY-----
     * <p>
     * 
     * convert private key to encrypted pem format
     * normal pbeWithSHAAnd3_KeyTripleDES_CBC to encrypt
     * @param privateKey
     * @param password
     * @return
     */
    public static String toEncryptedPkcs8PemPrivateKey(RSAPrivateKey privateKey, String password) {
        try {
            ASN1ObjectIdentifier s = PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC;
            JcePKCSPBEOutputEncryptorBuilder encryptorBuilder = new JcePKCSPBEOutputEncryptorBuilder(s);
            return toEncryptedPkcs8PemPrivateKey(privateKey, password, encryptorBuilder.build(password.toCharArray()));
        } catch (OperatorCreationException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse private key from encrypted pem format
     * @param encryptedPem  encoded pem encrypted private key
     * @param password
     * @return
     */
    public static RSAPrivateKey fromEncryptedPkcs8PemPrivateKey(String encryptedPem, String password) {
        try ( Reader reader = new StringReader(encryptedPem); 
              PEMParser pemParser = new PEMParser(reader);
        ) {
            PKCS8EncryptedPrivateKeyInfo encrypted = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
            JcePKCSPBEInputDecryptorProviderBuilder decryptorBuilder = new JcePKCSPBEInputDecryptorProviderBuilder();
            PrivateKeyInfo pkInfo = encrypted.decryptPrivateKeyInfo(decryptorBuilder.build(password.toCharArray()));
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(Providers.BC.get().getName());
            return (RSAPrivateKey) converter.getPrivateKey(pkInfo);
        } catch (IOException | PKCSException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------EXTRACT PUBLIC KEY FROM PRIVATE KEY-----------------------------------
    /**
     * extract public key from private key
     * @param privateKey
     * @return
     */
    public static RSAPublicKey extractPublicKey(RSAPrivateKey privateKey) {
        try {
            RSAPrivateCrtKey key = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
            return (RSAPublicKey) KeyFactory.getInstance(ALG_RSA).generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PUBLIC KEY PKCS1 FORMAT-----------------------------------
    /**
     * MIGJAoGBAKVpbo/Wum3G5ciustuKNGvPX/rgkdZw33QGqBR5UOKUoD5/h/IeQlS7ladX+oa+ciVCXyP854Zq+0RVQ7x87DfAohLmyXlIGOJ7KLJZkUWDYSG0WsPbnTOEmxQcRzqEV5g9pVHIjgPH6N/j6HHKRs5xDEd3pVpoRBZKEncbZ85xAgMBAAE=
     * <p>
     * 
     * The RSA Public key PEM file is specific for RSA keys.<p>
     * convert public key to pkcs1 format<p>
     * @param publicKey
     * @return
     */
    public static String toPkcs1PublicKey(RSAPublicKey publicKey) {
        try {
            SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
            return Base64.getEncoder().encodeToString(spkInfo.parsePublicKey().getEncoded());
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse public key from pkcs1 format
     * @param pkcs1PublicKey  encoded base64 pkcs1 public key
     * @return
     */
    public static RSAPublicKey fromPkcs1PublicKey(String pkcs1PublicKey) {
        byte[] bytes = Base64.getDecoder().decode(pkcs1PublicKey);
        try {
            org.bouncycastle.asn1.pkcs.RSAPublicKey pk = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(bytes);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(pk.getModulus(), pk.getPublicExponent());
            return (RSAPublicKey) KeyFactory.getInstance(ALG_RSA).generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PUBLIC KEY X509 PKCS8 FORMAT-----------------------------------
    /**
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClaW6P1rptxuXIrrLbijRrz1/64JHWcN90BqgUeVDilKA+f4fyHkJUu5WnV/qGvnIlQl8j/OeGavtEVUO8fOw3wKIS5sl5SBjieyiyWZFFg2EhtFrD250zhJsUHEc6hFeYPaVRyI4Dx+jf4+hxykbOcQxHd6VaaEQWShJ3G2fOcQIDAQAB
     * 
     * convert public key to x509 pkcs8 fromat
     * @param publicKey
     * @return
     */
    public static String toPkcs8PublicKey(RSAPublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * parse public key from base64 X509 pkcs8 fromat
     * @param pkcs8PublicKey  encoded base64 x509 pkcs8 fromat
     * @return RSAPublicKey
     */
    public static RSAPublicKey fromPkcs8PublicKey(String pkcs8PublicKey) {
        byte[] keyBytes = Base64.getDecoder().decode(pkcs8PublicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
            return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PUBLIC KEY X509 PKCS8 PEM FORMAT-----------------------------------
    /**
     * -----BEGIN PUBLIC KEY-----
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClaW6P1rptxuXIrrLbijRrz1/6
     * 4JHWcN90BqgUeVDilKA+f4fyHkJUu5WnV/qGvnIlQl8j/OeGavtEVUO8fOw3wKIS
     * 5sl5SBjieyiyWZFFg2EhtFrD250zhJsUHEc6hFeYPaVRyI4Dx+jf4+hxykbOcQxH
     * d6VaaEQWShJ3G2fOcQIDAQAB
     * -----END PUBLIC KEY-----
     * <p>
     * 
     * new PemObject("RSA PUBLIC KEY", toPkcs8Encode(publicKey))
     * 
     * convert public key to pem fromat (pkcs8)
     * @param publicKey
     * @return
     */
    public static String toPkcs8PemPublicKey(RSAPublicKey publicKey) {
        try (StringWriter stringWriter = new StringWriter(); 
            JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        ) {
            pemWriter.writeObject(publicKey);
            pemWriter.flush();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse public key from pem format
     * @param pemPublicKey  encoded pem public key
     * @return
     */
    public static RSAPublicKey fromPkcs8PemPublicKey(String pemPublicKey) {
        try (Reader reader = new StringReader(pemPublicKey); 
             PEMParser pemParser = new PEMParser(reader);
        ) {
            SubjectPublicKeyInfo subPkInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            RSAKeyParameters param = (RSAKeyParameters) PublicKeyFactory.createKey(subPkInfo);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(param.getModulus(), param.getExponent());
            return (RSAPublicKey) KeyFactory.getInstance(ALG_RSA).generatePublic(keySpec);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // ---------------------------------------sign/verify---------------------------------------
    /**
     * SHA1签名
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signSha1(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithm.SHA1withRSA);
    }

    /**
     * MD5签名
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signMd5(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithm.MD5withRSA);
    }

    /**
     * SHA1
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifySha1(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithm.SHA1withRSA);
    }

    /**
     * MD5验签
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifyMd5(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithm.MD5withRSA);
    }

    /**
     * <pre>
     *   1、可以通过修改生成密钥的长度来调整密文长度
     *   2、不管明文长度是多少，RSA生成的密文长度总是固定的
     *   3、明文长度不能超过密钥长度：
     *     1）sun jdk默认的RSA加密实现不允许明文长度超过密钥长度减去11字节（byte）：比如1024位（bit）的密钥，则待加密的明文最长为1024/8-11=117（byte）
     *     2）BouncyCastle提供的加密算法能够支持到的RSA明文长度最长为密钥长度
     *   4、每次生成的密文都不一致证明加密算法安全：这是因为在加密前使用RSA/None/PKCS1Padding对明文信息进行了随机数填充
     *   5、javax.crypto.Cipher是线程不安全的
     * </pre>
     * 
     * 大数据分块加密
     * @param data 源数据
     * @param Key
     * @return
     */
    public static <T extends Key & RSAKey> byte[] encrypt(byte[] data, T key) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int inputLen = data.length, modLen = key.getModulus().bitLength() / 8 - 11;
            byte[] block;
            for (int len, i = 0, offSet = 0; inputLen - offSet > 0; i++, offSet = i * modLen) {
                // 对数据分段加密
                len = (inputLen - offSet > modLen) ? modLen : inputLen - offSet;
                block = cipher.doFinal(data, offSet, len);
                out.write(block, 0, block.length);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 大数据分块解密
     * @param encrypted
     * @param key
     * @return
     */
    public static <T extends Key & RSAKey> byte[] decrypt(byte[] encrypted, T key) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int inputLen = encrypted.length, modLen = key.getModulus().bitLength() / 8;
            byte[] block;
            for (int len, i = 0, offSet = 0; inputLen - offSet > 0; i++, offSet = i * modLen) {
                // 对数据分段解密
                len = (inputLen - offSet > modLen) ? modLen : inputLen - offSet;
                block = cipher.doFinal(encrypted, offSet, len);
                out.write(block, 0, block.length);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // -------------------------------private methods-------------------------------
    /**
     * 数据签名
     * @param data
     * @param privateKey
     * @param algId
     * @return
     */
    private static byte[] sign(byte[] data, RSAPrivateKey privateKey, RSASignAlgorithm alg) {
        try {
            Signature signature = Signature.getInstance(alg.name());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验签
     * @param data
     * @param publicKey
     * @param signed
     * @param algId
     * @return
     */
    private static boolean verify(byte[] data, RSAPublicKey publicKey, byte[] signed, RSASignAlgorithm alg) {
        try {
            Signature signature = Signature.getInstance(alg.name());
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signed);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * RSA密钥对
     */
    public static final class RSAKeyPair implements Serializable {
        private static final long serialVersionUID = -1592700389671199076L;
        private final RSAPrivateKey privateKey;
        private final RSAPublicKey publicKey;

        private RSAKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        public String getPkcs8PrivateKey() {
            return toPkcs8PrivateKey(privateKey);
        }

        public String getPkcs1PrivateKey() {
            return toPkcs1PrivateKey(privateKey);
        }

        public String getPkcs8PublicKey() {
            return toPkcs8PublicKey(publicKey);
        }

        public String getPkcs1PublicKey() {
            return toPkcs1PublicKey(publicKey);
        }
    }

    public static void main(String[] args) throws Exception {
        RSAKeyPair keyPair = genRSAKeyPair(1024);
        test(keyPair.getPrivateKey(), extractPublicKey(keyPair.getPrivateKey()));
        
        test(fromPkcs1PemPrivateKey(toPkcs1PemPrivateKey(fromPkcs1PrivateKey(keyPair.getPkcs1PrivateKey()))),
             fromPkcs8PemPublicKey(toPkcs8PemPublicKey(fromPkcs1PublicKey(keyPair.getPkcs1PublicKey()))));
        
        test(fromPkcs1PrivateKey(toPkcs1PrivateKey(keyPair.getPrivateKey())),
             fromPkcs1PublicKey(keyPair.getPkcs1PublicKey()));

        test(fromPkcs8PrivateKey(keyPair.getPkcs8PrivateKey()),
             fromPkcs8PublicKey(keyPair.getPkcs8PublicKey()));

        System.out.println(fromEncryptedPkcs8PemPrivateKey(toEncryptedPkcs8PemPrivateKey(keyPair.getPrivateKey(),"123"), "123"));

        System.out.println(toPkcs1PrivateKey(keyPair.getPrivateKey()));
        System.out.println(toPkcs8PrivateKey(keyPair.getPrivateKey()));
        System.out.println(toPkcs1PemPrivateKey(keyPair.getPrivateKey()));
        System.out.println(toEncryptedPkcs8PemPrivateKey(keyPair.getPrivateKey(), "1234"));
    }

    private  static void test(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws IOException {
        long i = System.currentTimeMillis();
        System.out.println("=============================加密测试==============================");
        //byte[] data = "加解密测试".getBytes();
        byte[] data = Streams.file2bytes(MavenProjects.getMainJavaFile(RSACryptor.class));
        System.out.println("原文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(data, 0, 100)));
        byte[] encodedData = RSACryptor.encrypt(data, publicKey);
        System.out.println("密文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(encodedData, 0, 100)));
        System.out.println("解密：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, privateKey), 0, 100)));

        System.out.println("===========================签名测试=========================");
        data = Base64.getDecoder().decode("");
        byte[] signed = RSACryptor.signSha1(data, privateKey);
        System.out.println("签名数据：len->" + signed.length + " ， b64->" + Base64.getEncoder().encodeToString(signed));
        System.out.println("验签结果：" + RSACryptor.verifySha1(data, publicKey, signed));

        System.out.println("cost time: " + (System.currentTimeMillis() - i));
    }
}
