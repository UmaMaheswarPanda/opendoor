
package com.sumit.opendoor.interceptors;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class MyCustomInterceptor implements HandlerInterceptor {

	@Value("${validate.session:false}")
    private boolean validateSession;
	@Value("${server.servlet.context-path}")
	private String contextPath;
	private static final String SESSION_NOT_PRESENT = "Session Id is not present";
	private static final String SESSION_NOT_AUTHRORIZED = "Session is UnAuthorized";
	@Autowired
	private Environment env;

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (validateSession(request, response, handler)) {
			return true; // readDataFromRedis.checkModulePermission(0, null, null); }
		}
		return false;
	}

	private boolean validateSession(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {
	   if(!validateSession) {
	     return true;
	   }
		String sessionId = request.getHeader("sessionId");
		if (sessionId == null || sessionId.isEmpty()) {
			log.info("Endpoint : " + request.getRequestURI() + " : " + SESSION_NOT_PRESENT);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, SESSION_NOT_PRESENT);
			return false;
		}
		return true;
	}


	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}


	private void sendInvalidResponse(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
	}

}
