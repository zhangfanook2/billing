package com.akamai;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    public static String request(CloseableHttpClient httpclient, HttpRequestBase httpMethod, int retryTimes, int retrySleepMs) throws Exception {
        String resJsonString = null;
        int currentTimes = 1;
        String url = httpMethod.getURI().toString();
        String host = httpMethod.getURI().getHost();
        String ip = null;
        while (currentTimes <= retryTimes) {
            try {
                InetAddress inetAddress = InetAddress.getByName(host);
                ip = inetAddress.getHostAddress();
                HttpResponse response = httpclient.execute(httpMethod);
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    resJsonString = getStringFromResEntity(entity);
                    if (StringUtils.isNotBlank(resJsonString)) {
                        LOGGER.info("第{}次成功!", currentTimes);
                        EntityUtils.consume(entity);
                        return resJsonString;
                    } else {
                        LOGGER.error("请求:{}, 第{}次失败!", url, currentTimes);
                    }
                } else {
                    String errorContent = getStringFromResEntity(entity);
                    LOGGER.error("请求:{}, 第{}次重试失败,返回码{}...返回的错误内容:{}", url, currentTimes, statusCode, errorContent);
                }
                EntityUtils.consume(entity);
                currentTimes += 1;
            } catch (Exception e) {
                LOGGER.error("请求:{}, ip:{}, 发生异常:{}", url, ip, e);
                currentTimes += 1;
                if (currentTimes > retryTimes) {
                    throw new Exception("请求: " + url + ", 发生异常: " + e.getMessage(), e);
                }
            }
            //等待
            Thread.sleep(retrySleepMs);
        }
        throw new Exception("请求:" + url + " 失败，返回状态码非200");
    }

    //.setConnectTimeout(10 * 1000).setSocketTimeout(30 * 1000).setConnectionRequestTimeout(10 * 1000)
    public static CloseableHttpClient createHttpClient(int connectTimeout, int connectionRequestTimeout, int socketTimeout) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(60, TimeUnit.SECONDS);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(200);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(connectionManager);
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        httpClientBuilder.setDnsResolver(new DnsResolver() {
            @Override
            public InetAddress[] resolve(String host) throws UnknownHostException {
                List<InetAddress> ipv4s = new ArrayList<>();
                while (true) {
                    InetAddress[] all = InetAddress.getAllByName(host);
                    for (InetAddress addr : all) {
                        if (addr instanceof Inet4Address) ipv4s.add(addr);
                    }
                    if (!ipv4s.isEmpty()) {
                        break;
                    } else {
                        LOGGER.warn("host not resolve ipv4:{}", all);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
                return ipv4s.toArray(new InetAddress[0]);
            }
        });
        httpClientBuilder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        return httpClientBuilder.build();
    }

    /**
     * 获取Response内容,以String 返回
     *
     * @param entity
     * @return
     */
    private static String getStringFromResEntity(HttpEntity entity) {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(),
                StandardCharsets.UTF_8))) {
            // 将返回的数据读到buffer中
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                buffer.append(temp);
            }
        } catch (Exception e) {
            LOGGER.error("获取响应数据发送未知异常...", e);
            return null;
        }
        return buffer.toString();
    }


}
