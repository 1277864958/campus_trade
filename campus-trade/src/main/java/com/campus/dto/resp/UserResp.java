package com.campus.dto.resp;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class UserResp {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private Double avgScore;
    private LocalDateTime createdAt;
}
