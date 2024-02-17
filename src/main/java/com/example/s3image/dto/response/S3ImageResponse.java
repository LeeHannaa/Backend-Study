package com.example.s3image.dto.response;

import lombok.Data;

@Data
public class S3ImageResponse {
    // server가 client로 전달하는 정보
    private Long keyId;
    private String imageName;
    private String imagePath;
    private String createDate;
}
