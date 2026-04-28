package com.br3akPoint.api_gateway.data;

public class UserRequestData {
    private String email;
    private Long userId;

    public UserRequestData(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
