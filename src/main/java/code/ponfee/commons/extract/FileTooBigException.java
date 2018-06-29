package code.ponfee.commons.extract;

/**
 * The file too big exception
 * 
 * @author Ponfee
 */
public class FileTooBigException extends Exception {

    private static final long serialVersionUID = 2154768302918164161L;

    public FileTooBigException() {
        super();
    }

    public FileTooBigException(String message) {
        super(message);
    }

    public FileTooBigException(Throwable cause) {
        super(cause);
    }

    public FileTooBigException(String message, Throwable cause) {
        super(message, cause);
    }
}
