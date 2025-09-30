package com.cdc.ots_thumbnail_worker.repository;

import com.cdc.ots_thumbnail_worker.entity.Image;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByGcsPath(String gcsPath);
}
