package com.example.s3image.repository;

import com.example.s3image.domain.S3Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface S3Repository extends JpaRepository<S3Image, Long> {
}
