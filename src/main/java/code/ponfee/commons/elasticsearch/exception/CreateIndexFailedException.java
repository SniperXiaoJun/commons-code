package code.ponfee.commons.elasticsearch.exception;

public class CreateIndexFailedException extends RuntimeException {
    private static final long serialVersionUID = 4096428870265263132L;

    public CreateIndexFailedException(String indexName, Throwable cause) {
        super(String.format("Creating Index '%s' failed", indexName), cause);
    }
}
