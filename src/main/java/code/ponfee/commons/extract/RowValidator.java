package code.ponfee.commons.extract;

/**
 * 行验证
 * 
 * @author Ponfee
 * @param <T>
 */
@FunctionalInterface
public interface RowValidator<T> {

    String verify(int rowNumber, T rowData);
}
