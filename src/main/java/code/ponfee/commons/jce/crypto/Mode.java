package code.ponfee.commons.jce.crypto;

/**
 * 对称加密分组模式<p>
 * 推荐使用CBC和CTR模式<p>
 * CFB,OFB,CTR模式不需要padding<p>
 * @author fupf
 */
public enum Mode {
    ECB, CBC, CFB, OFB, CTR/*, None*/;
}
