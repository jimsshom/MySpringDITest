package com.jimsshom.springditest.component;

/**
 * User: xiaohe.yz
 * Date: 16/6/9
 * Time: 16:25
 */
public class TestClass {
    private Integer value = 10;

    private A a;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestClass{");
        sb.append("value=").append(value);
        sb.append(", a=").append(a);
        sb.append('}');
        return sb.toString();
    }
}
