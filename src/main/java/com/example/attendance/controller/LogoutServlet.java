package com.example.attendance.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;  // ← 忘れずに
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")  // ← このURLでアクセス可能
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate(); // セッション破棄
        }

        // ログイン画面にリダイレクト
        resp.sendRedirect("login.jsp");
    }
}
