/**
 * Java Cryptography Extension提供用于加密、密钥生成和协商
 * 以及 Message Authentication Code（MAC）算法的实现<p>
 * 
 * 1、密码：
 *   你知道什么：口令（密码）、口令摘要、质询/响应
 *   你有什么：认证令牌（质询/响应令牌、时间令牌），PIN双因素认证、SSL与认证令牌、智能卡
 *   你是什么：生物特征认证，FAR(False Accept Ratio)，FRR(False Reject Ratio)
 * 
 * 2、对称加密：
 *   优点：效率高、紧凑型
 *   缺点：密钥成几何数增长、需要事先协商密钥
 *   类型：分组密码（DES、3DES、AES），序列密码（RC4）、盐加密（PBE）
 *   分组模式：ECB、CBC、OFB、CFB
 *   填充：NoPadding, PKCS5Padding, PKCS7Padding, PADDING_ISO10126
 * 
 * 3、非对称加密：
 *   优点：密钥分发安全，公开公钥即可
 *   缺点：效率低、杂凑型
 *   算法：
 *      DH：基于离散对数的实现，主要用于密钥交换
 *      RSA：基于大整数分解的实现，Ron Rivest, Adi Shamir, Leonard Adleman（三人）
 *      DSA：基于整数有限域离散对数难题（特点是两个素数公开），Digital Signature Algorithm，顾名思义只用于数字签名
 *      ECC：基于椭圆曲线算法，指在取代RSA
 *   填充：RSA_PKCS1_PADDING（ blocklen=keysize/8–11）、RSA_PKCS1_OAEP_PADDING(keysize-41)、RSA_NO_PADDING
 *   签名/验签：PKCS1及填充、PKCS7格式（附原文|不附原文）
 * 
 * 4、对称与非对称结合：数字信封envelop，结构体，（带签名|不带签名）
 * 
 * 5、数字证书：ASN1、X509、p7b、p7r、p10、p12、PEM、DER等概念
 * 
 * 6、BASE64编码：3个字节切分为4个字节后每字节最高位补00  1 ~ 2^7-1 + ”=“，并与编码表对照
 *               前生：解决邮件只能发ASCII码问题
 *               应用：字节流数据文本化（某些场景的网络传输及文本表示）
 * 
 * 7、哈希算法：指纹、摘要，用于防篡改等
 *    MD5：前身MD2、MD3和MD4，安全性低，算法原理（填充、记录长度、数据处理）
 *    SHA-1：已被严重质疑
 *    SHA-2：SHA-224、SHA-256、SHA-384、SHA-512，算法跟SHA-1基本上仍然相似
 *    SHA-3：之前名为Keccak算法，是一个加密杂凑算法
 *    RIPEMD-160：哈希加密算法，用于替代128 位哈希函数 MD4、MD5 和 RIPEMD
 */
package code.ponfee.commons.jce;
