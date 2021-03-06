/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import entities.User;
import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.UserFacade;
import utilities.PasswordHasher;
import utilities.SessionDetails;

/**
 *
 * @author Topsy
 *
 */
public class LoginServlet extends HttpServlet {

    @EJB
    private PasswordHasher passwordHasher;

    @EJB
    private UserFacade userFacade;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        User user = userFacade.getAuthenticatedUser(email);

        if (user == null) {
            request.setAttribute("ErrorMessage", "Invalid credentials, Please sign up if u do not have an account.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/loginPage.jsp");
            dispatcher.forward(request, response);
        } else if (!passwordHasher.getHash(password).equalsIgnoreCase(user.getPassword())) {
            request.setAttribute("ErrorMessage", "Password is incorrect. Please Try Again.");
            request.setAttribute("email", email);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/loginPage.jsp");
            dispatcher.forward(request, response);
        } else {
            SessionDetails.setUserId(user.getUserId());
            SessionDetails.setUserEmail(email);
            SessionDetails.setUserFirstName(user.getFirstName());
            SessionDetails.setUserLastName(user.getLastName());
            SessionDetails.setUserRole(user.getUserRole());

            switch (user.getUserRole()) {
                case "admin": {
                    response.sendRedirect("AdminController");
                    break;
                }
                case "customer": {
                    response.sendRedirect("CustomerController");
                    break;
                }
                case "pharmacist": {
                    response.sendRedirect("AdminController");
                    break;
                }
                case "doctor": {
                    response.sendRedirect("CustomerController");
                    break;
                }
            }
        }

    }

}
