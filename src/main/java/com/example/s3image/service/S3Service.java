package com.example.s3image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.s3image.domain.S3Image;
import com.example.s3image.dto.request.S3ImageReqeust;
import com.example.s3image.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Repository s3Repository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String postFile(MultipartFile multipartFile) throws IOException{
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return uploadFileToS3(uploadFile);
    }

    private String uploadFileToS3(File uploadFile){
        UUID uuid = UUID.randomUUID();
        String fileName = uploadFile.getName() + "_" + uuid;
        String filePath = uploadFile.getName();

        amazonS3(uploadFile, fileName);
        saveDB(fileName, filePath);
        removeNewFile(uploadFile);

        return uploadFile.getName();
    }

    private void saveDB(String fileName, String filePath) {
        S3Image s3Image = S3Image.builder()
                .imageName(fileName)
                .imagePath(filePath)
                .build();
        s3Repository.save(s3Image);
    }


    private String amazonS3(File uploadFile, String fileName){
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // remove temp file
    private void removeNewFile(File targetFile){
        if(targetFile.delete()){
            log.info("파일이 삭제되었습니다.");
        }else{
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    // file로 convert
    private Optional<File> convert(MultipartFile file) throws  IOException {
        File convertFile = new File(file.getOriginalFilename());
        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

}