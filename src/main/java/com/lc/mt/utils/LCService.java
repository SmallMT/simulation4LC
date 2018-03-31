package com.lc.mt.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

public class LCService {
    private static CloseableHttpClient httpClient;
    private static final String lcurl = "http://199.224.253.5:8085/";
    private static final String lcurl_ = "http://199.224.253.5:8087/Service/";
    private LCService(){}


    //根据事项编码获取表单信息
    //返回json格式字符串
//    public static String getFormInfo(String itemCode){
//        Map<String,String> params = new HashMap<>();
//        params.put("itemCode",itemCode);
//        String urlPath = lcurl + "getFormInfo";
//        String content = get(urlPath,params);
//        return content;
//    }
    //根据formId获取表单
    //返回html格式字符串
//    public static String getFormUI(String formId) {
//        //http://199.224.253.11:8081/cform/getFormUI?formId=ShiShouLiBiaoDan
//        Map<String,String> params = new HashMap<>();
//        params.put("formId",formId);
//        String urlPath = "http://199.224.253.11:8081/cform/getFormUI";
//        String content = get(urlPath,params);
//        return content;
//    }

//    public static String saveData(String formId , String data){
//        String urlPath = lcurl + "saveDataEx?formId="+formId;
//    }
    public static void main(String[] args) {
//        LCService.getFormInfo("46020000YG-FW-0104");
//        LCService.getFormUI("ShiShouLiBiaoDan");
//        String str = getItemInfoByItemCode("46020000YG-XK-0102");
//        System.out.println(str);
//        String[] codes = {"46020000YG-XK-0102","46020000YG-XK-0101","46020000YG-FW-0104"
//        ,"46020000YG-FW-0103","46020000YG-FW-0102","46020000YG-FW-0101",""};
    }

    public static String getFormInfo(String itemCode){
        String urlPath = lcurl_ + "getFormInfo";
        Map<String,String> params = new HashMap<>();
        params.put("itemCode",itemCode);
        return get(urlPath,params);
    }

    public static String getFormUI(String formId){
        String urlPath = "http://199.224.253.5:8081/cform/getFormUI";
        Map<String,String> params = new HashMap<>();
        params.put("formId",formId);
        return get(urlPath,params);
    }

    public static String saveData(String formId,String formData){
        String urlPath = lcurl_ + "saveDataEx";
        Map<String,String> map = new HashMap<>();
        map.put("formId",formId);
        map.put("formData",formData);
        return post(urlPath,map);
    }

    public static String getItemInfoByItemCode(String itemCode){
        String urlPaht = lcurl+"main/power/getItemInfoByItemCode";
        Map<String,String> params = new HashMap<>();
        params.put("itemCode",itemCode);
        params.put("type","material");
        String info = get(urlPaht,params);
        return info == null ? "":info;
    }

    public static String getBusinessInfo(String receiveNum){
        String urlPaht = lcurl_+"getBusinessInfo";
        Map<String,String> params = new HashMap<>();
        params.put("receiveNumber",receiveNum);
        return get(urlPaht,params);
    }

    public static JSONArray getBusinessInfoJSONArray(String receiveNum){
        String urlPaht = lcurl_+"getBusinessInfo";
        Map<String,String> params = new HashMap<>();
        params.put("receiveNumber",receiveNum);
        String info = get(urlPaht,params);
        if ("".equals(info))
            return null;
        JSONObject obj = JSONObject.fromObject(info);
        return obj.getJSONArray("info");
    }

    public static JSONArray getItemInfoByItemCodeJSONArray(String itemCode){
        String itemInfo = getItemInfoByItemCode(itemCode);
        if ("".equals(itemInfo)) {
            return null;
        }
        JSONObject json = JSONObject.fromObject(itemInfo);
        if ("1".equals(json.getString("state"))){
            JSONArray array = json.getJSONArray("ItemInfo");
            return array;
        }
        return null;
    }

    public static String post(String url,Map<String,String> map){
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        String content = null;
        try{
            httpClient = getCloseableHttpClient();
            httpPost = new HttpPost(url);
            //封装参数
            httpPost.addHeader("Content-type","application/json; charset=utf-8");
            httpPost.setHeader("Accept", "application/json");
            List<NameValuePair> list = new ArrayList<>();
            Set<String> keys = map.keySet();
            for (String key : keys) {
                list.add(new BasicNameValuePair(key,map.get(key)));
            }
            if(list.size() > 0){
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"UTF-8");
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                content = EntityUtils.toString(response.getEntity(),"utf-8");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String get(String url , Map<String,String> params){
        String content = null;
        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        URIBuilder builder = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = getCloseableHttpClient();
            //封装请求参数
            builder = new URIBuilder(url);
            for (String key : params.keySet()) {
                builder.setParameter(key,params.get(key));
            }
            httpGet = new HttpGet(builder.build());
            RequestConfig config = RequestConfig.custom()//
                    .setConnectTimeout(5000)//
                    .setConnectionRequestTimeout(5000)//
                    .setSocketTimeout(5000)//
                    .build();
            httpGet.setConfig(config);
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {//请求成功
                HttpEntity entity = response.getEntity();
                content = EntityUtils.toString(entity,"utf-8");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (httpClient != null)
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return content;

    }

    private static CloseableHttpClient getCloseableHttpClient(){
        if (null == httpClient)
            return HttpClients.createDefault();
        return httpClient;
    }
}
