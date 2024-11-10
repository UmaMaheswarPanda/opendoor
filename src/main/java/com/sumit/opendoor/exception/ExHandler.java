package com.sumit.opendoor.exception;

import java.io.StringWriter;

import com.amazonaws.services.iot.model.InvalidRequestException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.amazonaws.services.transcribe.model.BadRequestException;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice()
public class ExHandler {

	@ExceptionHandler(value = { Exception.class, InvalidRequestException.class })
	public void exceptionHandle(Exception exception, HttpServletResponse response) throws Exception {
		log.info("exception is: " + exception.getMessage());
		log.error("error occurred ", exception);
		log.info("Error type: "+exception.getClass().getSimpleName());
		if(exception instanceof HttpRequestMethodNotSupportedException) {
			log.info(" return when exception is: {}", exception.getMessage());
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The requested Method is not allowed.");
		}
		else if (exception instanceof NoHandlerFoundException) {
			log.info("No handler found for the request: {}", exception.getMessage());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested URL was not found on this server.");
		}
		else if(exception instanceof MissingServletRequestParameterException) {
			throw new BadRequestException("Bad request");
		}
		else if (exception.getMessage().contains("not supported")
				|| exception.getMessage().contains("Required request body is missing") || exception.getMessage().contains("Could not read document:")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid Request body");
		}
		else if (exception.getClass().getSimpleName().contains("MethodArgumentNotValidException")) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Validation Failed - Parameters can not be null or empty");
		}

		else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
		}
	}

	public synchronized static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		log.error(throwable.getMessage());
		return sw.getBuffer().toString();
	}


}
