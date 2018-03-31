package com.lc.mt.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import net.sf.json.JSONObject;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class MongoUtils {

    private DB db ;

    @Autowired
    MongoTemplate mongoTemplate;

    //文件保存到mongo库中
    public String up(MultipartFile file) {
        String uuidStr = null;
        this.db = mongoTemplate.getDb();
        GridFS gridFS = new GridFS(db);
        String fileName = file.getOriginalFilename();
        try {
            GridFSInputFile gridFSInputFile = gridFS.createFile(file.getBytes());
            gridFSInputFile.put("filename",fileName);
            gridFSInputFile.put("contentType",file.getContentType());
            UUID uuid = UUID.randomUUID();
            uuidStr = uuid.toString();
            gridFSInputFile.put("uuid",uuidStr);
            gridFSInputFile.put("docid",uuidStr);
            gridFSInputFile.save();
        }catch (IOException e){
            e.printStackTrace();
        }
        return uuidStr;
    }

    //从mongo库中获取文件
    public byte[] load(String docid){
        this.db = mongoTemplate.getDb();
        GridFS gridFS = new GridFS(db);
        //根据uuid获取文件objectId
        DBObject object = new BasicDBObject("docid",docid);
        GridFSDBFile file = gridFS.findOne(object);
        try {
            Long len = file.getLength();
            byte[] b = new byte[len.intValue()];
            file.getInputStream().read(b);
            return b;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //保存表单数据生成dataId
    public String generateDataId(){
        //dataId 采用当前年月日+uuid
        StringBuffer dataId = new StringBuffer();
        //获取当前年月日
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String[]  preDates= format.format(currentDate).split("-");
        for (String str : preDates) {
            dataId.append(str);
        }
        UUID uuid = UUID.randomUUID();
        String suffix = uuid.toString().replaceAll("-","");
        dataId.append(suffix);
        return dataId.toString();
    }

    //生成申报后的流水号
    public String generateReceiveNum(String itemCode){
        //使用当前事项编号与当前时间戳作为流水号
        StringBuffer sb = new StringBuffer();
        sb.append(itemCode);
        sb.append("-");
        sb.append(currentDateString());
        return sb.toString();
    }
    //获取当前时间字符串
    //格式为 20180109101306
    private String currentDateString(){
        StringBuffer sb = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        sb.append(year);
        int month = calendar.get(Calendar.MONTH) + 1;
        if (month < 10 ) {
            sb.append(0);
        }
        sb.append(month);
        int day = calendar.get(Calendar.DATE);
        if (day < 10) {
            sb.append(0);
        }
        sb.append(day);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            sb.append(0);
        }
        sb.append(hour);
        int min = calendar.get(Calendar.MINUTE);
        if (min < 10) {
            sb.append(0);
        }
        sb.append(min);
        int second = calendar.get(Calendar.SECOND);
        if (second < 10) {
            sb.append(0);
        }
        sb.append(second);
        return sb.toString();
    }

    //模拟获取浪潮事项所需材料
    public String getItemMetailInfo(String itemCode){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(itemCode));
        Map mp = mongoTemplate.findOne(query,Map.class,TableName.LC_ITEMMETAILINFO);
        JSONObject obj = JSONObject.fromObject(mp);
        return obj.toString();
    }


    public void  get(){
       List<Map> list = mongoTemplate.findAll(Map.class,"zhsl_item_info");
       for (Map map : list) {
            String code = map.get("code").toString();
            String info = LCService.getItemInfoByItemCode(code);
           JSONObject obj = JSONObject.fromObject(info);
           Document d = new Document();
           d.put("code",code);
           Set keys = obj.keySet();
           for (Object key : keys) {
               d.put(key.toString(),obj.getString(key.toString()));
           }
           mongoTemplate.insert(d,"item");
       }
    }

    public static void main(String[] args) {
        System.out.println( new MongoUtils().currentDateString());

    }
}
