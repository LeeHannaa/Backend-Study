package com.example.s3image.controller;

import com.example.s3image.domain.S3Image;
import com.example.s3image.dto.response.S3ImageResponse;
import com.example.s3image.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/b1a2/s3Image")
public class S3ImageController {
    private final S3Service s3Service;

    @PostMapping("/post")
    public void postFile(@RequestParam MultipartFile s3Image) throws IOException {
        s3Service.postFile(s3Image);
    }

    @GetMapping("/list")
    public ResponseEntity<List<S3ImageResponse>> findAll() {return ResponseEntity.ok(s3Service.findAll());}

    @GetMapping("/{keyId}")
    //사용자의 HttpRequest에 대한 응답 데이터를 포함하는 클래스 -> HttpStatus, HttpHeaders, HttpBody를 포함
    public ResponseEntity<S3Image> s3ImageRead(@PathVariable Long keyId){
        S3Image s3Image = s3Service.findByKeyId(keyId);
        return new ResponseEntity<>(s3Image, HttpStatus.OK);
    }

}
