package com.jimsshom.springditest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: xiaohe.yz
 * Date: 16/6/9
 * Time: 16:27
 */
public class MyJsonDIContext {

    private static Map<String, Object> beanMapById = new ConcurrentHashMap<String, Object>();

    public static Object getBeanById(String id) {
        return beanMapById.get(id);
    }

    public static void addContextByFilePath(String filePath) throws Exception {
        String jsonText = readContextFile(filePath);
        initContextByJsonTest(jsonText);
    }

    private static String readContextFile(String filePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(filePath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static void initContextByJsonTest(String jsonContextText)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException {
        JSONArray beansArray = JSON.parseArray(jsonContextText);
        for (Object bean : beansArray) {
            Map beanDefinition = (Map) bean;
            String id = ((String) beanDefinition.get("id"));
            String className = ((String) beanDefinition.get("class"));
            Map properties = new HashMap();
            if (beanDefinition.containsKey("properties")) {
                properties = ((Map) beanDefinition.get("properties"));
            }

            Object instance = newInstance(className, properties);
            beanMapById.put(id, instance);
        }
    }

    private static Object newInstance(String className, Map properties)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException {
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(className);
        Object obj = aClass.newInstance();

        for (Object entry : properties.entrySet()) {
            Map.Entry<String, Object> objectEntry = (Map.Entry<String, Object>) entry;
            String key = objectEntry.getKey();
            Object value = objectEntry.getValue();
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase("set" + key)) {
                    method.invoke(obj, value);
                }
            }
        }
        return obj;
    }
}
