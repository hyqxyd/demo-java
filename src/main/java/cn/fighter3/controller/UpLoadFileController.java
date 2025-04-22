package cn.fighter3.controller;

import cn.fighter3.result.Result;
import cn.fighter3.service.FileAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@RestController
public class UpLoadFileController {
    @Autowired
    private FileAnalysisService fileAnalysisService;
    // 通义千问上传接口
//    @PostMapping("/api/tongyiupload")
//    public Result tongyiUpload(@RequestParam("file") MultipartFile file) {
//        return uploadFile(file, "tongyi");
//    }
//
//    // 科大讯飞上传接口
//    @PostMapping("/api/xunfeiupload")
//    public Result xunfeiUpload(@RequestParam("file") MultipartFile file) {
//        return uploadFile(file, "xunfei");
//    }
//
//    // 文心一言上传接口
//    @PostMapping("/api/wenxinupload")
//    public Result wenxinUpload(@RequestParam("file") MultipartFile file) {
//        return uploadFile(file, "wenxin");
//    }
//
//    // 文心一言上传接口
//    @PostMapping("/api/kimiupload")
//    public Result kimiUpload(@RequestParam("file") MultipartFile file) {
//        return uploadFile(file, "kimi");
//    }


    @PostMapping("/api/file/analyze")
    private Result uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam String service,
            @RequestParam Integer userId,
            @RequestParam String sessionId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer topicId,
            @RequestParam(required = false) Integer problemId,
            @RequestParam String query
           ) {

        int m_id=0;
        if ("wenxin".equals(service)){
            m_id=1;
        }else if ("tongyi".equals(service)){
            m_id=2;
        }else if ("xunfei".equals(service)){
            m_id=3;
        }else if ("kimi".equals(service)){
            m_id=4;
        }
        // 获取项目的绝对路径
        String basePath = System.getProperty("user.dir") + "/src/main/resources/upload/" + service + "/";
        File dir = new File(basePath);

        // 确保目录存在
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录
        }

        // 生成文件名
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File destFile = new File(dir, fileName);

        try {
            // 保存文件
            file.transferTo(destFile);
            fileAnalysisService.analyzeAndSave(service,fileName,m_id,userId,sessionId,courseId,topicId,problemId,query);

            return new Result(200, "请求成功", "文件上传成功: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(500, "", "文件上传失败");
        }
    }



}
