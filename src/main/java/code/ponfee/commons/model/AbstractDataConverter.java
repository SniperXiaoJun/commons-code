package code.ponfee.commons.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import code.ponfee.commons.util.ObjectUtils;

import static code.ponfee.commons.reflect.GenericUtils.getActualTypeArgument;

/**
 * Converts model to the data transfer object
 * 
 * @param <F> from(source)
 * @param <T> to  (target)
 * 
 * @author Ponfee
 */
public abstract class AbstractDataConverter<F, T> implements Function<F, T> {

    @SuppressWarnings("unchecked")
    public T convert(F from) {
        if (from == null) {
            return null;
        }
        return convert(from, (Class<T>) getActualTypeArgument(this.getClass(), 1));
    }

    public final List<T> convert(List<F> list) {
        if (list == null) {
            return null;
        }

        return list.stream().map(this).collect(Collectors.toList());
    }

    public final Page<T> convert(Page<F> page) {
        if (page == null) {
            return null;
        }

        return page.transform(this);
    }

    public final Result<T> convertResultBean(Result<F> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    public final Result<List<T>> convertResultList(Result<List<F>> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    public final Result<Page<T>> convertResultPage(Result<Page<F>> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    // ----------------------------------------------other methods
    public @Override final T apply(F from) {
        return this.convert(from);
    }

    // -----------------------------------------------static methods
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, F> T convert(F from, Class<T> type) {
        if (from == null || type.isInstance(from)) {
            return (T) from;
        }

        T to;
        try {
            to = type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (Map.class.isAssignableFrom(type) && Map.class.isInstance(from)) {
            ((Map) to).putAll((Map<?, ?>) from);
        } else if (Map.class.isAssignableFrom(type)) {
            ((Map) to).putAll(ObjectUtils.bean2map(from));
        } else if (Map.class.isInstance(from)) {
            ObjectUtils.map2bean((Map) from, to);
        } else {
            org.springframework.beans.BeanUtils.copyProperties(from, to);
            //org.apache.commons.beanutils.BeanUtils.copyProperties(to, from);
            //org.apache.commons.beanutils.PropertyUtils.copyProperties(to, from);
            //org.springframework.cglib.beans.BeanCopier.create(source, target, false);
        }
        return to;
    }

    public static <F, T> T convert(F from, Function<F, T> converter) {
        if (from == null) {
            return null;
        }
        return converter.apply(from);
    }

    public static <F, T> List<T> convert(
        List<F> list, Function<F, T> converter) {
        if (list == null) {
            return null;
        }
        return list.stream().map(converter).collect(Collectors.toList());
    }

    public static <F, T> Page<T> convert(
        Page<F> page, Function<F, T> converter) {
        if (page == null) {
            return null;
        }
        return page.transform(converter);
    }

    public static <F, T> Result<T> convertResultBean(
        Result<F> result, Function<F, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(converter.apply(result.getData()));
    }

    public static <F, T> Result<List<T>> convertResultList(
        Result<List<F>> result, Function<F, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

    public static <F, T> Result<Page<T>> convertResultPage(
        Result<Page<F>> result, Function<F, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

}
