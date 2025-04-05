package cn.fighter3.dto;

import lombok.Data;

@Data
public class AnswerRequest {
    private Integer Id;  // 问题ID
    private String content;  // 回答内容
}