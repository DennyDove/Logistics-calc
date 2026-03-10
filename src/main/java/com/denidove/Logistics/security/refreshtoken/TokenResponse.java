package com.denidove.Logistics.security.refreshtoken;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;

    public TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

}
