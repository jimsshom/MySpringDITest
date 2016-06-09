package com.jimsshom.springditest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: xiaohe.yz
 * Date: 16/6/9
 * Time: 16:27
 */
public class MyJsonDIContext {

    private static Map<String, Object> beanMapById = new ConcurrentHashMap<String, Object>();

    private static Map<Object, Map<String, String>> idTypePropertiesMap = new ConcurrentHashMap<Object, Map<String, String>>();
    private static Map<Object, List<Object[]>> initFuncListMap = new ConcurrentHashMap<Object, List<Object[]>>();

    public static Object getBeanById(String id) throws Exception {
        Object obj = beanMapById.get(id);
        injectIdTypeProperties(obj);
        callInitFuncList(obj);
        return obj;
    }

    private static void callInitFuncList(Object obj) throws InvocationTargetException, IllegalAccessException {
        if (obj == null || !initFuncListMap.containsKey(obj)) {
            return;
        }

        for (Object[] func : initFuncListMap.get(obj)) {
            Method method = (Method) func[0];
            Object[] params = (Object[]) func[1];
            for (int i = 0; i < method.getParameterTypes().length && i < params.length; i++) {
                if (method.getParameterTypes()[i].equals(Double.class)) {
                    params[i] = ((BigDecimal) params[i]).doubleValue();
                } else if (method.getParameterTypes()[i].equals(Float.class)) {
                    params[i] = ((BigDecimal) params[i]).floatValue();
                }
            }
            method.invoke(obj, params);
        }
    }

    private static void injectIdTypeProperties(Object obj) throws Exception {
        if (obj == null || !idTypePropertiesMap.containsKey(obj)) {
            return;
        }

        for (Map.Entry<String, String> entry : idTypePropertiesMap.get(obj).entrySet()) {
            String field = entry.getKey();
            String valueRefId = entry.getValue();
            Object value = getBeanById(valueRefId);//为避免死锁，可以调用不注入属性的getBean方法
            if (value == null) {
                continue;
            }
            for (Method method : obj.getClass().getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase("set" + field)) {
                    method.invoke(obj, value);
                }
            }
        }
        idTypePropertiesMap.remove(obj);
    }

    public static void addContextByFilePath(String filePath) throws Exception {
        String jsonText = readContextFile(filePath);
        initContextByJsonText(jsonText);
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

    private static void initContextByJsonText(String jsonContextText) throws Exception {
        JSONArray beansArray = JSON.parseArray(jsonContextText);
        for (Object bean : beansArray) {
            Map beanDefinition = (Map) bean;
            String id = ((String) beanDefinition.get("id"));
            String className = ((String) beanDefinition.get("class"));
            Map properties = new HashMap();
            List initFuncList = new ArrayList();
            if (beanDefinition.containsKey("properties")) {
                properties = ((Map) beanDefinition.get("properties"));
            }
            if (beanDefinition.containsKey("initFuncList")) {
                initFuncList = (List) beanDefinition.get("initFuncList");
            }

            Object instance = newInstance(className, properties);
            addInitFuncList(instance, initFuncList);

            beanMapById.put(id, instance);
        }
    }

    private static void addInitFuncList(Object instance, List initFuncList) {
        for (Object obj : initFuncList) {
            Map funcMap = (Map) obj;
            String funcName = ((String) funcMap.get("func"));
            Object[] params = null;
            if (funcMap.containsKey("params")) {
                List paramList = ((List) funcMap.get("params"));
                params = paramList.toArray();
            }

            for (Method method : instance.getClass().getDeclaredMethods()) {
                if (method.getName().equals(funcName)) {//这样支持不了重载的方法
                    if (!initFuncListMap.containsKey(instance)) {
                        initFuncListMap.put(instance, new ArrayList<Object[]>());
                    }
                    Object[] func = new Object[2];
                    func[0] = method;
                    func[1] = params;

                    initFuncListMap.get(instance).add(func);
                }
            }
        }
    }

    /**
     * 属性赋值：
     * value类型直接调用getter赋值
     * id类型先保存下来不做赋值，待bean被使用时再检查是否需要赋值
     */
    private static Object newInstance(String className, Map properties) throws Exception {
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(className);
        Object obj = aClass.newInstance();

        for (Object entry : properties.entrySet()) {
            Map.Entry<String, Object> objectEntry = (Map.Entry<String, Object>) entry;
            String key = objectEntry.getKey();
            Map value = ((Map) objectEntry.getValue());

            if ("value".equals(value.get("type"))) {
                for (Method method : aClass.getDeclaredMethods()) {
                    if (method.getName().equalsIgnoreCase("set" + key)) {
                        System.out.println(method.getName() + "," + value.get("value") + "," + value.get("value").getClass());
                        Class<?> paramClass = method.getParameterTypes()[0];
                        if (paramClass.equals(Double.class)) {
                            method.invoke(obj, ((BigDecimal) value.get("value")).doubleValue());
                        } else if (paramClass.equals(Float.class)) {
                            method.invoke(obj, ((BigDecimal) value.get("value")).floatValue());
                        } else {
                            method.invoke(obj, value.get("value"));
                        }
                    }
                }
            } else if ("id".equals(value.get("type"))) {
                if (!idTypePropertiesMap.containsKey(obj)) {
                    idTypePropertiesMap.put(obj, new ConcurrentHashMap<String, String>());
                }
                idTypePropertiesMap.get(obj).put(key, ((String) value.get("value")));
            }
        }
        return obj;
    }
}
