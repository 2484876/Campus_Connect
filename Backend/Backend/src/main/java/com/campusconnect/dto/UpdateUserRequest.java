package com.campusconnect.dto;
import lombok.Data;
import java.util.List;
@Data
public class UpdateUserRequest {
    private String name;
    private String department;
    private String position;
    private String bio;
    private String phone;
    private List<String> skills;
}