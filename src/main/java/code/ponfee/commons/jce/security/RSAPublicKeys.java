package code.ponfee.commons.jce.security;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * PKCS#1格式：BEGIN RSA PRIVATE KEY
 * PKCS#8格式：BEGIN PRIVATE KEY
 * PKCS#8加密：BEGIN ENCRYPTED PRIVATE KEY
 * 
 * RSA Public Key convert
 * @author fupf
 */
public final class RSAPublicKeys {
    private RSAPublicKeys() {}

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
    public static String toPkcs1(RSAPublicKey publicKey) {
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
    public static RSAPublicKey fromPkcs1(String pkcs1PublicKey) {
        byte[] bytes = Base64.getDecoder().decode(pkcs1PublicKey);
        try {
            org.bouncycastle.asn1.pkcs.RSAPublicKey pk = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(bytes);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(pk.getModulus(), pk.getPublicExponent());
            return (RSAPublicKey) KeyFactory.getInstance(RSACryptor.ALG_RSA).generatePublic(keySpec);
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
    public static String toPkcs8(RSAPublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * parse public key from base64 X509 pkcs8 fromat
     * @param pkcs8PublicKey  encoded base64 x509 pkcs8 fromat
     * @return RSAPublicKey
     */
    public static RSAPublicKey fromPkcs8(String pkcs8PublicKey) {
        byte[] keyBytes = Base64.getDecoder().decode(pkcs8PublicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSACryptor.ALG_RSA);
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
    public static String toPkcs8Pem(RSAPublicKey publicKey) {
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
    public static RSAPublicKey fromPkcs8Pem(String pemPublicKey) {
        try (Reader reader = new StringReader(pemPublicKey); 
             PEMParser pemParser = new PEMParser(reader);
        ) {
            SubjectPublicKeyInfo subPkInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            RSAKeyParameters param = (RSAKeyParameters) PublicKeyFactory.createKey(subPkInfo);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(param.getModulus(), param.getExponent());
            return (RSAPublicKey) KeyFactory.getInstance(RSACryptor.ALG_RSA).generatePublic(keySpec);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}
