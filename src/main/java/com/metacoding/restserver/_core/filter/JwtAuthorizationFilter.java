package com.metacoding.restserver._core.filter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metacoding.restserver._core.auth.LoginUser;
import com.metacoding.restserver._core.util.JwtUtil;
import com.metacoding.restserver._core.util.Resp;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.PrintWriter;

// 요청 헤더의 JWT(Json Web Token)를 파싱 및 검증하여 사용자 인증 처리
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements Filter {

    private final JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println("jooho: JwtAuthorizationFilter 작동됨 ~~~~~~~~~~~~~~~~~~~~~~~~~");

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String jwt = request.getHeader("Authorization");

        if (jwt == null) {
            onError(response, "토큰없어");
            return;
        }

        if (!jwt.startsWith("Bearer ")) {
            onError(response, "프로토콜 잘못됨 혹은 공백일 수 없다");
            return;
        }

        try {
            LoginUser loginUser = jwtUtil.verify(jwt);

            HttpSession session = request.getSession();
            session.setAttribute("sessionUser", loginUser);
        } catch (JWTDecodeException jwtDecodeException) {
            onError(response, "토큰 검증 실패");
            return;
        }

        chain.doFilter(request, response);
    }

    private void onError(HttpServletResponse response, String msg) {
        try {
            String responseBody = new ObjectMapper().writeValueAsString(Resp.fail(msg));

            response.setStatus(401);
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.println(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}