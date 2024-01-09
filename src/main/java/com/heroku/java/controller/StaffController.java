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

    //original syahir punyer, guna session
    // @GetMapping("/managerAddRoom")
    // public String managerAddRoom(HttpSession session) {
    //     // int roomNumber = (int) session.getAttribute("roomNumber");
    //     // System.out.println("roomNumber id :" + roomNumber);
    //     return "manager/managerAddRoom";
    // }


    @GetMapping("/managerStaffList")
    public String managerStaffList(Model model) {

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

    // @PostMapping("/managerAddRoom")
    // public String managerAddRoom(@ModelAttribute("managerAddRoom")room room){

    //     try {
    //         Connection connection = dataSource.getConnection();
    //         String sql = "INSERT INTO public.room(roomnum,roomtype,maxguest,roomrate,roomsize,roomstatus) VALUES(?,?,?,?,?,?)";
    //         final var statement = connection.prepareStatement(sql);

    //         String roomNum = room.getRoomNum();
    //         String roomType = room.getRoomType();
    //         String maxGuest = room.getMaxGuest();
    //         String roomRate = room.getRoomRate();
    //         String roomSize = room.getRoomSize();
    //         String roomstatus = room.getRoomStatus();
            
    //         statement.setString(1, roomNum);
    //         statement.setString(2, roomType);
    //         statement.setString(3, maxGuest );
    //         statement.setString(4, roomRate);
    //         statement.setString(5, roomSize);
    //         statement.setString(6, roomstatus);
    //         statement.executeUpdate();
            
    //          System.out.println("room number : "+roomNum);
    //         // System.out.println("type : "+protype);
    //         // System.out.println("product price : RM"+proprice);
    //         // System.out.println("proimg: "+proimgs.getBytes());
            
    //         connection.close();
                
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 return "redirect:/index";
    //             }
    //         return "redirect:/managerRoomList";
    //      }


         @GetMapping("/managerViewStaff")
         public String managerViewStaff(@RequestParam("staffICNumber") String staffICNumber, Model model) {
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
         public String managerStaffUpdate(@RequestParam("staffICNumber") String staffICNumber, Model model) {
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
         
           return "manager/managerStaffUpdate";
         }
        
         
         @PostMapping("/managerStaffUpdate")
        public String managerStaffUpdate(@ModelAttribute("managerStaffUpdate") room staff){
          System.out.println("pass here <<<<<<<");
          try{
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE staff SET staffname=? ,staffgender=?, staffphonenumber=?, staffrace=?, staffreligion=?, staffmaritalstatus=?, staffaddress=?, staffrole=?, staffstatus=?, managerICNumber=?, staffemail=? WHERE stafficnumber=?";
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
            statement.setString(12, staffICNumber);

            statement.executeUpdate();
            
            connection.close();

          }catch(Exception e){
            e.printStackTrace();
          }
            return "redirect:/managerStaffList";
        }
    

    //     @GetMapping("/staffRoomList")
    // public String staffRoomList(Model model) {

    //     List<room> rooms = new ArrayList<room>();
    //     // Retrieve the logged-in room's role from the session (syahir punya nih)
    //     //String staffsrole = (String) session.getAttribute("staffsrole");
    //     //System.out.println("staffrole staffRoomList : " + staffsrole);
    //     try (Connection connection = dataSource.getConnection()) {
    //         String sql = "SELECT roomNum, roomType, maxGuest, roomRate, roomSize, roomStatus FROM public.room order by roomNum desc"; //ni originally WHERE staffsrole=?
    //         final var statement = connection.prepareStatement(sql);
    //         //statement.setString(1, "baker"); (syahir punya nih)
    //         final var resultSet = statement.executeQuery();
    //         System.out.println("pass try staffRoomList >>>>>");

    //         while (resultSet.next()) {
    //             String roomNum = resultSet.getString("roomNum");
    //             String roomType = resultSet.getString("roomType");
    //             String maxGuest = resultSet.getString("maxGuest");
    //             String roomRate = resultSet.getString("roomRate");
    //             String roomSize = resultSet.getString("roomSize");
    //             String roomStatus = resultSet.getString("roomStatus");
    //             //System.out.println("room number" + roomNum);
                
    //             room room = new room();
    //             room.setRoomNum(roomNum);
    //             room.setRoomType(roomType);
    //             room.setMaxGuest(maxGuest);
    //             room.setRoomRate(roomRate);
    //             room.setRoomSize(roomSize);
    //             room.setRoomStatus(roomStatus);                

    //             rooms.add(room);
    //             model.addAttribute("rooms", rooms);
    //             //model.addAttribute("isAdmin", staffsrole != null && staffsrole.equals("admin")); // Add isAdmin flag to the modelF (syahir punya gak)

    //         }

    //         connection.close();

    //     return "staff/staffRoomList";
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         // Handle the exception as desired (e.g., show an error message)
    //         return "error";
    //     }
        
    // }

    // @PostMapping("/staffAddRoom")
    // public String staffAddRoom(@ModelAttribute("staffAddRoom")room room){

    //     try {
    //         Connection connection = dataSource.getConnection();
    //         String sql = "INSERT INTO public.room(roomnum,roomtype,maxguest,roomrate,roomsize,roomstatus) VALUES(?,?,?,?,?,?)";
    //         final var statement = connection.prepareStatement(sql);

    //         String roomNum = room.getRoomNum();
    //         String roomType = room.getRoomType();
    //         String maxGuest = room.getMaxGuest();
    //         String roomRate = room.getRoomRate();
    //         String roomSize = room.getRoomSize();
    //         String roomstatus = room.getRoomStatus();
            
    //         statement.setString(1, roomNum);
    //         statement.setString(2, roomType);
    //         statement.setString(3, maxGuest );
    //         statement.setString(4, roomRate);
    //         statement.setString(5, roomSize);
    //         statement.setString(6, roomstatus);
    //         statement.executeUpdate();
            
    //         connection.close();
                
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 return "redirect:/index";
    //             }
    //         return "redirect:/staffRoomList";
    //      }


    //       @GetMapping("/staffViewRoom")
    //      public String staffViewRoom(@RequestParam("roomNum") String roomNum, Model model) {
    //        System.out.println("Room Number : " + roomNum);
    //        try {
    //          Connection connection = dataSource.getConnection();
    //          String sql = "SELECT roomnum, roomtype, maxguest, roomrate, roomsize, roomstatus FROM public.room WHERE roomnum = ?";
    //          final var statement = connection.prepareStatement(sql);
    //          statement.setString(1, roomNum);
    //          final var resultSet = statement.executeQuery();
         
    //          if (resultSet.next()) {
    //             String roomType = resultSet.getString("roomType");
    //             String maxGuest = resultSet.getString("maxGuest");
    //             String roomRate = resultSet.getString("roomRate");
    //             String roomSize = resultSet.getString("roomSize");
    //             String roomStatus = resultSet.getString("roomStatus");
         
    //             room room = new room();
    //             room.setRoomNum(roomNum);
    //             room.setRoomType(roomType);
    //             room.setMaxGuest(maxGuest);
    //             room.setRoomRate(roomRate);
    //             room.setRoomSize(roomSize);
    //             room.setRoomStatus(roomStatus);
    //             model.addAttribute("room", room);  
   
    //            connection.close();
    //          }
    //        } catch (Exception e) {
    //          e.printStackTrace();
    //        }
         
    //        return "staff/staffViewRoom";
    //      }


    //      @GetMapping("/staffUpdateRoom")
    //      public String staffUpdateRoom(@RequestParam("roomNum") String roomNum, Model model) {
    //        System.out.println("Room Number : " + roomNum);
    //        try {
    //          Connection connection = dataSource.getConnection();
    //          String sql = "SELECT roomnum, roomtype, maxguest, roomrate, roomsize, roomstatus FROM public.room WHERE roomnum = ?";
    //          final var statement = connection.prepareStatement(sql);
    //          statement.setString(1, roomNum);
    //          final var resultSet = statement.executeQuery();
         
    //          if (resultSet.next()) {
    //             String roomType = resultSet.getString("roomType");
    //             String maxGuest = resultSet.getString("maxGuest");
    //             String roomRate = resultSet.getString("roomRate");
    //             String roomSize = resultSet.getString("roomSize");
    //             String roomStatus = resultSet.getString("roomStatus");
         
    //             room room = new room();
    //             room.setRoomNum(roomNum);
    //             room.setRoomType(roomType);
    //             room.setMaxGuest(maxGuest);
    //             room.setRoomRate(roomRate);
    //             room.setRoomSize(roomSize);
    //             room.setRoomStatus(roomStatus);
    //             model.addAttribute("room", room);  
   
    //            connection.close();
    //          }
    //        } catch (Exception e) {
    //          e.printStackTrace();
    //        }
         
    //        return "staff/staffUpdateRoom";
    //      }
        
         
    //      @PostMapping("/staffUpdateRoom")
    //     public String staffUpdateRoom(@ModelAttribute("staffUpdateRoom") room room){
    //       System.out.println("pass here <<<<<<<");
    //       try{
    //         Connection connection = dataSource.getConnection();
    //         String sql = "UPDATE room SET roomType=? ,maxGuest=?, roomRate=?, roomSize=?, roomStatus=? WHERE roomNum=?";
    //         final var statement = connection.prepareStatement(sql);
    //         String roomNum = room.getRoomNum();
    //         String roomType = room.getRoomType();
    //         String maxGuest = room.getMaxGuest();
    //         String roomRate = room.getRoomRate();
    //         String roomSize = room.getRoomSize();
    //         String roomstatus = room.getRoomStatus();

    //         //debug
    //         // System.out.println("pro price update : "+proprice);
    //         // System.out.println("pro id update : "+proid);

    //         statement.setString(1, roomType);
    //         statement.setString(2, maxGuest );
    //         statement.setString(3, roomRate);
    //         statement.setString(4, roomSize);
    //         statement.setString(5, roomstatus);
    //         statement.setString(6, roomNum);

    //         statement.executeUpdate();
            
    //         connection.close();

    //       }catch(Exception e){
    //         e.printStackTrace();
    //       }
    //         return "redirect:/staffRoomList";
    //     }
    // }


    // /* delete controller
    // @GetMapping("/deletestaff")
    // public String deleteProfileCust(HttpSession session, Model model) {
    //     String fullname = (String) session.getAttribute("roomType");
    //     int userid = (int) session.getAttribute("roomNum");

    //     if (fullname != null) {
    //         try (Connection connection = dataSource.getConnection()) {

    //             // Delete user record
    //             final var deleteStaffStatement = connection.prepareStatement("DELETE FROM staffs WHERE roomNum=?");
    //             deleteStaffStatement.setInt(1, userid);
    //             int userRowsAffected = deleteStaffStatement.executeUpdate();

    //             if (userRowsAffected > 0) {
    //                 // Deletion successful
    //                 // You can redirect to a success page or perform any other desired actions
                    
    //                 session.invalidate();
    //                 connection.close();
    //                 return "redirect:/";
    //             } else {
    //                 // Deletion failed
    //                 connection.close();
    //                  System.out.println("Delete Failed");
    //                 return "admin/deletestaff";
                   
    //             }
    //         } catch (SQLException e) {
    //             // Handle any potential exceptions (e.g., log the error, display an error page)
    //             e.printStackTrace();

    //             // Deletion failed
    //             // You can redirect to an error page or perform any other desired actions
    //             System.out.println("Error");
    //         }
    //     }
    //     // Username is null or deletion failed, handle accordingly (e.g., redirect to an
    //     // error page)
    //     return "staff/stafforder";
    // }*/
    }
