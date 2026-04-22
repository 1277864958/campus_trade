package com.campus.dto.resp;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class TokenResp {
    private String accessToken;
    private String refreshToken;
    private Long   userId;
    private String username;
    private String role;
    private long   expiresIn;
}
