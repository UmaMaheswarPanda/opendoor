package com.sumit.opendoor.controller;

import com.google.gson.Gson;
import com.sumit.opendoor.controller.response.ProcessFileResponse;
import com.sumit.opendoor.service.ProcessFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@Scope("prototype")
@RequestMapping(value = "/internal/partner")
@Tag(name = "File Process APIs" , description = "APIs for processing file")
public class ProcessFileController {

    @Autowired
    ProcessFileService processFileService;

    Gson gson = new Gson();

    @Operation(summary = "This API is used to add a terminal ID to a specific Partner", responses = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of API response", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProcessFileResponse.class), examples = {
                    @ExampleObject(name = "Success response", value = "{\"status\":\"Success\",\"respMessage\":\"Success\",\"statusCode\":\"S101\",\"respDetails\":\"s3Url\"}"),
                    @ExampleObject(name = "Partial Success response", value = "{\"status\":\"Failed\",\"respMessage\":\"Partial_Success\",\"statusCode\":\"S102\",\"respDetails\":\"s3Url\"}"),
                    @ExampleObject(name = "Action Validation Failed", value = "{\"status\":\"Failed\",\"respMessage\":\"Action Validation Failed\",\"statusCode\":\"S102\",\"respDetails\":null}"),
                    @ExampleObject(name = "Partner Name Missing", value = "{\"status\":\"Failed\",\"respMessage\":\"partnerName  is a Mandatory  parameter\",\"statusCode\":\"S102\"}"),
                    @ExampleObject(name = "Invalid Status", value = "{\"status\":\"Failed\",\"respMessage\":\"Partner Status Validation Failed\",\"statusCode\":\"S102\",\"respDetails\":null}"),
                    @ExampleObject(name = "Failed", value = "{\"Status\": \"Failed\", \"respMessage\": \"Sorry! Some internal Server Error occured, Please try after some time\", \"statusCode\": \"E101\"}"),

            })),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"Status\": \"Failed\", \"respMessage\": \"Sorry! Some internal Server Error occurred, Please try after some time\",\"statusCode\": \"E101\"}"))) })

    @PostMapping(value = "/process-file")
    public ProcessFileResponse addUpdateTidPartner(@RequestHeader(value = "fileType") String fileType, @RequestBody MultipartFile multipartFile) {
        ProcessFileResponse processFileResponse;
        try {
            log.info("Got request for process-file API: {}", multipartFile.getName());
            processFileResponse = processFileService.processAndUploadFile(multipartFile, fileType);
        } catch (Exception e) {
            log.error("Exception occurred while adding tid to a specific partner :",e);
            throw e;
        }
        log.info("process-file Response : {}", gson.toJson(processFileResponse));
        return processFileResponse;
    }
}
