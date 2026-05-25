package com.willian.springmail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String mail;

    @NotBlank
    private String phone;

    @NotBlank
    private String message;
}
