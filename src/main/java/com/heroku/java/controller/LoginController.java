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
    public String guestLogin(@RequestParam(name = "success", required = false) Boolean success, String email, String password, HttpSession session, Model model, guest guest) {

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

                String guestICNumber = resultSet.getString("guestICNumber");
                String guestName = resultSet.getString("guestname");
                String guestEmail = resultSet.getString("guestemail");
                String guestPassword = resultSet.getString("guestpassword");

                System.out.println(guestName);
                // if they're admin
                System.out.println("Email : " + guestEmail.equals(email) + " | " + email);
                System.out.println("Password status : " + guestPassword.equals(password));

                if (guestEmail.equals(email) && guestPassword.equals(password)) {

                    session.setAttribute("guestname", guestName);
                    session.setAttribute("guestICNumber", guestICNumber);
                    
                    return "redirect:/index?success=true";
                }
            }

            connection.close();
            return "redirect:/guestLogin?invalidUsername&Password";

        } catch (SQLException sqe) {
            System.out.println("Error Code = " + sqe.getErrorCode());
            System.out.println("SQL state = " + sqe.getSQLState());
            System.out.println("Message = " + sqe.getMessage());
            System.out.println("printTrace /n");
            sqe.printStackTrace();

            return "redirect:/guestLogin?error";

        } catch (Exception e) {
            System.out.println("E message : " + e.getMessage());
            return "redirect:/guestLogin?error";
        }

    }

    @PostMapping("/staffLogin")
    public String staffLogin(@RequestParam(name = "success", required = false) Boolean success, HttpSession session, Model model, String email, String password, Model Model, staff staff) {

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

                String stafficnumber = resultSet.getString("staffICNumber");
                String staffname = resultSet.getString("staffName");
                String guestEmail = resultSet.getString("staffEmail");
                String guestPassword = resultSet.getString("staffPassword");
                String staffrole = resultSet.getString("staffrole");
                String staffStatus = resultSet.getString("staffStatus");

                System.out.println(staffname);
                // if they're admin
                System.out.println("Email : " + guestEmail.equals(email) + " | " + email);
                System.out.println("Password status : " + guestPassword.equals(password));

                if (guestEmail.equals(email)
                        && guestPassword.equals(password)
                        && staffStatus.equalsIgnoreCase("Employed") ){

                    session.setAttribute("staffName", staffname);
                    session.setAttribute("staffICNumber", stafficnumber);

                    if (staffrole.equals("Manager")) {
                        session.setAttribute("staffRole", "Manager");
                        connection.close();
                        
                        // debug
                        System.out.println("manager name : " + staffname);
                        System.out.println("manager id: " + stafficnumber);
                        System.out.println("manager role: " + staffrole);
                        return "redirect:/managerHome?success=true";

                    } else {

                        session.setAttribute("staffRole", "Staff");
                        connection.close();

                        // debug
                        System.out.println("staff name : " + staffname);
                        System.out.println("staff id: " + stafficnumber);
                        System.out.println("staff role: " + staffrole);
                        return "redirect:/staffHome?success=true";
                    }

                }
            }

            connection.close();
            return "redirect:/staffLogin?invalidUsername&Password";

        } catch (SQLException sqe) {
            System.out.println("Error Code = " + sqe.getErrorCode());
            System.out.println("SQL state = " + sqe.getSQLState());
            System.out.println("Message = " + sqe.getMessage());
            System.out.println("printTrace /n");
            sqe.printStackTrace();

            return "redirect:/staffLogin?error";
        } catch (Exception e) {
            System.out.println("E message : " + e.getMessage());
            return "redirect:/staffLogin?error";
        }

    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
