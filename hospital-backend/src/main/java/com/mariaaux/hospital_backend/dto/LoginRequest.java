package com.mariaaux.hospital_backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String dni;
    private String clave;
}