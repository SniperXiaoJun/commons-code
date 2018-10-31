package test.utils;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.cglib.beans.BeanCopier;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.CglibUtils;
import code.ponfee.commons.reflect.Fields;

public class TestBeanCopy {

    public static void main(String[] args) {
        Object obj = new Object();
        System.out.println(Fields.addressOf(obj));
        System.out.println(System.identityHashCode(obj));
    }
    
    static int round = 99999999;
    @Test @Ignore
    public void test1() {
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            org.springframework.beans.BeanUtils.copyProperties(result1, result2);
        }
    }
    
    @Test
    public void test2() {
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            CglibUtils.copyProperties(result1, result2);
        }
    }

    @Test
    public void test3() {
        BeanCopier copier = BeanCopier.create(Result.class, Result.class, false);
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            copier.copy(result1, result2, null);
        }
    }
}
