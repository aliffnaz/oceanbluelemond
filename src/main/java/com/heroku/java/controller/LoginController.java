package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.*;

import jakarta.servlet.http.HttpSession;

import java.sql.*;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

import java.util.List;

@Controller
public class LoginController {
    private final DataSource dataSource;

    @Autowired
    public LoginController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/guestLogin")
    public String guestLogin(HttpSession session, Model model, @ModelAttribute("guestLogin") String email,
            String password, Model Model, guest guest) {

        try {
            // String returnPage = null;
            Connection connection = dataSource.getConnection();

            String sql = "SELECT guesticnumber, guestname, guestphonenumber, guestgender, guestreligion, guestrace, guestaddress, guestemail, guestpassword FROM public.guest WHERE guestemail=?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, email);

            final var resultSet = statement.executeQuery();

            System.out.println("guest pass : " + password);
            System.out.println("guest : " + email);

            if (resultSet.next()) {

                int userid = resultSet.getInt("guestICNumber");
                String fullname = resultSet.getString("guestname");
                String guestEmail = resultSet.getString("guestemail");
                String guestPassword = resultSet.getString("guestpassword");

                System.out.println(fullname);
                // if they're admin
                System.out.println("Email : " + guestEmail.equals(email) + " | " + email);
                System.out.println("Password status : " + guestPassword.equals(password));

                if (guestEmail.equals(email)
                        && guestPassword.equals(password)) {

                    session.setAttribute("guestname", fullname);
                    session.setAttribute("guestICNumber", userid);

                    // if (staffsrole.equals("admin")) {

                    // session.setAttribute("staffsrole","admin" );
                    // connection.close();
                    // // debug
                    // System.out.println("admin name : " + fullname);
                    // System.out.println("admin id: " + userid);
                    // System.out.println("admin role: " + staffsrole);
                    // return "redirect:/stafforder";

                    // } else if(staffsrole.equals("baker")){

                    // session.setAttribute("staffsrole","baker" );
                    // connection.close();

                    // // debug
                    // System.out.println("staff name : " + fullname);
                    // System.out.println("staff id: " + userid);
                    // System.out.println("staff role: " + staffsrole);
                    // return "redirect:/stafforder";
                    // }
                }
            }

            connection.close();
            return "redirect:/login?invalidUsername&Password";

        } catch (SQLException sqe) {
            System.out.println("Error Code = " + sqe.getErrorCode());
            System.out.println("SQL state = " + sqe.getSQLState());
            System.out.println("Message = " + sqe.getMessage());
            System.out.println("printTrace /n");
            sqe.printStackTrace();

            return "redirect:/login?error";
        } catch (Exception e) {
            System.out.println("E message : " + e.getMessage());
            return "redirect:/login?error";
        }

    }

    @PostMapping("/stafflogin")
    public String stafflogin(HttpSession session, Model model, @ModelAttribute("stafflogin") String email,
            String password, Model Model, staff staff) {

        try {
            // String returnPage = null;
            Connection connection = dataSource.getConnection();

            String sql = "SELECT stafficnumber, staffname, staffgender, staffphonenumber, staffrace, staffreligion, staffmaritalstatus, staffaddress, staffrole, staffstatus, managericnumber, staffemail, staffpassword FROM public.staff WHERE staffemail=?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, email);

            final var resultSet = statement.executeQuery();

            System.out.println("staff pass : " + password);
            System.out.println("staff : " + email);

            if (resultSet.next()) {

                int userid = resultSet.getInt("staffICNumber");
                String fullname = resultSet.getString("staffName");
                String guestEmail = resultSet.getString("staffEmail");
                String guestPassword = resultSet.getString("staffPassword");
                String staffrole = resultSet.getString("staffrole");

                System.out.println(fullname);
                // if they're admin
                System.out.println("Email : " + guestEmail.equals(email) + " | " + email);
                System.out.println("Password status : " + guestPassword.equals(password));

                if (guestEmail.equals(email)
                        && guestPassword.equals(password)) {

                    session.setAttribute("staffName", fullname);
                    session.setAttribute("staffICNumber", userid);

                    if (staffrole.equals("manager")) {

                        session.setAttribute("staffRole", "manager");
                        connection.close();
                        // debug
                        System.out.println("admin name : " + fullname);
                        System.out.println("admin id: " + userid);
                        System.out.println("admin role: " + staffrole);
                        return "redirect:/managerHome";

                    } else if (staffrole.equals("staff")) {

                        session.setAttribute("staffRole", "staff");
                        connection.close();

                        // debug
                        System.out.println("staff name : " + fullname);
                        System.out.println("staff id: " + userid);
                        System.out.println("staff role: " + staffrole);
                        return "redirect:/staffHome";
                    }

                }
            }

            connection.close();
            return "redirect:/login?invalidUsername&Password";

        } catch (SQLException sqe) {
            System.out.println("Error Code = " + sqe.getErrorCode());
            System.out.println("SQL state = " + sqe.getSQLState());
            System.out.println("Message = " + sqe.getMessage());
            System.out.println("printTrace /n");
            sqe.printStackTrace();

            return "redirect:/login?error";
        } catch (Exception e) {
            System.out.println("E message : " + e.getMessage());
            return "redirect:/login?error";
        }

    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}