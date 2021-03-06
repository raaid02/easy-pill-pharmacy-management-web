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
import model.UserFacade;
import utilities.PasswordHasher;
import utilities.UserEmailValidator;
import utilities.UserIdGenerator;

/**
 *
 * @author Topsy
 */
public class RegisterServlet extends HttpServlet {

    @EJB
    private UserEmailValidator userEmailValidator;
    
    @EJB
    private PasswordHasher passwordHasher;
    @EJB
    private UserIdGenerator userIdGenerator;
    @EJB
    private UserFacade userFacade;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userRole = request.getParameter("command");

        if (userEmailValidator.emailExists(request.getParameter("email"))) {
            request.setAttribute("firstName", request.getParameter("firstName"));
            request.setAttribute("lastName", request.getParameter("lastName"));
            request.setAttribute("email", request.getParameter("email"));
            request.setAttribute("address", request.getParameter("address"));
            request.setAttribute("password", request.getParameter("password"));
            request.setAttribute("contact", request.getParameter("contact"));
            request.setAttribute("expertise", request.getParameter("expertise"));
            request.setAttribute("ErrorMessage", "Email already exists, Please try a different email");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/" + userRole + "SignUpPage.jsp");
            dispatcher.forward(request, response);
        } else {
            User user = new User();
            user.setFirstName(request.getParameter("firstName"));
            user.setLastName(request.getParameter("lastName"));
            user.setEmail(request.getParameter("email"));
            user.setPassword(passwordHasher.getHash(request.getParameter("password")));
            user.setAddress(request.getParameter("address"));
            user.setContactNo(request.getParameter("contact"));
            user.setUserRole(userRole);
            user.setUserId(userIdGenerator.generateNumber());

            try {
                userFacade.create(user);
            } catch (Exception e) {
                System.out.println("Unable to add User!!");
                System.out.println("Exception: " + e);
                request.getRequestDispatcher("accountCreationFailed.jsp").include(request, response);
            }
            request.getRequestDispatcher("accountCreationSuccess.jsp").include(request, response);

        }

    }

}
