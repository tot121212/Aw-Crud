package com.crud_project.crud.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.crud_project.crud.controller.SessionKeys;
import com.crud_project.crud.dvo.PageState;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionInitFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false); // don't create if it doesn't exist
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (session != null && auth != null && auth.isAuthenticated()) {
            Object mutex = WebUtils.getSessionMutex(session);

            synchronized (mutex) {
                if (session.getAttribute(SessionKeys.CUR_USER_PAGE_STATE) == null) {
                    session.setAttribute(SessionKeys.CUR_USER_PAGE_STATE, new PageState());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
