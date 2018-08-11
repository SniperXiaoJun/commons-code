package code.ponfee.commons.extract;

/**
 * 行数据校验
 * 
 * @author Ponfee
 * @param <T>
 */
@FunctionalInterface
public interface RowValidator<T> {

    String verify(int rowNumber, T rowData);
}
