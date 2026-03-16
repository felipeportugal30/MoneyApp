package com.moneyapp.v1.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeleteFileResponseDto {
    private String message;
    private UUID id;
    private String filename;
    private String hash;
    private Long size;
    private String path;
}
