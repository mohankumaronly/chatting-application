package com.rockranger.chat.auth.application;

import com.rockranger.chat.common.config.AppProperties;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.session.application.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService {

    private final SessionService sessionService;
    private final AppProperties appProperties;

    public LogoutService(SessionService sessionService, AppProperties appProperties) {
        this.sessionService = sessionService;
        this.appProperties = appProperties;
    }

    @Transactional
    public void logout(CurrentUser user, HttpServletResponse response) {
        if (user != null && user.getSessionId() != null) {
            sessionService.revokeSession(user.getSessionId());
        }
        if (response != null) {
            clearRefreshCookie(response);
        }
    }

    @Transactional
    public void logoutAll(CurrentUser user, HttpServletResponse response) {
        if (user != null && user.getUserId() != null) {
            sessionService.revokeAllUserSessions(user.getUserId());
        }
        if (response != null) {
            clearRefreshCookie(response);
        }
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(appProperties.getCookie().isSecure());
        cookie.setPath(appProperties.getCookie().getPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
