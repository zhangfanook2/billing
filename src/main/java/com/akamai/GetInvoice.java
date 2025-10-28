package com.akamai;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class GetInvoice {
    public static void main(String[] args) {
        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();
        String token = getProperty("linode.api.token");
        //System.out.println(token);

        // 构建请求
        Request request = new Request.Builder()
                .url("https://api.linode.com/v4/account/invoices")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Bearer " + token)
                .build();

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            // 检查请求是否成功 (HTTP 状态码 2xx)
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string(); // 获取响应体内容
                //System.out.println("请求成功！响应内容：");
                //System.out.println(responseBody);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Object json = gson.fromJson(responseBody, Object.class);
                String prettyJson = gson.toJson(json);
                System.out.println("✅ 请求成功！响应内容（格式化后）：");
                System.out.println(prettyJson);
            } else {
                // 请求失败，打印错误信息
                System.out.println("请求失败！状态码: " + response.code());
                if (response.body() != null) {
                    System.out.println("错误信息: " + response.body().string());
                }
            }
        } catch (IOException e) {
            // 处理网络异常
            System.err.println("网络请求发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getProperty(String key) {
        try (InputStream input = GetInvoice.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("无法找到 application.properties 文件");
                return null;
            }
            prop.load(input);
            return prop.getProperty(key);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}