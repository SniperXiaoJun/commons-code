package code.ponfee.commons.cache;

/**
 * 时间服务提供
 * @author fupf
 */
@FunctionalInterface
interface DateProvider {

    DateProvider SYSTEM = System::currentTimeMillis;

    long now();

}
