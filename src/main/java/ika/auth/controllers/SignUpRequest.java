package ika.auth.controllers;

import lombok.Data;

@Data
public class SignUpRequest {
    private String displayName;

    private String email;

    private String password;

    private String locale;
}
