package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import com.heroku.java.model.*;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

// import org.jscience.physics.amount.Amount;
// import org.jscience.physics.model.RelativisticModel;
// import javax.measure.unit.SI;
@SpringBootApplication
@Controller

public class ReservationController {
  private final DataSource dataSource;

  @Autowired
  public ReservationController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @GetMapping("/guestMakeRoomReservation")
  public String guestMakeRoomReservation() {
      return "guest/guestMakeRoomReservation";
  }

  @PostMapping("/guestMakeRoomReservation")
  public String guestMakeRoomReservation(HttpSession session, @ModelAttribute("guestMakeRoomReservation") reservation reservation, room room, service service, reservationService reservationService, roomReservation roomReservation, staff staff, Model model){

    try{
        Connection connection = dataSource.getConnection();
        // String sql = "INSERT INTO reservation() VALUES (?)";
        // final var statement = connection.prepareStatement(sql);

        String reservationID = reservation.getReservationID();
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        // Date dateStart = reservation.getAttribute("dateStart");
        // Date dateEnd = reservation.getAttribute("dateEnd");
        String dateStart = reservation.getDateStart();
        int totalAdult = reservation.getTotalAdult();
        int totalKids = reservation.getTotalKids();
        String reserveStatus = "Pending";
        int totalRoom = reservation.getTotalRoom();
        String totalPayment = "0.00";

        System.out.println("reservation date: " + dateStart);

    }
    catch (Exception e) {
        e.printStackTrace();
        return "redirect:/index";
    }
    return "guest/guestMakeRoomService";  
}
  
}
