package com.jimsshom.springditest;

import com.jimsshom.springditest.component.TestClass;

/**
 * User: xiaohe.yz
 * Date: 16/6/9
 * Time: 16:22
 */
public class Application {
    public static void main(String[] args) throws Exception {
        MyJsonDIContext.addContextByFilePath("resources/application-context.json");
        TestClass testClass = (TestClass) MyJsonDIContext.getBeanById("testClass");
        System.out.println(testClass);

        testClass = (TestClass) MyJsonDIContext.getBeanById("class2");
        System.out.println(testClass);
    }
}
