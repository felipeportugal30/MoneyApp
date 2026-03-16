package com.moneyapp.v1.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadFileResponseDto {
    private String message;
    private List<UUID> ids;
    private List<String> filenames;
    private List<Double> sizesMb;
    private int numFile;
    private boolean success;
}
