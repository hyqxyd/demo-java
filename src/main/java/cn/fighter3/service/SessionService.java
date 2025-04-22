package cn.fighter3.service;


import cn.fighter3.dto.HistorySessionDTO;
import cn.fighter3.dto.QuestionDetail;
import cn.fighter3.entity.Session;
import cn.fighter3.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.fighter3.mapper.SessionMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class SessionService {
    @Autowired
    private SessionMapper sessionMapper;
    public void saveSession(String id,int qId,int aId,int mId,int tId,int userId,String content,int pid) {
        Session session=new Session();
        session.setId(id);
        session.setQId(qId);
        session.setAId(aId);
        session.setMId(mId);
        session.setTId(tId);
        session.setUserId(userId);
        session.setContent(content);
        session.setSessionTime();
        session.setPId(pid);
        sessionMapper.insert(session);

    }
    //加载历史记录
    public List<HistorySessionDTO> getSessionByUserId(int userId) {
        System.out.println(userId);
     List<Session> historysessions= sessionMapper.selectByUserId(userId);
     List<HistorySessionDTO> historySessionDTOS=new ArrayList<>();
     for(Session session:historysessions) {
         HistorySessionDTO historySessionDTO=new HistorySessionDTO();
         historySessionDTO.setDate(session.getSessionTime());
         historySessionDTO.setTopic(sessionMapper.selectNameByTId(session.getTId()));
         historySessionDTO.setContent(session.getId());
         historySessionDTOS.add(historySessionDTO);

     }
        return  historySessionDTOS;
    }
    //加载历史对话
    //删除对话
    public void deleteSession(String id) {sessionMapper.deleteById(id);
    }
    public List<Session> getSessionListByUserId(int userId) {
        return sessionMapper.selectByUserId(userId);
    }
    public Session getSessionById(String id) {
        return sessionMapper.selectById(id);
    }
    public  List<Session> getSessionBystudentIdtopicIdAndproblemId(int studentId , int topicId ,int problemId) {
        return sessionMapper.selectBystudentIdtopicIdproblemId(studentId,topicId,problemId);
    }



}
