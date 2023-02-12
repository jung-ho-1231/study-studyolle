package com.example.studyolle.mail;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
public class EmailMessage {
    private String to;
    private String subject;
    private String message;
}
