package com.heroku.java.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;

//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.reservationModel;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.ResultSet;
// import java.util.ArrayList;
// import java.util.Map;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.RequestBody;


// import org.jscience.physics.amount.Amount;
// import org.jscience.physics.model.RelativisticModel;
// import javax.measure.unit.SI;
@SpringBootApplication
@Controller


public class RegisterController{

    private final DataSource dataRegister;


@Autowired
public RegisterController(DataSource dataRegister){
    this.dataRegister = dataRegister;
}

@PostMapping("/guestRegister")
public String guestRegisterAccount(HttpSession session, @ModelAttribute("guestRegister") reservationModel reserve) {

    try{
Connection conn = dataRegister.getConnection();
PreparedStatement ps = conn.prepareStatement("INSERT INTO `guest`(`guestICNumber`, `guestName`, `guestPhoneNumber`, `guestGender`, `guestReligion`, `guestRace`, `guestAddress`) VALUES ('[value-1]','[value-2]','[value-3]','[value-4]','[value-5]','[value-6]','[value-7]')");
// getString from bean
ResultSet insertGuest = ps.executeQuery();

  return "redirect:/guestLogin";
  
    }catch(SQLException e){
        e.printStackTrace();
       return "redirect:/guestRegister";
    }


  
}


}