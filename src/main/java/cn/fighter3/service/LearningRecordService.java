package cn.fighter3.service;

import cn.fighter3.dto.*;
import cn.fighter3.entity.LearningRecord;
import cn.fighter3.entity.LearningRecordUpdate;
import cn.fighter3.entity.Session;
import cn.fighter3.mapper.LearningRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningRecordService {

    @Autowired
    private LearningRecordMapper learningRecordMapper;

    public int save(LearningRecord learningRecord){
        learningRecordMapper.insert(learningRecord);
        return learningRecord.getId();
//       JSONObject jsonObject = new JSONObject(learningRecord);
//       int studentId = jsonObject.getInt("studentId");
//       int problemId = jsonObject.getInt("problemId");
//       String sessionStartTime = jsonObject.getString("sessionStartTime");
//       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//       int modelUsed=jsonObject.getInt("modelUsed");
//       int duration = jsonObject.getInt("duration");
//       String content=jsonObject.getString("contert");
//       String keywords=jsonObject.getString("keywords");
//
//
//
//       Date Startdate = null;
//
//
//       try {
//
//           Startdate= dateFormat.parse(sessionStartTime);
//           // 输出 Date 对象
//           System.out.println("转换后的日期时间： " + Startdate);
//
//       }catch (ParseException e) {
//
//           e.printStackTrace();
//
//       }
//       LearningRecord learningRecord1 = new LearningRecord();
//       learningRecord1.setStudentId(studentId);
//       learningRecord1.setProblemId(problemId);
//       learningRecord1.setSessionStartTime(Startdate);
//       learningRecord1.setSessionEndTime(null);
//       learningRecord1.setDuration(duration);
//       learningRecord1.setModelUsed(modelUsed);
//       learningRecord1.setContent(content);
//       learningRecord1.setKeywords(keywords);
//       learningRecordMapper.insert(learningRecord1);
//
//       return learningRecord1.getId();

    }
    public void update(LearningRecordUpdate learningRecord, int id) {


        LearningRecord learningRecord1 = learningRecordMapper.selectOne(new QueryWrapper<LearningRecord>().eq("id", id));
        learningRecord1.setDuration(learningRecord.getDuration()/(1000));
        learningRecord1.setSessionEndTime(learningRecord.getSessionEndTime());
        UpdateWrapper<LearningRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        learningRecordMapper.update(learningRecord1,updateWrapper );

    }

    public List<LearningRecord> getLearningRecordsByProblemId(Integer problemId) {
        return learningRecordMapper.getLearningRecordsByProblemId(problemId);


    }

    public List<LearningRecord> getLearningRecordsByUserId(Integer studentId, Integer problemId) {
        return learningRecordMapper.getLearningRecordsByStudentAndProblem(studentId, problemId);
    }

    // LearningRecordService.java
    public List<FrequencyDTO> getDailyFrequency(Integer studentId, Integer problemId) {
        return learningRecordMapper.getDailyFrequency(studentId, problemId);
    }

    public List<ModelUsageDTO> getModelUsageCount(Integer studentId, Integer problemId) {
        return learningRecordMapper.getModelUsageCount(studentId, problemId);
    }

    public List<DurationDTO> getDailyDuration(Integer studentId, Integer problemId) {
        return learningRecordMapper.getDailyDuration(studentId, problemId);
    }

    // LearningRecordService.java
    public List<ModelDurationDTO> getModelDuration(Integer studentId, Integer problemId) {
        return learningRecordMapper.getModelDuration(studentId, problemId);
    }
    public List<KeywordDTO> getKeywords(Integer studentId, Integer problemId) {
        List<String> keywordStrings = learningRecordMapper.findKeywordsByStudentAndProblem(studentId, problemId);
        Map<String, Integer> keywordCountMap = new HashMap<>();

        for (String keywords : keywordStrings) {
            if (keywords == null || keywords.trim().isEmpty()) continue;
            Arrays.stream(keywords.split(","))
                    .map(String::trim)
                    .filter(keyword -> !keyword.isEmpty())
                    .forEach(keyword ->
                            keywordCountMap.put(keyword, keywordCountMap.getOrDefault(keyword, 0) + 1)
                    );
        }

        return keywordCountMap.entrySet().stream()
                .map(entry -> new KeywordDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.getCount() - a.getCount())
                .collect(Collectors.toList());
    }


}
