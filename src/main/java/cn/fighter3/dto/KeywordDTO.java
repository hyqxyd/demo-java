// KeywordDTO.java
package cn.fighter3.dto;

import lombok.Data;

@Data
public class KeywordDTO {
    private String keyword;
    private Integer count;

    public KeywordDTO(String keyword, Integer count) {
        this.keyword = keyword;
        this.count = count;

    }

    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
