package com.jimsshom.springditest.component;

/**
 * User: xiaohe.yz
 * Date: 16/6/9
 * Time: 18:16
 */
public class A {
    private String name;
    private TestClass testClass;
    private Integer intNum;
    private Double doubleNum;
    private Float floatNum;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestClass getTestClass() {
        return testClass;
    }

    public void setTestClass(TestClass testClass) {
        this.testClass = testClass;
    }

    public Integer getIntNum() {
        return intNum;
    }

    public void setIntNum(Integer intNum) {
        this.intNum = intNum;
    }

    public Double getDoubleNum() {
        return doubleNum;
    }

    public void setDoubleNum(Double doubleNum) {
        this.doubleNum = doubleNum;
    }

    public Float getFloatNum() {
        return floatNum;
    }

    public void setFloatNum(Float floatNum) {
        this.floatNum = floatNum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("A{");
        sb.append("name='").append(name).append('\'');
        sb.append(", testClass=").append(testClass);
        sb.append(", intNum=").append(intNum);
        sb.append(", doubleNum=").append(doubleNum);
        sb.append(", floatNum=").append(floatNum);
        sb.append('}');
        return sb.toString();
    }
}
