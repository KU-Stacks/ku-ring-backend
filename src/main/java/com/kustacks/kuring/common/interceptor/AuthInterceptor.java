package com.kustacks.kuring.common.interceptor;

import com.kustacks.kuring.common.annotation.CheckSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if(!(handler instanceof HandlerMethod)) {
            // return true이면  Controller에 있는 메서드가 아니므로, 그대로 컨트롤러로 진행
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        CheckSession checkSession = handlerMethod.getMethodAnnotation(CheckSession.class);
        if(checkSession == null) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if(session != null) {
            log.debug("sessionId = {}", session.getId());
            log.debug("lastAccessedTime = {}", session.getLastAccessedTime());
        }

        boolean isSessionFromCookie = session != null;
        boolean isSessionRequired = checkSession.isSessionRequired();
        
        // 세션이 있어야 하는 경우 - 없으면 로그인 페이지로 이동
        if(isSessionRequired) {
            log.debug("isSessionFromCookie = {}", isSessionFromCookie);
            if(isSessionFromCookie) {
                return true;
            } else {
                response.sendRedirect("/admin/login");
                return false;
            }
        }
        // 세션이 없어야 하는 경우(로그인 페이지) - 있으면 이전 페이지로 이동
        else {
            if(isSessionFromCookie) {
                response.setStatus(401);
                response.sendRedirect("/admin/dashboard");
                return false;
            } else {
                return true;
            }
        }
    }
}
