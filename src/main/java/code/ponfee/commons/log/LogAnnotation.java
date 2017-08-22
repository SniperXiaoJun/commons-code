package code.ponfee.commons.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果是日志入库，则无法用在service的只读事务方法上
 * 需要新开启嵌套的事务
 * <p>
 * 
 * 日志注解
 * @author fupf
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {

    LogType type() default LogType.UNDEFINED;

    boolean limit() default false; // 是否限制频率

    String desc() default "";

    enum LogType {
        UNDEFINED(0x0, null), ADD(0x1, "新增"), UPDATE(0x2, "更新"), 
        DELETE(0x3, "删除"), QUERY(0x4, "查询");

        private int type;

        private String comment;

        LogType(int type, String comment) {
            this.type = type;
            this.comment = comment;
        }

        public String comment() {
            return comment;
        }

        public int type() {
            return type;
        }
    }

}
