package code.ponfee.commons.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * <pre>
 * ContextLoaderListener的beanfactory是DispatcherServlet的parent
 * spring上下文无法访问spring mvc上下文，但spring mvc上下文却能访问spring上下文
 *   解决方案1：在DispatcherServlet配置bean aware，如<bean id="bean" class="xxx.BeanImpl"/>
 *   解决方案2：Set<ApplicationContext>
 * </pre>
 * 
 * spring上下文持有类
 * @author fupf
 */
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

    private static final Set<ApplicationContext> HOLDER = new HashSet<>();

    @Override
    public void setApplicationContext(ApplicationContext c) throws BeansException {
        HOLDER.add(c);
    }

    @Override
    public void destroy() throws Exception {
        HOLDER.clear();
    }

    /**
     * 通过名称获取bean
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        assertContextInjected();
        BeansException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                Object bean = c.getBean(name);
                if (bean != null) return bean;
            } catch (BeansException e) {
                ex = e;
            }
        }
        if (ex == null) return null;
        else throw ex;
    }

    /**
     * 通过类获取bean
     * @param clszz
     * @return
     */
    public static <T> T getBean(Class<T> clszz) {
        assertContextInjected();
        BeansException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                T bean = c.getBean(clszz);
                if (bean != null) return bean;
            } catch (BeansException e) {
                ex = e;
            }
        }
        if (ex == null) return null;
        else throw ex;
    }

    /**
     * @param name
     * @param clszz
     * @return
     */
    public static <T> T getBean(String name, Class<T> clszz) {
        assertContextInjected();
        BeansException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                T bean = c.getBean(name, clszz);
                if (bean != null) {
                    return bean;
                }
            } catch (BeansException e) {
                ex = e;
            }
        }
        if (ex == null) return null;
        else throw ex;
    }

    /**
     * 判断是否含有该名称的Bean
     * @param name
     * @return
     */
    public static boolean containsBean(String name) {
        assertContextInjected();
        for (ApplicationContext c : HOLDER) {
            if (c.containsBean(name)) return true;
        }
        return false;
    }

    /**
     * 判断Bean是否单例
     * @param name
     * @return
     */
    public static boolean isSingleton(String name) {
        assertContextInjected();
        NoSuchBeanDefinitionException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                if (c.isSingleton(name)) return true;
            } catch (NoSuchBeanDefinitionException e) {
                ex = e;
            }
        }
        if (ex == null) return false;
        else throw ex;
    }

    /**
     * 获取Bean的类型
     * @param name
     * @return
     */
    public static Class<?> getType(String name) {
        assertContextInjected();
        NoSuchBeanDefinitionException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                if (c.getType(name) != null) {
                    return c.getType(name);
                }
            } catch (NoSuchBeanDefinitionException e) {
                ex = e;
            }
        }
        if (ex == null) return null;
        else throw ex;
    }

    /**
     * 获取bean的别名
     * @param name
     * @return
     */
    public static String[] getAliases(String name) {
        assertContextInjected();
        for (ApplicationContext c : HOLDER) {
            String[] aliases = c.getAliases(name);
            if (aliases != null) return aliases;
        }
        return null;
    }

    /**
     * 检查ApplicationContext不为空.
     */
    private static void assertContextInjected() {
        Assert.state(HOLDER.size() > 0, "must be defined SpringContextHolder within spring config file.");
    }
}
