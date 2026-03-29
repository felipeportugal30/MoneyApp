package com.moneyapp.v1.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SendMessageResponseDto {
    private String message;
    private String answer;
    private Date date;
    private boolean sucess;
}
