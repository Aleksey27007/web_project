package com.app.totalizator.servlet;

import com.app.totalizator.model.User;
import com.app.totalizator.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;;

@WebServlet(name = "registrationServlet", urlPatterns = "/register")
public class RegisterController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger();
    private static final UserServiceImpl userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        request.getRequestDispatcher("templates/register.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.setAttribute("username", request.getParameter("username"));
//        request.setAttribute("email", request.getParameter("email"));
//        request.setAttribute("firstName", request.getParameter("firstName"));
//        request.setAttribute("lastName", request.getParameter("lastName"));
        System.out.println("syda doshlo");
        if (userService.checkUserConfirmPasswordFromRequest(request)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("templates/register.html").forward(request, response);
            return;
        }
        System.out.println("i syda doshlo");
        if (userService.checkUserFromRequest(request)) {
            request.setAttribute("error", "All required fields must be filled");
            request.getRequestDispatcher("templates/register.html").forward(request, response);
            return;
        }
        System.out.println("i i syda doshlo");
        try {
            User user = userService.createUserFromRequest(request);

            User registeredUser = userService.register(user);
            logger.info("New user registered: {}", registeredUser.getUsername());
            System.out.println(request.getContextPath() + "/login");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed: {}", e.getMessage());

            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("templates/register.html").forward(request, response);
        }
    }
}