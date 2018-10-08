/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import java.lang.reflect.Field;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 
 * @author Ponfee
 */
public class TestSerialize {

    public static enum E {
        A, B
    }

    public TestSerialize(byte a, Byte b, int c, Integer d, float f, Float g, E h) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.f = f;
        this.g = g;
        this.h = h;
    }

    private byte a;
    private Byte b;
    private int c;
    private Integer d;
    private float f;
    private Float g;
    private E h;

    public byte getA() {
        return a;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public Byte getB() {
        return b;
    }

    public void setB(Byte b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public Integer getD() {
        return d;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public Float getG() {
        return g;
    }

    public void setG(Float g) {
        this.g = g;
    }

    public E getH() {
        return h;
    }

    public void setH(E h) {
        this.h = h;
    }

    @Override
    public String toString() {
        return "TestSerialize [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", f=" + f + ", g=" + g + ", h=" + h + "]";
    }

    public static void main(String[] args) {
        TestSerialize source = new TestSerialize((byte) 12, (Byte) (byte) 23, 34, 45, 56f, 67f, E.A);
        System.out.println(source);
        TestSerialize target = new TestSerialize((byte) 0, null, 1, 2, 3, null, E.B);
        System.out.println(target);

        for (Field field : ClassUtils.listFields(TestSerialize.class)) {
            byte[] value = ObjectUtils.serialize(Fields.get(source, field), field.getType());
            Fields.put(target, field, ObjectUtils.deserialize(value, field.getType()));
        }
        System.out.println(target);

        for (Field field : ClassUtils.listFields(TestSerialize.class)) {
            Fields.put(target, field, ObjectUtils.deserialize(null, field.getType()));
        }
        System.out.println(target);
    }
}
