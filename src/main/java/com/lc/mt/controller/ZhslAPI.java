package com.lc.mt.controller;

import com.lc.mt.utils.MongoUtils;
import com.lc.mt.utils.TableName;
import net.sf.json.JSONObject;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
public class ZhslAPI {


    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoUtils mongoUtils;

    //模拟浪潮提交表单数据
    @PostMapping(value = "saveData")
    public String saveData(@RequestBody String dataStr){
        JSONObject json = JSONObject.fromObject(dataStr);
        String formId = json.getString("formId");
        String formData = json.getString("formData");
        //需要入库的数据
        Document document = new Document();
        JSONObject data = JSONObject.fromObject(formData);
        document.put("formId",formId);//表单id
        document.put("formData",data);//表单数据
        document.put("saveTime",new Date());
        //生成唯一标识作为dataId
        String dataId = mongoUtils.generateDataId();
        document.put("dataId",dataId);
        mongoTemplate.insert(document,TableName.LC_SAVEDATA);
        JSONObject result = new JSONObject();
        result.put("dataId",dataId);
        result.put("state","200");
        return result.toString();
    }

    //模拟浪潮更新表单数据接口
    @PostMapping(value = "upsaveData")
    public String upsaveData(HttpServletRequest request){
        String formId = request.getParameter("formId");
        String formData = request.getParameter("formData");
        JSONObject data = JSONObject.fromObject(formData);
        String dataId = request.getParameter("dataId");
        Query query = new Query();
        query.addCriteria(Criteria.where("dataId").is(dataId));
        Document doc = mongoTemplate.findAndRemove(query,Document.class,TableName.LC_SAVEDATA);
        JSONObject result = new JSONObject();
        if (null == doc) {
            result.put("state","300");
            return result.toString();
        }
        doc.put("formData",data);
        doc.put("saveTime",new Date());
        mongoTemplate.insert(doc,TableName.LC_SAVEDATA);
        result.put("dataId",dataId);
        result.put("state","200");
        return result.toString();
    }

    //模拟浪潮文件上传网盘
    @PostMapping(value = "upfile")
    public String up(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String uuid = mongoUtils.up(file);
        JSONObject msg = new JSONObject();
        if (null != msg ) {
            msg.put("msg","OK");
            msg.put("code","0000");
            msg.put("docid",uuid);
            msg.put("uuid",uuid);
        } else {
            msg.put("msg","GG");
            msg.put("code","0001");
        }
        return msg.toString();
    }

    //模拟浪潮文件下载
    @GetMapping("load")
    public byte[] load(@RequestParam(value = "docid") String docid, HttpServletResponse response){
        //获取文件信息
        byte[] b = mongoUtils.load(docid);
        return b;
    }

    //模拟浪潮获取事项材料信息
    @GetMapping("getItemInfo")
    public String getItemInfo(@RequestParam(value = "itemCode") String itemCode){
       return mongoUtils.getItemMetailInfo(itemCode);
    }

    //模拟申报事项
    @PostMapping("webapply")
    public String webApply(@RequestParam(value = "postdata") String postdata,@RequestParam(value = "itemCode") String itemCode){

        //解析postdata
        JSONObject object = JSONObject.fromObject(postdata);
        Document document = new Document();
        document.put("postdata",object);
        document.put("applyTime",new Date());
        String receiveNum = mongoUtils.generateReceiveNum(itemCode);
        document.put("receiveNum",receiveNum);
        mongoTemplate.insert(document,TableName.LC_WEBAPPLY);
        JSONObject result = new JSONObject();
        result.put("receiveNum",receiveNum);
        result.put("state","200");
        return result.toString();
    }

}
