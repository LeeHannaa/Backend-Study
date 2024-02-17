package com.example.s3image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.s3image.domain.S3Image;
import com.example.s3image.dto.request.S3ImageReqeust;
import com.example.s3image.dto.response.S3ImageResponse;
import com.example.s3image.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
                // multipartFile이 없을 때 실행이 안되었을 때
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return uploadFileToS3(uploadFile);
    }

    private String uploadFileToS3(File uploadFile){
        // random id 생성 후 파일 이름
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
        System.out.println(s3Image.getImageName());
        System.out.println(s3Image.getImagePath());
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
            // createNewFile : 빈 파일 생성 -> 같은 이름을 가진 파일이 없으면 true / 있으면 false
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                // FileOutputStream 무조건 파일 생성 -> 존재하는 파일은 덮어쓰기
                fos.write(file.getBytes());
                // 입력받은 내용을 파일 내용으로 기록
            }
            System.out.println(file);
            return Optional.of(convertFile);
            // null이 올 수 있는 값을 감싸는 Wrapper 클래스 / 참조하더라도 NPE가 발생하지 않도록 도와준다.
        }
        return Optional.empty();
    }

    // --------- 이미지 RUD ---------

    // 전체 이미지 불러오기
    public List<S3ImageResponse> findAll(){
        List<S3Image> s3Images = s3Repository.findAll();
        List<S3ImageResponse> responses = new ArrayList<>();

        for (S3Image img : s3Images) {
            S3ImageResponse response = new S3ImageResponse();
            response.setKeyId(img.getKeyId());
            response.setImagePath(img.getImagePath());
            response.setCreateDate(img.getCreateDate());

            responses.add(response);
        }
        return responses;
    }

    // keyId로 특정 이미지만 불러오기
    public S3Image findByKeyId(Long keyId) {
        return s3Repository.findById(keyId).get();
    }


}