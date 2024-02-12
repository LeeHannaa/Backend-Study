package com.example.s3image.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class S3ImageReqeust {
    // client가 server로 전달하는 정보
    private String imagePath;
}
