package code.ponfee.commons.cache;

/**
 * 时间服务提供
 * @author fupf
 */
interface DateProvider {

    DateProvider SYSTEM = System::currentTimeMillis;

    long now();

}
