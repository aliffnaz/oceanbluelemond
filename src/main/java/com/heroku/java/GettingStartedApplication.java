package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

// for guest

    @GetMapping("/")
    public String index() {
        return "index_logout";
    }

    @GetMapping("/logoutViewRoom")
    public String logoutViewRoom() {
        return "logoutViewRoom";
    }

    @GetMapping("/index")
    public String index1(@RequestParam(name = "success", required = false) Boolean success, HttpSession session) {
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        return "index";
    }

    @GetMapping("/guestLogin")
    public String guestLogin() {
        return "guest/guestLogin";
    }

    @GetMapping("/index_logout")
    public String index_logout() {
        return "index_logout";
    }

        @GetMapping("/guestViewRoom")
    public String guestViewRoom(HttpSession session) {
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        return "guest/guestViewRoom";
    }

    // for staff

    @GetMapping("/staffHome")
    public String staffHome(@RequestParam(name = "success", required = false) Boolean success, HttpSession session) {
        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
        return "staff/staffHome";
    }

    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "staff/staffLogin";
    }

    @GetMapping("/staffAddRoom")
    public String staffAddRoom(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "staff/staffAddRoom";
}

@GetMapping("/staffGenerateReport")
public String staffGenerateReport(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "staff/staffGenerateReport";
}

// for manager

// @GetMapping("/managerHome")
// public String managerHome(@RequestParam(name = "success", required = false) Boolean success, HttpSession session) {
//     String staffICNumber = (String) session.getAttribute("staffICNumber") ;
//     return "manager/managerHome";
// }

@GetMapping("/managerAddRoom")
public String managerAddRoom(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "manager/managerAddRoom";
}

@GetMapping("/managerAddStaff")
public String managerAddStaff(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "manager/managerAddStaff";
}

@GetMapping("/managerGenerateReport")
public String managerGenerateReport(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "manager/managerGenerateReport";
}

@GetMapping("/reservationReport")
public String reservationReport(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "manager/reservationReport";
}

@GetMapping("/salesReport")
public String salesReport(HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    return "manager/salesReport";
}


    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
