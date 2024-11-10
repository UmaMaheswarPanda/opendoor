package com.sumit.opendoor.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.common.SftpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AwsOperationUtils {

    @Value("${aws.s3.bucket}")
    private String awsS3Bucket;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.endPoint}")
    private String awsS3Endpoint;

    @Value("${is.signed.url.active:false}")
    private boolean isSignedUrlActive;

    @Value("${signed.url.expiry.time.hours:24}")
    private int expiryTimeHrs;

    public String uploadSFTPFileToS3Bucket(String fileName, InputStream targetStream, ObjectMetadata metadata,
                                           String fileType) throws SftpException, IOException {
        try {

            PutObjectRequest putObjectRequest = new PutObjectRequest(awsS3Bucket,
                    fileType + "/" + fileName, targetStream, metadata);
            PutObjectResult result = this.amazonS3.putObject(putObjectRequest);
            log.info("uploadSFTPFileToS3Bucket response: " + result.getETag());
            return awsS3Endpoint + fileType + "/" + fileName;
        } catch (AmazonServiceException ex) {
            log.error("error occured ", ex);
            return null;
        }
    }

    public Map<String, String> getUniqueFileToBeOnboarded(String localFilePath, List<String> filesToBeonboarded,
                                                          String fileType) throws IOException {
        Map<String, String> files = new HashMap<>();
        for (String fileName : filesToBeonboarded) {
            try {
                if (!checkIfFileExist(fileName, fileType)) {
                    String s3Url = loadFilesToS3Bucket(localFilePath, fileName, fileType);
                    files.put(fileName, s3Url);
                } else {
                    log.info("File already exist in S3 " + fileType + "/" + fileName);
                }
            } catch (Exception ex) {
                log.error("get Unique File To Be Onboarded " , ex);
                log.info("S3 file check failed ! " + fileName);
            }
        }
        return files;
    }

    public String uploadFileToS3BucketFromAdmin(String fileName, File file, String fileType) {
        try {
            log.info("uploadFileToS3BucketFromAdmin file name" + file.getName());
            PutObjectRequest putObjectRequest = new PutObjectRequest(awsS3Bucket,
                    fileType + "/" + fileName, file);
            PutObjectResult result = this.amazonS3.putObject(putObjectRequest);
            log.info("uploadFileToS3BucketFromAdmin response: " + result.getETag());
            return awsS3Endpoint + fileType + "/" + fileName;
        } catch (Exception e) {
            log.error("error occurred while uploading file to s3 bucket", e);
            return null;
        }
    }


    public boolean checkIfFileExist(String fileName, String fileType) {
        try {
            String path = fileType + "/" + fileName;
            boolean ifExist = this.amazonS3.doesObjectExist(awsS3Bucket, path);
            return ifExist;
        } catch (Exception e) {
            log.error("exception occured while checking file exist ", e);
            return false;
        }
    }

    public String loadFilesToS3Bucket (String path, String fileName, String fileType) throws IOException {
        String s3Url = "";
        try {
            InputStream is = new FileInputStream(path + fileName);
            byte[] contentBytes = IOUtils.toByteArray(is);
            Long contentLength = Long.valueOf(contentBytes.length);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            InputStream targetStream = new ByteArrayInputStream(contentBytes);

            s3Url = uploadSFTPFileToS3Bucket(fileName, targetStream, metadata, fileType);
        } catch (Exception ex) {
            log.error("load Files To S3Bucket", ex);
            log.info("S3 file loading failed ! " + fileName);
            s3Url = "";
        }
        return s3Url;

    }

    public String getS3SignedUrl(String s3Url) {
        log.info("isSignedUrlActive: {}, expiryTimeHrs: {}, s3Url : {}" , isSignedUrlActive, expiryTimeHrs, s3Url);
        if (isSignedUrlActive) {
            try {
                expiryTimeHrs = expiryTimeHrs == 0 ? 24 : expiryTimeHrs;
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR_OF_DAY, expiryTimeHrs);
                log.info("Converting to signed URL");
                AmazonS3URI s3uri = new AmazonS3URI(new URI(s3Url));
                URL signedFile = amazonS3.generatePresignedUrl(s3uri.getBucket(), s3uri.getKey(), cal.getTime(),
                        HttpMethod.GET);
                return signedFile.toString();
            } catch (Exception e) {
                log.error("unable To generate Signed Url", e);
            }
        }
        return s3Url;
    }

}
