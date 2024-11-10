package com.sumit.opendoor.service;

import com.sumit.opendoor.controller.response.ProcessFileResponse;
import com.sumit.opendoor.entity.FileProcessingInfo;
import com.sumit.opendoor.repository.ProcessFileRepository;
import com.sumit.opendoor.utils.AwsOperationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class ProcessFileService {

    @Autowired
    private ProcessFileRepository processFileRepository;

    @Autowired
    private AwsOperationUtils awsOperationUtils;



    public ProcessFileResponse processAndUploadFile(MultipartFile multipartFile, String fileType) {
        try {
            log.info("Got request for process-file API: {}", multipartFile.getName());
            File File = convertMultipartFileToFile(multipartFile);
            String fileName = fileType + "/" + File.getName();
            String s3Url = awsOperationUtils.uploadFileToS3BucketFromAdmin(fileName, File, fileType);
            String s3SignedUrl =  awsOperationUtils.getS3SignedUrl(s3Url);
            SaveProcessedFileInDB(fileName, s3Url,fileType, "Success");
            return frameProcessFileResponse(s3SignedUrl,"Success","Success");
        } catch (Exception e) {
            log.error("Exception occurred while processing and uploading file :",e);
            return null;
        }
    }

    private ProcessFileResponse frameProcessFileResponse(String s3SignedUrl, String status, String message) {
        ProcessFileResponse processFileResponse = new ProcessFileResponse();
        processFileResponse.setStatus(status);
        processFileResponse.setRespMessage(message);
        processFileResponse.setStatusCode("S101");
        processFileResponse.setRespDetails(s3SignedUrl);
        return processFileResponse;
    }

    private FileProcessingInfo SaveProcessedFileInDB(String fileName, String s3Url, String fileType, String status) {
        FileProcessingInfo fileProcessingInfo = new FileProcessingInfo();
        fileProcessingInfo.setSourceFileName(fileName);
        fileProcessingInfo.setRemarks("Processed File");
        fileProcessingInfo.setSourceFilePath(s3Url);
        fileProcessingInfo.setStatus(status);
        fileProcessingInfo.setFileType(fileType);
        fileProcessingInfo.setS3UploadedStatus(status);
        return processFileRepository.save(fileProcessingInfo);
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File convFile = File.createTempFile("temp", multipartFile.getOriginalFilename());
        convFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(multipartFile.getBytes());
        }
        return convFile;
    }
}
