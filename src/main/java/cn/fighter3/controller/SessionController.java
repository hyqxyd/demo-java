package cn.fighter3.controller;

import cn.fighter3.dto.HistorySessionDTO;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import cn.fighter3.result.Result;
import cn.fighter3.service.SessionService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historyDialogue")
public class SessionController {
          @Autowired
          private SessionService sessionService;
    @Autowired
    private SessionMapper sessionMapper;

    @GetMapping("/list/{userId}")//显示历史记录
      public Result sessionList(@PathVariable int userId) {
              System.out.println(userId);
              List<HistorySessionDTO> sessionList = sessionService.getSessionByUserId(userId);
              return new Result(200, "Success", sessionList);

    }
    @PostMapping("/deleteHistory")
    //删除历史对话
    public Result deleteHistoryDialogue(@RequestBody String prompt) {
        System.out.println(prompt);
        JSONObject jsonObj =JSON.parseObject(prompt);
        String id=jsonObj.getString("id");
        sessionService.deleteSession(id);
        return new Result(200, "Success", null);
    }



    @PostMapping("/loadHistory")
    public Result loadHistoryDialogue(@RequestBody String prompt) {
        JSONObject jsonObj = JSON.parseObject(prompt);
        int userId = jsonObj.getIntValue("id");
        String sessionId = jsonObj.getString("sessionId");

        // 查询会话
        Session session = sessionMapper.selectOne(
                new QueryWrapper<Session>()
                        .eq("id", sessionId)
                        .eq("user_id", userId)
        );
        if (session == null) {
            return new Result(404, "Session not found", null);
        }

        // 直接解析数据库中的JSON数组
        JSONArray jsonArray = JSON.parseArray(session.getContent());
        // 解析数据库中的 JSON 数组，并恢复换行符
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String content = obj.getString("content");
            // 将 \\n 恢复为 \n（如果数据库中有残留转义）
            obj.put("content", content.replace("\\n", "\n"));
        }

        List<Map<String, Object>> messages = jsonArray.toJavaObject(
                new TypeReference<List<Map<String, Object>>>() {}
        );

        return new Result(200, "Success", messages);
    }
    @GetMapping("/listByUserId/{userId}")
    public Result sessionListByUserId(@PathVariable int userId) {
        List<Session> sessionList = sessionService.getSessionListByUserId(userId);
        return new Result(200, "Success", sessionList);
    }
    @GetMapping("/session/{sessionId}")
    public Result getSessionById(@PathVariable String sessionId) {
        Session session = sessionService.getSessionById( sessionId);
        return new Result(200, "Success", session);
    }
}
