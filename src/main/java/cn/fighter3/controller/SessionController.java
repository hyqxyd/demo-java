package cn.fighter3.controller;


import cn.fighter3.dto.HistorySessionDTO;
import cn.fighter3.dto.QueryDTO;
import cn.fighter3.dto.QuestionDetail;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.SessionMapper;
import cn.fighter3.result.Result;
import cn.fighter3.service.SessionService;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @PostMapping("/loadHistory")//加载历史对话
    public Result loadHistoryDialogue(@RequestBody String prompt) {
        System.out.println(prompt);
        JSONObject jsonObj =JSON.parseObject(prompt);
        System.out.println(prompt);
        int user_id=jsonObj.getIntValue("id");
        String s_id=jsonObj.getString("sessionId");
        System.out.println(s_id);

        Session session=sessionMapper.selectOne(new QueryWrapper<Session>().eq("id", s_id).eq("user_id", user_id));
        int m_id=session.getMId();
        String.valueOf(m_id);
        String json="["+session.getContent()+"]";
         System.out.println(json);
        JSONArray jsonArray = JSON.parseArray(json);
        List<Map<String, Object>> messages =new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Map<String, Object> messageMap = jsonObject.toJavaObject(new TypeReference<Map<String, Object>>() {});
            messages.add(messageMap);
        }

        // 现在messages是一个包含Map的列表，每个Map对应于JSON中的一个对象
        System.out.println(messages);
       return new Result(200,  String.valueOf(m_id), messages);
    }

}
