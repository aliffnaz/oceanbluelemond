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

import com.heroku.java.model.staff;

import jakarta.servlet.http.HttpSession;

import java.sql.*;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

import java.util.List;

@Controller
public class StaffController {
    private final DataSource dataSource;

    @Autowired
    public StaffController(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @GetMapping("/managerStaffList")
    public String managerStaffList(Model model, HttpSession session) {
	String userStaffICNumber = (String) session.getAttribute("staffICNumber") ;
        List<staff> staffs = new ArrayList<staff>();
        // Retrieve the logged-in room's role from the session (syahir punya nih)
        //String staffsrole = (String) session.getAttribute("staffsrole");
        //System.out.println("staffrole managerRoomList : " + staffsrole);
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT stafficnumber, staffname, staffgender, staffphonenumber, staffrace, staffreligion, staffmaritalstatus, staffaddress, staffrole, staffstatus, managerICNumber FROM public.staff order by staffname";
            final var statement = connection.prepareStatement(sql);
            //statement.setString(1, "baker"); (syahir punya nih)
            final var resultSet = statement.executeQuery();
            System.out.println("pass try managerStaffList >>>>>");

            while (resultSet.next()) {
                String staffICNumber = resultSet.getString("stafficnumber");
                String staffName = resultSet.getString("staffname");
                String staffGender = resultSet.getString("staffgender");
                String staffPhoneNumber = resultSet.getString("staffphonenumber");
                String staffRace = resultSet.getString("staffrace");
                String staffReligion = resultSet.getString("staffreligion");
                String staffMaritalStatus = resultSet.getString("staffmaritalstatus");
                String staffAddress = resultSet.getString("staffaddress");
                String staffRole = resultSet.getString("staffrole");
                String staffStatus = resultSet.getString("staffstatus");
                String managerICNumber = resultSet.getString("managerICNumber");
                
                staff staff = new staff();
                staff.setStaffICNumber(staffICNumber);
                staff.setStaffName(staffName);
                staff.setStaffGender(staffGender);
                staff.setStaffPhoneNumber(staffPhoneNumber);
                staff.setStaffRace(staffRace);
                staff.setStaffReligion(staffReligion);  
                staff.setStaffMaritalStatus(staffMaritalStatus);
                staff.setStaffAddress(staffAddress);
                staff.setStaffRole(staffRole);
                staff.setStaffStatus(staffStatus);
                staff.setManagerICNumber(managerICNumber);                

                staffs.add(staff);
                model.addAttribute("staffs", staffs);
                //model.addAttribute("isAdmin", staffsrole != null && staffsrole.equals("admin")); // Add isAdmin flag to the modelF (syahir punya gak)

            }

            connection.close();

        return "manager/managerStaffList";
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            return "error";
        }
        
    }

