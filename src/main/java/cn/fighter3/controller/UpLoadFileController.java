package cn.fighter3.controller;

import cn.fighter3.result.Result;
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
    // 通义千问上传接口
    @PostMapping("/api/tongyiupload")
    public Result tongyiUpload(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "tongyi");
    }

    // 科大讯飞上传接口
    @PostMapping("/api/xunfeiupload")
    public Result xunfeiUpload(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "xunfei");
    }

    // 文心一言上传接口
    @PostMapping("/api/wenxinupload")
    public Result wenxinUpload(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "wenxin");
    }

    // 文心一言上传接口
    @PostMapping("/api/kimiupload")
    public Result kimiUpload(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "kimi");
    }
    private Result uploadFile(MultipartFile file, String service) {
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
            return new Result(200, "请求成功", "文件上传成功: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(500, "", "文件上传失败");
        }
    }



}
