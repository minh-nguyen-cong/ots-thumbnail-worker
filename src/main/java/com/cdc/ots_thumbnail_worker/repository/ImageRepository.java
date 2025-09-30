package com.cdc.ots_thumbnail_worker.repository;

import com.cdc.ots_thumbnail_worker.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    // You can add custom query methods here if needed in the future
}
