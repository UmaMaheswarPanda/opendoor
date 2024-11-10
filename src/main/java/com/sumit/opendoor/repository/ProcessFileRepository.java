package com.sumit.opendoor.repository;

import com.sumit.opendoor.entity.FileProcessingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessFileRepository extends JpaRepository<FileProcessingInfo, Integer> {
}
