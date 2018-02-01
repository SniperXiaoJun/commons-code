package code.ponfee.commons.jce.ecc;

public class NotOnMotherException extends Exception {

    private static final long serialVersionUID = -4784996763190735382L;

    private ECPoint sender;

    public NotOnMotherException(ECPoint sender) {
        this.sender = sender;
    }

    public String getErrorString() {
        return "NotOnMother";
    }

    public ECPoint getSource() {
        return sender;
    }
}
