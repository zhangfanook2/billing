package com.akamai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.client.methods.HttpGet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String LI_NODE_ID_URL = "https://api.linode.com/v4/account/invoices";
    private static final String token = getProperty("linode.api.token_bytedance");
    private static final String token2 = getProperty("linode.api.token_2");
    private static final String token3 = getProperty("linode.api.token_3");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();



    public static void main(String[] args) throws Exception{
        MesosTaskContext mesosTaskContext = new MesosTaskContext();

        //获取currentYearMonth
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date triggerOffset = new Date();
        String currentYearMonth = sdf.format(triggerOffset);

        //获取token集合
        Set<String> tokenSet = Sets.newHashSet();
        Set<String> allocationTokensSet = Set.of(token);
        Set<String> notAllocationTokensSet = Set.of (token2);
        tokenSet.addAll(allocationTokensSet);
        tokenSet.addAll(notAllocationTokensSet);
        //System.out.println(tokenSet);//print

        Map<String, Set<String>> tokenIgnoreIdMap = Maps.newHashMap();
        mesosTaskContext.setArg();
        //System.out.println(mesosTaskContext.getArg("tokenC", "null"));//print

        //每个token需要排除的id
        for (String token : tokenSet) {
            String tokenIgnoreIdParam = "ignoreId_" + currentYearMonth + "_" + token;
            String ignoreIdStr = mesosTaskContext.getArg(tokenIgnoreIdParam, "null");
            if ("null".equals(ignoreIdStr)) {
                throw new Exception(tokenIgnoreIdParam + " param not set");
            }
            Set<String> ignoreIdSet = Arrays.stream(ignoreIdStr.split(",")).map(String::trim).collect(Collectors.toSet());
            tokenIgnoreIdMap.put(token, ignoreIdSet);
        }
        System.out.println(tokenIgnoreIdMap);


        //fromNotAllocationLiNodeHttp();

    }

    public static LiNode.Items getItemsData() throws Exception{
        long startTimestamp = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date triggerOffset = new Date();
        String currentYearMonth = sdf.format(triggerOffset);

        int httpRetryTimes = 3;
        int retrySleepMs = 1000;
        int connectTimeout = 15000;
        int connectionRequestTimeout = 15000;
        int socketTimeout = 60000;

        CloseableHttpClient httpClient = HttpUtil.createHttpClient(connectTimeout, connectionRequestTimeout, socketTimeout);
        //获取该账号下的所有账单，取到对应账单周期的InvoiceId,默认第一页开始
        HttpGet idGet = new HttpGet(LI_NODE_ID_URL);
        idGet.addHeader("Authorization", "Bearer " + token);
        String idData = HttpUtil.request(httpClient, idGet, httpRetryTimes, retrySleepMs);
        //System.out.println("idData: " + idData); //print
        //LOGGER.info("token:{} \n url:{}, \n elapse:{}, \n idData:{} \n", token, LI_NODE_ID_URL, System.currentTimeMillis() - startTimestamp, idData);
        LiNode.InvoiceId invoiceId = GSON.fromJson(idData, LiNode.InvoiceId.class);
        //分页请求
        int page = invoiceId.getPage();
        int pages = invoiceId.getPages();
        //LOGGER.info("token:{} \n page:{}, \n pages:{}, \n results:{} \n", token, page, pages, invoiceId.getResults());
        while (page < pages) {
            page++;
            String url = LI_NODE_ID_URL + "?page=" + page;
            HttpGet indexGet = new HttpGet(url);
            indexGet.addHeader("Authorization", "Bearer " + token);
            String data = HttpUtil.request(httpClient, indexGet, httpRetryTimes, retrySleepMs);
            //LOGGER.info("token:{} request id url:{}, elapse:{}, page:{}, idData:{}", token, url, System.currentTimeMillis() - startTimestamp, page, idData);
            LiNode.InvoiceId invoiceIdData = GSON.fromJson(data, LiNode.InvoiceId.class);
            invoiceId.getData().addAll(invoiceIdData.getData());
        }
        Long id = null;
        for (LiNode.InvoiceId.Data data : invoiceId.getData()) {
            if (data.getDate() != null && currentYearMonth.equals(data.getDate().substring(0, 7))) {
                id = data.getId();
                //LOGGER.info("Invoice ID:{}",id);
                break;
            }
        }
        if (id == null) {
            throw new Exception(token + " current month invoiceId not find");
        }

        startTimestamp = System.currentTimeMillis();
        //从invoice-list拿到当月的Invoice ID之后，获取该月的账单明细
        String itemUrl = "https://api.linode.com/v4/account/invoices/" + id + "/items";
        HttpGet itemsGet = new HttpGet(itemUrl);
        itemsGet.addHeader("Authorization", "Bearer " + token);
        String itemData = HttpUtil.request(httpClient, itemsGet, httpRetryTimes, retrySleepMs);
        //LOGGER.info("token:{} ,\n request items url:{},\n elapse:{} ,\n item data length:{} ,\n detail:{}", token, itemUrl, System.currentTimeMillis() - startTimestamp, itemData.getBytes().length, itemData);
        LiNode.Items items = GSON.fromJson(itemData, LiNode.Items.class);
        //LOGGER.info("items:{}",items);//print

        //Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
        //System.out.println(PRETTY_GSON.toJson(items));
        /*
        int pageItems = items.getPage();
        int pagesItems = items.getPages();
        LOGGER.info("token:{} items data page:{}, pages:{}, results:{}", token, pageItems, pagesItems, items.getResults());
        while (pageItems < pagesItems) {
            pageItems++;
            String url = itemUrl + "?page=" + pageItems;
            HttpGet indexGet = new HttpGet(url);
            indexGet.addHeader("Authorization", "Bearer " + token);
            String data = HttpUtil.request(httpClient, indexGet, httpRetryTimes, retrySleepMs);
            //LOGGER.info("token:{} request items url:{}, elapse:{}, page:{}, item data length:{}, detail:{}", token, url, System.currentTimeMillis() - startTimestamp, pageItems, data.getBytes().length, data);
            LiNode.Items dataItems = GSON.fromJson(data, LiNode.Items.class);
            items.getData().addAll(dataItems.getData());
        }
        System.out.println("data 中的数据个数: " + items.getData().size());//print
        */
        return items;
    }

    public static List<ByteData> fromNotAllocationLiNodeHttp()throws Exception{
        int httpRetryTimes = 3;
        int retrySleepMs = 1000;
        int connectTimeout = 15000;
        int connectionRequestTimeout = 15000;
        int socketTimeout = 60000;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date triggerOffset = new Date();
        String currentYearMonth = sdf.format(triggerOffset);
        //Set<String> ignoreIdSet = Arrays.stream(ignoreIdStr.split(",")).map(String::trim).collect(Collectors.toSet());

        LiNode.Items items = getItemsData();

        List<ByteData> reportDataList = Lists.newArrayList();
        List<ByteData.BillEntry> billEntryList = Lists.newArrayList();
        int num = 0;
        int totalLog = 0;

        /*
        //id 重复判断
        Set<String> labelSetTemp = Sets.newHashSet();
        for (LiNode.Items.Data data : items.getData()) {
            String label = data.getLabel();

            if (!label.startsWith("Dedicated") && !label.contains("Network Transfer")) {
                LOGGER.info("token:{} filter label:{}", token, label);
                continue;
            }

            int start = label.indexOf("(");
            int end = label.indexOf(")");
            if (start < 0 || end < 0) {
                LOGGER.warn("token:{} label invalid: {}", token, label);
                continue;
            }
            String instance_id = label.substring(start + 1, end);
            if (ignoreIdSet.contains(instance_id)) {
                LOGGER.info("token:{} ignore instance_id: {}", token, instance_id);
                continue;
            }

            if (labelSetTemp.contains(label)) {
                labelSet.add(label);
            } else {
                labelSetTemp.add(label);
            }

            String good_category;
            String amount_number;
            String amount_unit;
            Long quantity = data.getQuantity();
            if (label.startsWith("Dedicated")) {
                good_category = "CloudVirtualServer";
                amount_unit = "Percentage";
                amount_number = "1";
                if (quantity != null) {
                    double v = quantity * 1.0 / (24 * lastMonthDays);
                    amount_number = String.format("%.3f", v);
                }
            } else {
                good_category = "CloudTraffic";
                amount_unit = "GB";
                amount_number = "0";
                if (quantity != null) {
                    amount_number = quantity.toString();
                }
            }


            ByteData.BillEntry billEntry = new ByteData.BillEntry();

            //字节那把ap以外的都识别为国内，然后费用就少了，需求方要求逻辑改成固定写死ap
            billEntry.setBill_region("ap");

            billEntry.setBill_cycle(lastYearMonth);
            billEntry.setGood_category(good_category);
            billEntry.setInstance_type("ServerUuid");
            billEntry.setInstance_id(instance_id);
            billEntry.setNinety_five_time("");
            billEntry.setAmount_number(amount_number);
            billEntry.setAmount_unit(amount_unit);
            //只是为了查看
            billEntry.setToken(token);

            billEntryList.add(billEntry);
            num++;
            totalLog++;
            if (num >= numPerRequest) {
                ByteData byteData = new ByteData();
                byteData.setIs_test(isTest);
                byteData.setBill_entry_list(billEntryList);
                billEntryList = Lists.newArrayList();
                reportDataList.add(byteData);
                num = 0;
            }
        }
        */

        return null;
    }




    //读取application.properties
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
