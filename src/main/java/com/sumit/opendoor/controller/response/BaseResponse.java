package com.sumit.opendoor.controller.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class BaseResponse {
  
	@Schema(example = "[\"Success\", \"Failed\"]", description = "api response status")
	private String status;
	
	@Schema(example = "Success", description ="api response message")
	private String respMessage;
	
	@Schema(example = "[\"S101\", \"S102\",\"S111\",\"L131\",\"P139\",\"L130\",\"L120\",\"L129\"]", description = "unique status code given for api response")
	private String statusCode;
}