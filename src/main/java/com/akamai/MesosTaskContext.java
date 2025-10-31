package com.akamai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MesosTaskContext {
    private Map<String, String> ignoreIdMap = new HashMap<>();

    public void setArg() {
        InputStream is = MesosTaskContext.class.getClassLoader().getResourceAsStream("ignoreids");
        if (is == null) {
            throw new IllegalArgumentException("Resource file 'ignoreids' not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    String value = line.substring(eqIndex + 1).trim();
                    ignoreIdMap.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ignoreids file", e);
        }
    }

    public String getArg(String key, String defaultValue) {
        return ignoreIdMap.getOrDefault(key, defaultValue);
    }
}


/*public class MesosTaskContext {
    private Set<String> tokenIgnoreIdParamSet;

    public void setArg() {  // 注意：这里可以去掉参数，改为内部加载
        tokenIgnoreIdParamSet = new HashSet<>();
        // 获取 classpath 下的 ignoreid 文件（必须在 resources 目录下）
        try (InputStream is = MesosTaskContext.class.getClassLoader().getResourceAsStream("ignoreids");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                throw new IllegalArgumentException("Resource 'ignoreids' not found in classpath");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // 去除前后空格
                if (!line.isEmpty() && !line.startsWith("#")) {  // 忽略空行和注释
                    tokenIgnoreIdParamSet.add(line);
                }
            }
            //System.out.println("ignoreids 文件加载完成，共加载 " + tokenIgnoreIdParamSet.size() + " 个 ID：");//print
            //tokenIgnoreIdParamSet.forEach(id -> System.out.println("   - " + id));//print
            System.out.println("加载的 ID 集合: " + tokenIgnoreIdParamSet);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ignoreid file", e);
        }
    }

    // getArg 方法保持不变
    public String getArg(String tokenIgnoreIdParam, String de) {
        if (tokenIgnoreIdParamSet.stream()
                .anyMatch(s -> s.contains(tokenIgnoreIdParam))) {
            return tokenIgnoreIdParam;
        } else {
            return de;
        }
    }
}
*/