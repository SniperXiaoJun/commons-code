//package code.ponfee.commons.model;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.core.MethodParameter;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//
//import code.ponfee.commons.math.Numbers;
//import code.ponfee.commons.reflect.Fields;
//
///**
// * 分页查询方法参数解析
// * 
// * {@link https://blog.csdn.net/lqzkcx3/article/details/78794636}
// * 
// * @see org.springframework.web.method.support.HandlerMethodArgumentResolverComposite
// * @see org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver
// * 
// * 被注入到RequestMappingHandlerAdapter中的argumentResolvers字段
// * 
// * @author Ponfee
// */
//public class PageMethodArgumentResolver implements HandlerMethodArgumentResolver {
//
//    @Override
//    public boolean supportsParameter(MethodParameter parameter) {
//        //return parameter.hasParameterAnnotation(PageRequestParam.class);
//        return PageRequestParams.class == parameter.getParameterType();
//    }
//
//    @Override
//    public PageRequestParams resolveArgument(
//        MethodParameter parameter, ModelAndViewContainer mavContainer,
//        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
//
//        PageRequestParams page = new PageRequestParams();
//        webRequest.getParameterMap().entrySet().stream().forEach(entry -> {
//            if (PageRequestParams.PAGE_PARAMS.contains(entry.getKey())) {
//                int value = Numbers.toInt(entry.getValue()[0], 0);
//                Fields.put(page, entry.getKey(), value);
//                page.put(entry.getKey(), value);
//            } else if (PageRequestParams.SORT_PARAM.equalsIgnoreCase(entry.getKey())) {
//                // value：“name ASC, age DESC”
//                String value = StringUtils.join(entry.getValue(), ',');
//                Fields.put(page, PageRequestParams.SORT_PARAM, value);
//                page.put(PageRequestParams.SORT_PARAM, value);
//            } else {
//                String[] value = entry.getValue();
//                page.put(entry.getKey(), value.length == 1 ? value[0] : value);
//            }
//        });
//        return page;
//    }
//
//    //@Target(ElementType.PARAMETER)
//    //@Retention(RetentionPolicy.RUNTIME)
//    //@Documented
//    //public static @interface PageRequestParam {}
//
//}