    @PostMapping("/managerAddStaff")
    public String managerAddStaff(@ModelAttribute("managerAddStaff")staff staff, HttpSession session){
	String userStaffICNumber = (String) session.getAttribute("staffICNumber") ;
        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO public.staff(stafficnumber,staffname,staffgender,staffphonenumber,staffrace,staffreligion,staffmaritalstatus,staffaddress,staffrole,staffstatus,managerICNumber,staffemail,staffpassword) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            final var statement = connection.prepareStatement(sql);

            String staffICNumber = staff.getStaffICNumber();
            String staffName = staff.getStaffName();
            String staffGender = staff.getStaffGender();
            String staffPhoneNumber = staff.getStaffPhoneNumber();
            String staffRace = staff.getStaffRace();
            String staffReligion = staff.getStaffReligion();
            String staffMaritalStatus = staff.getStaffMaritalStatus();
            String staffAddress = staff.getStaffAddress();
            String staffRole = staff.getStaffRole();
            String staffStatus = "Employed";
            String managerICNumber = userStaffICNumber;
            String staffEmail = staff.getStaffEmail();
            String staffPassword = staff.getStaffPassword();
            
            
            statement.setString(1, staffICNumber);
            statement.setString(2, staffName);
            statement.setString(3, staffGender);
            statement.setString(4, staffPhoneNumber);
            statement.setString(5, staffRace);
            statement.setString(6, staffReligion);
            statement.setString(7, staffMaritalStatus);
            statement.setString(8, staffAddress);
            statement.setString(9, staffRole);
            statement.setString(10, staffStatus);
            statement.setString(11, managerICNumber);
            statement.setString(12, staffEmail);
            statement.setString(13, staffPassword);

            statement.executeUpdate();
            
             System.out.println("Staff IC Number : "+staffICNumber);
            
            connection.close();
                
                } catch (Exception e) {
                    e.printStackTrace();
                    return "redirect:/index";
                }
            return "redirect:/managerStaffList";
        }


         @GetMapping("/managerViewStaff")
         public String managerViewStaff(@RequestParam("staffICNumber") String staffICNumber, Model model, HttpSession session) {
	 String userStaffICNumber = (String) session.getAttribute("staffICNumber") ;
           System.out.println("Staff IC Number : " + staffICNumber);
           try {
             Connection connection = dataSource.getConnection();
             String sql = "SELECT stafficnumber, staffname, staffgender, staffphonenumber, staffrace, staffreligion, staffmaritalstatus, staffaddress, staffrole, staffstatus, managerICNumber, staffemail FROM public.staff where stafficnumber = ?";
             final var statement = connection.prepareStatement(sql);
             statement.setString(1, staffICNumber);
             final var resultSet = statement.executeQuery();
         
             if (resultSet.next()) {
                String staffName = resultSet.getString("staffname");
                String staffGender = resultSet.getString("staffgender");
                String staffPhoneNumber = resultSet.getString("staffphonenumber");
                String staffRace = resultSet.getString("staffrace");
                String staffReligion = resultSet.getString("staffreligion");
                String staffMaritalStatus = resultSet.getString("staffmaritalstatus");
                String staffAddress = resultSet.getString("staffaddress");
                String staffRole = resultSet.getString("staffrole");
                String staffStatus = resultSet.getString("staffstatus");
                String managerICNumber = resultSet.getString("managerICNumber");
                String staffEmail = resultSet.getString("staffemail");
         
                staff staff = new staff();
                staff.setStaffICNumber(staffICNumber);
                staff.setStaffName(staffName);
                staff.setStaffGender(staffGender);
                staff.setStaffPhoneNumber(staffPhoneNumber);
                staff.setStaffRace(staffRace);
                staff.setStaffReligion(staffReligion);  
                staff.setStaffMaritalStatus(staffMaritalStatus);
                staff.setStaffAddress(staffAddress);
                staff.setStaffRole(staffRole);
                staff.setStaffStatus(staffStatus);
                staff.setManagerICNumber(managerICNumber);
                staff.setStaffEmail(staffEmail);
                model.addAttribute("staff", staff); 
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "manager/managerViewStaff";
         }

    

    @GetMapping("/managerStaffUpdate")
         public String managerStaffUpdate(@RequestParam("staffICNumber") String staffICNumber, Model model, HttpSession session) {
           String userStaffICNumber = (String) session.getAttribute("staffICNumber") ;
           System.out.println("Staff IC Number : " + userStaffICNumber);
           try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT stafficnumber, staffname, staffgender, staffphonenumber, staffrace, staffreligion, staffmaritalstatus, staffaddress, staffrole, staffstatus, managerICNumber,staffemail, staffpassword FROM public.staff where stafficnumber = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, staffICNumber);
            final var resultSet = statement.executeQuery();
         
            if (resultSet.next()) {
                String staffName = resultSet.getString("staffname");
                String staffGender = resultSet.getString("staffgender");
                String staffPhoneNumber = resultSet.getString("staffphonenumber");
                String staffRace = resultSet.getString("staffrace");
                String staffReligion = resultSet.getString("staffreligion");
                String staffMaritalStatus = resultSet.getString("staffmaritalstatus");
                String staffAddress = resultSet.getString("staffaddress");
                String staffRole = resultSet.getString("staffrole");
                String staffStatus = resultSet.getString("staffstatus");
                String managerICNumber = resultSet.getString("managerICNumber");
                String staffEmail = resultSet.getString("staffemail");
                String staffPassword = resultSet.getString("staffpassword");
         
                staff staff = new staff();
                staff.setStaffICNumber(staffICNumber);
                staff.setStaffName(staffName);
                staff.setStaffGender(staffGender);
                staff.setStaffPhoneNumber(staffPhoneNumber);
                staff.setStaffRace(staffRace);
                staff.setStaffReligion(staffReligion);  
                staff.setStaffMaritalStatus(staffMaritalStatus);
                staff.setStaffAddress(staffAddress);
                staff.setStaffRole(staffRole);
                staff.setStaffStatus(staffStatus);
                staff.setManagerICNumber(managerICNumber);
                staff.setStaffEmail(staffEmail);
                staff.setStaffPassword(staffPassword);
                model.addAttribute("staff", staff);  
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "manager/managerStaffUpdate";
         }
        
         
         @PostMapping("/managerStaffUpdate")
        public String managerStaffUpdate(@ModelAttribute("managerStaffUpdate") staff staff, HttpSession session){
	        String userStaffICNumber = (String) session.getAttribute("staffICNumber") ;
          System.out.println("pass here <<<<<<<");
          try{
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE staff SET staffname=? ,staffgender=?, staffphonenumber=?, staffrace=?, staffreligion=?, staffmaritalstatus=?, staffaddress=?, staffrole=?, staffstatus=?, managerICNumber=?, staffemail=?, staffpassword = ? WHERE stafficnumber=?";
            final var statement = connection.prepareStatement(sql);
            String staffICNumber = staff.getStaffICNumber();
            String staffName = staff.getStaffName();
            String staffGender = staff.getStaffGender();
            String staffPhoneNumber = staff.getStaffPhoneNumber();
            String staffRace = staff.getStaffRace();
            String staffReligion = staff.getStaffReligion();
            String staffMaritalStatus = staff.getStaffMaritalStatus();
            String staffAddress = staff.getStaffAddress();
            String staffRole = staff.getStaffRole();
            String staffStatus = staff.getStaffStatus();
            String managerICNumber = staff.getManagerICNumber();
	    String staffEmail = staff.getStaffEmail();
	    String staffPassword = staff.getStaffPassword();

            statement.setString(1, staffName);
            statement.setString(2, staffGender);
            statement.setString(3, staffPhoneNumber);
            statement.setString(4, staffRace);
            statement.setString(5, staffReligion);
            statement.setString(6, staffMaritalStatus);
            statement.setString(7, staffAddress);
            statement.setString(8, staffRole);
            statement.setString(9, staffStatus);
            statement.setString(10, managerICNumber);
            statement.setString(11, staffEmail);
            statement.setString(12, staffPassword);
            statement.setString(13, staffICNumber);

            statement.executeUpdate();
            
            connection.close();

          }catch(Exception e){
            e.printStackTrace();
          }
            return "redirect:/managerStaffList";
        }

        @GetMapping("/staffProfile")
         public String staffProfile(HttpSession session, Model model) {
	        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
          System.out.println("Staff IC Number : " + staffICNumber);
           try {
             Connection connection = dataSource.getConnection();
             String sql = "SELECT stafficnumber, staffname, staffgender, staffphonenumber, staffrace, staffreligion, staffmaritalstatus, staffaddress, staffrole, staffstatus, managerICNumber, staffemail FROM public.staff where stafficnumber = ?";
             final var statement = connection.prepareStatement(sql);
             statement.setString(1, staffICNumber);
             final var resultSet = statement.executeQuery();
         
             if (resultSet.next()) {
                String staffName = resultSet.getString("staffname");
                String staffGender = resultSet.getString("staffgender");
                String staffPhoneNumber = resultSet.getString("staffphonenumber");
                String staffRace = resultSet.getString("staffrace");
                String staffReligion = resultSet.getString("staffreligion");
                String staffMaritalStatus = resultSet.getString("staffmaritalstatus");
                String staffAddress = resultSet.getString("staffaddress");
                String staffRole = resultSet.getString("staffrole");
                String staffStatus = resultSet.getString("staffstatus");
                String managerICNumber = resultSet.getString("managerICNumber");
                String staffEmail = resultSet.getString("staffemail");
         
                staff staff = new staff();
                staff.setStaffICNumber(staffICNumber);
                staff.setStaffName(staffName);
                staff.setStaffGender(staffGender);
                staff.setStaffPhoneNumber(staffPhoneNumber);
                staff.setStaffRace(staffRace);
                staff.setStaffReligion(staffReligion);  
                staff.setStaffMaritalStatus(staffMaritalStatus);
                staff.setStaffAddress(staffAddress);
                staff.setStaffRole(staffRole);
                staff.setStaffStatus(staffStatus);
                staff.setManagerICNumber(managerICNumber);
                staff.setStaffEmail(staffEmail);
                model.addAttribute("staff", staff); 
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "staff/staffProfile";
         }

         @GetMapping("/staffUpdate")
         public String staffUpdate(HttpSession session, Model model) {
	        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
          System.out.println("Staff IC Number : " + staffICNumber);
           try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT stafficnumber, staffname, staffgender, staffphonenumber, staffrace, staffreligion, staffmaritalstatus, staffaddress, staffrole, staffstatus, managerICNumber, staffemail, staffpassword FROM public.staff where stafficnumber = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, staffICNumber);
            final var resultSet = statement.executeQuery();
         
            if (resultSet.next()) {
                String staffName = resultSet.getString("staffname");
                String staffGender = resultSet.getString("staffgender");
                String staffPhoneNumber = resultSet.getString("staffphonenumber");
                String staffRace = resultSet.getString("staffrace");
                String staffReligion = resultSet.getString("staffreligion");
                String staffMaritalStatus = resultSet.getString("staffmaritalstatus");
                String staffAddress = resultSet.getString("staffaddress");
                String staffRole = resultSet.getString("staffrole");
                String staffStatus = resultSet.getString("staffstatus");
                String managerICNumber = resultSet.getString("managerICNumber");
                String staffEmail = resultSet.getString("staffemail");
                String staffPassword = resultSet.getString("staffpassword");
         
                staff staff = new staff();
                staff.setStaffICNumber(staffICNumber);
                staff.setStaffName(staffName);
                staff.setStaffGender(staffGender);
                staff.setStaffPhoneNumber(staffPhoneNumber);
                staff.setStaffRace(staffRace);
                staff.setStaffReligion(staffReligion);  
                staff.setStaffMaritalStatus(staffMaritalStatus);
                staff.setStaffAddress(staffAddress);
                staff.setStaffRole(staffRole);
                staff.setStaffStatus(staffStatus);
                staff.setManagerICNumber(managerICNumber);
                staff.setStaffEmail(staffEmail);
                staff.setStaffPassword(staffPassword);
                model.addAttribute("staff", staff);  
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "staff/staffUpdate";
         }
         
         @PostMapping("/staffUpdate")
         public String staffUpdate1(HttpSession session, @ModelAttribute("staffUpdate1") staff staff, Model model) {
		 if (session.getAttribute("messege") != null) {
          session.removeAttribute("messege");
        }
	        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
          System.out.println("Staff IC Number : " + staffICNumber);
           try {

            Connection connection = dataSource.getConnection();
            String sql = "UPDATE staff SET staffname=?, staffgender=?, staffphonenumber=?, staffrace=?, staffreligion=?, staffmaritalstatus=?, staffaddress=?, staffrole=?, staffstatus=?, managerICNumber=?, staffemail=?, staffpassword=? WHERE stafficnumber=?";
            final var statement = connection.prepareStatement(sql);

            String staffName = staff.getStaffName();
            String staffGender = staff.getStaffGender();
            String staffPhoneNumber = staff.getStaffPhoneNumber();
            String staffRace = staff.getStaffRace();
            String staffReligion = staff.getStaffReligion();
            String staffMaritalStatus = staff.getStaffMaritalStatus();
            String staffAddress = staff.getStaffAddress();
            String staffRole = staff.getStaffRole();
            String staffStatus = "Employed";
            String managerICNumber = staff.getManagerICNumber();
            String staffEmail = staff.getStaffEmail();
            String staffPassword = staff.getStaffPassword();
            
            statement.setString(1, staffName);
            statement.setString(2, staffGender);
            statement.setString(3, staffPhoneNumber);
            statement.setString(4, staffRace);
            statement.setString(5, staffReligion);
            statement.setString(6, staffMaritalStatus);
            statement.setString(7, staffAddress);
            statement.setString(8, staffRole);
            statement.setString(9, staffStatus);
            statement.setString(10, managerICNumber);
            statement.setString(11, staffEmail);
            statement.setString(12, staffPassword);
            statement.setString(13, staffICNumber);

            statement.executeUpdate();
            
             System.out.println("Staff IC Number : "+staffICNumber);
            
            connection.close();
                
                } catch (Exception e) {
                    e.printStackTrace();
		   session.setAttribute("messege", "Update Fail");
                    return "redirect:/index";
                }
		 
            return "redirect:/staffProfile";
        }

        @GetMapping("/managerTerminateStaff")
        public String managerTerminateStaff(HttpSession session, @RequestParam("staffICNumber") String staffICNumber){
          String userStaffICNumber = (String) session.getAttribute("staffICNumber");

          try{
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE staff SET staffstatus=? WHERE stafficnumber=?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, "Terminated");
            statement.setString(2, staffICNumber);
            statement.executeUpdate();
            
            connection.close();

          }
          catch(SQLException e){
            System.out.println("failed to terminate this staff");
            e.printStackTrace();
          }
          return "redirect:/managerStaffList";
        }
}
