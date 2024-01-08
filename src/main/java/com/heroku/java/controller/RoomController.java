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

import com.heroku.java.model.room;

import jakarta.servlet.http.HttpSession;

import java.sql.*;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

import java.util.List;

@Controller
public class RoomController {
    private final DataSource dataSource;

    @Autowired
    public RoomController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //original syahir punyer, guna session
    // @GetMapping("/managerAddRoom")
    // public String managerAddRoom(HttpSession session) {
    //     // int roomNumber = (int) session.getAttribute("roomNumber");
    //     // System.out.println("roomNumber id :" + roomNumber);
    //     return "manager/managerAddRoom";
    // }


    @GetMapping("/managerRoomList")
    public String managerRoomList(Model model) {

        List<room> rooms = new ArrayList<room>();
        // Retrieve the logged-in room's role from the session (syahir punya nih)
        //String staffsrole = (String) session.getAttribute("staffsrole");
        //System.out.println("staffrole managerRoomList : " + staffsrole);
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT roomNum, roomType, maxGuest, roomRate, roomSize, roomStatus FROM public.room order by roomNum"; //ni originally WHERE staffsrole=?
            final var statement = connection.prepareStatement(sql);
            //statement.setString(1, "baker"); (syahir punya nih)
            final var resultSet = statement.executeQuery();
            System.out.println("pass try managerRoomList >>>>>");

            while (resultSet.next()) {
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
                //System.out.println("room number" + roomNum);
                
                room room = new room();
                room.setRoomNum(roomNum);
                room.setRoomType(roomType);
                room.setMaxGuest(maxGuest);
                room.setRoomRate(roomRate);
                room.setRoomSize(roomSize);
                room.setRoomStatus(roomStatus);                

                rooms.add(room);
                model.addAttribute("rooms", rooms);
                //model.addAttribute("isAdmin", staffsrole != null && staffsrole.equals("admin")); // Add isAdmin flag to the modelF (syahir punya gak)

            }

            connection.close();

        return "manager/managerRoomList";
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            return "error";
        }
        
    }

    @PostMapping("/managerAddRoom")
    public String managerAddRoom(@ModelAttribute("managerAddRoom")room room){

        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO public.room(roomnum,roomtype,maxguest,roomrate,roomsize,roomstatus) VALUES(?,?,?,?,?,?)";
            final var statement = connection.prepareStatement(sql);

            String roomNum = room.getRoomNum();
            String roomType = room.getRoomType();
            String maxGuest = room.getMaxGuest();
            String roomRate = room.getRoomRate();
            String roomSize = room.getRoomSize();
            String roomstatus = room.getRoomStatus();
            
            statement.setString(1, roomNum);
            statement.setString(2, roomType);
            statement.setString(3, maxGuest );
            statement.setString(4, roomRate);
            statement.setString(5, roomSize);
            statement.setString(6, roomstatus);
            statement.executeUpdate();
            
             System.out.println("room number : "+roomNum);
            // System.out.println("type : "+protype);
            // System.out.println("product price : RM"+proprice);
            // System.out.println("proimg: "+proimgs.getBytes());
            
            connection.close();
                
                } catch (Exception e) {
                    e.printStackTrace();
                    return "redirect:/index";
                }
            return "redirect:/managerRoomList";
         }


         @GetMapping("/managerViewRoom")
         public String managerViewRoom(@RequestParam("roomNum") String roomNum, Model model) {
           System.out.println("Room Number : " + roomNum);
           try {
             Connection connection = dataSource.getConnection();
             String sql = "SELECT roomnum, roomtype, maxguest, roomrate, roomsize, roomstatus FROM public.room WHERE roomnum = ?";
             final var statement = connection.prepareStatement(sql);
             statement.setString(1, roomNum);
             final var resultSet = statement.executeQuery();
         
             if (resultSet.next()) {
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
         
                room room = new room();
                room.setRoomNum(roomNum);
                room.setRoomType(roomType);
                room.setMaxGuest(maxGuest);
                room.setRoomRate(roomRate);
                room.setRoomSize(roomSize);
                room.setRoomStatus(roomStatus);
                model.addAttribute("room", room);  
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "manager/managerViewRoom";
         }

    

    @GetMapping("/managerUpdateRoom")
         public String managerUpdateRoom(@RequestParam("roomNum") String roomNum, Model model) {
           System.out.println("Room Number : " + roomNum);
           try {
             Connection connection = dataSource.getConnection();
             String sql = "SELECT roomnum, roomtype, maxguest, roomrate, roomsize, roomstatus FROM public.room WHERE roomnum = ?";
             final var statement = connection.prepareStatement(sql);
             statement.setString(1, roomNum);
             final var resultSet = statement.executeQuery();
         
             if (resultSet.next()) {
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
         
                room room = new room();
                room.setRoomNum(roomNum);
                room.setRoomType(roomType);
                room.setMaxGuest(maxGuest);
                room.setRoomRate(roomRate);
                room.setRoomSize(roomSize);
                room.setRoomStatus(roomStatus);
                model.addAttribute("room", room);  
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "manager/managerUpdateRoom";
         }
        
         
         @PostMapping("/managerUpdateRoom")
        public String managerUpdateRoom(@ModelAttribute("managerUpdateRoom") room room){
          System.out.println("pass here <<<<<<<");
          try{
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE room SET roomType=? ,maxGuest=?, roomRate=?, roomSize=?, roomStatus=? WHERE roomNum=?";
            final var statement = connection.prepareStatement(sql);
            String roomNum = room.getRoomNum();
            String roomType = room.getRoomType();
            String maxGuest = room.getMaxGuest();
            String roomRate = room.getRoomRate();
            String roomSize = room.getRoomSize();
            String roomstatus = room.getRoomStatus();

            //debug
            // System.out.println("pro price update : "+proprice);
            // System.out.println("pro id update : "+proid);

            statement.setString(1, roomType);
            statement.setString(2, maxGuest );
            statement.setString(3, roomRate);
            statement.setString(4, roomSize);
            statement.setString(5, roomstatus);
            statement.setString(6, roomNum);

            statement.executeUpdate();
            
            connection.close();

          }catch(Exception e){
            e.printStackTrace();
          }
            return "redirect:/managerRoomList";
        }
    

        @GetMapping("/staffRoomList")
    public String staffRoomList(Model model) {

        List<room> rooms = new ArrayList<room>();
        // Retrieve the logged-in room's role from the session (syahir punya nih)
        //String staffsrole = (String) session.getAttribute("staffsrole");
        //System.out.println("staffrole staffRoomList : " + staffsrole);
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT roomNum, roomType, maxGuest, roomRate, roomSize, roomStatus FROM public.room order by roomNum desc"; //ni originally WHERE staffsrole=?
            final var statement = connection.prepareStatement(sql);
            //statement.setString(1, "baker"); (syahir punya nih)
            final var resultSet = statement.executeQuery();
            System.out.println("pass try staffRoomList >>>>>");

            while (resultSet.next()) {
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
                //System.out.println("room number" + roomNum);
                
                room room = new room();
                room.setRoomNum(roomNum);
                room.setRoomType(roomType);
                room.setMaxGuest(maxGuest);
                room.setRoomRate(roomRate);
                room.setRoomSize(roomSize);
                room.setRoomStatus(roomStatus);                

                rooms.add(room);
                model.addAttribute("rooms", rooms);
                //model.addAttribute("isAdmin", staffsrole != null && staffsrole.equals("admin")); // Add isAdmin flag to the modelF (syahir punya gak)

            }

            connection.close();

        return "staff/staffRoomList";
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            return "error";
        }
        
    }

    @PostMapping("/staffAddRoom")
    public String staffAddRoom(@ModelAttribute("staffAddRoom")room room){

        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO public.room(roomnum,roomtype,maxguest,roomrate,roomsize,roomstatus) VALUES(?,?,?,?,?,?)";
            final var statement = connection.prepareStatement(sql);

            String roomNum = room.getRoomNum();
            String roomType = room.getRoomType();
            String maxGuest = room.getMaxGuest();
            String roomRate = room.getRoomRate();
            String roomSize = room.getRoomSize();
            String roomstatus = room.getRoomStatus();
            
            statement.setString(1, roomNum);
            statement.setString(2, roomType);
            statement.setString(3, maxGuest );
            statement.setString(4, roomRate);
            statement.setString(5, roomSize);
            statement.setString(6, roomstatus);
            statement.executeUpdate();
            
            connection.close();
                
                } catch (Exception e) {
                    e.printStackTrace();
                    return "redirect:/index";
                }
            return "redirect:/staffRoomList";
         }


          @GetMapping("/staffViewRoom")
         public String staffViewRoom(@RequestParam("roomNum") String roomNum, Model model) {
           System.out.println("Room Number : " + roomNum);
           try {
             Connection connection = dataSource.getConnection();
             String sql = "SELECT roomnum, roomtype, maxguest, roomrate, roomsize, roomstatus FROM public.room WHERE roomnum = ?";
             final var statement = connection.prepareStatement(sql);
             statement.setString(1, roomNum);
             final var resultSet = statement.executeQuery();
         
             if (resultSet.next()) {
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
         
                room room = new room();
                room.setRoomNum(roomNum);
                room.setRoomType(roomType);
                room.setMaxGuest(maxGuest);
                room.setRoomRate(roomRate);
                room.setRoomSize(roomSize);
                room.setRoomStatus(roomStatus);
                model.addAttribute("room", room);  
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "staff/staffViewRoom";
         }


         @GetMapping("/staffUpdateRoom")
         public String staffUpdateRoom(@RequestParam("roomNum") String roomNum, Model model) {
           System.out.println("Room Number : " + roomNum);
           try {
             Connection connection = dataSource.getConnection();
             String sql = "SELECT roomnum, roomtype, maxguest, roomrate, roomsize, roomstatus FROM public.room WHERE roomnum = ?";
             final var statement = connection.prepareStatement(sql);
             statement.setString(1, roomNum);
             final var resultSet = statement.executeQuery();
         
             if (resultSet.next()) {
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
         
                room room = new room();
                room.setRoomNum(roomNum);
                room.setRoomType(roomType);
                room.setMaxGuest(maxGuest);
                room.setRoomRate(roomRate);
                room.setRoomSize(roomSize);
                room.setRoomStatus(roomStatus);
                model.addAttribute("room", room);  
   
               connection.close();
             }
           } catch (Exception e) {
             e.printStackTrace();
           }
         
           return "staff/staffUpdateRoom";
         }
        
         
         @PostMapping("/staffUpdateRoom")
        public String staffUpdateRoom(@ModelAttribute("staffUpdateRoom") room room){
          System.out.println("pass here <<<<<<<");
          try{
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE room SET roomType=? ,maxGuest=?, roomRate=?, roomSize=?, roomStatus=? WHERE roomNum=?";
            final var statement = connection.prepareStatement(sql);
            String roomNum = room.getRoomNum();
            String roomType = room.getRoomType();
            String maxGuest = room.getMaxGuest();
            String roomRate = room.getRoomRate();
            String roomSize = room.getRoomSize();
            String roomstatus = room.getRoomStatus();

            //debug
            // System.out.println("pro price update : "+proprice);
            // System.out.println("pro id update : "+proid);

            statement.setString(1, roomType);
            statement.setString(2, maxGuest );
            statement.setString(3, roomRate);
            statement.setString(4, roomSize);
            statement.setString(5, roomstatus);
            statement.setString(6, roomNum);

            statement.executeUpdate();
            
            connection.close();

          }catch(Exception e){
            e.printStackTrace();
          }
            return "redirect:/staffRoomList";
        }
    }


    /* delete controller
    @GetMapping("/deletestaff")
    public String deleteProfileCust(HttpSession session, Model model) {
        String fullname = (String) session.getAttribute("roomType");
        int userid = (int) session.getAttribute("roomNum");

        if (fullname != null) {
            try (Connection connection = dataSource.getConnection()) {

                // Delete user record
                final var deleteStaffStatement = connection.prepareStatement("DELETE FROM staffs WHERE roomNum=?");
                deleteStaffStatement.setInt(1, userid);
                int userRowsAffected = deleteStaffStatement.executeUpdate();

                if (userRowsAffected > 0) {
                    // Deletion successful
                    // You can redirect to a success page or perform any other desired actions
                    
                    session.invalidate();
                    connection.close();
                    return "redirect:/";
                } else {
                    // Deletion failed
                    connection.close();
                     System.out.println("Delete Failed");
                    return "admin/deletestaff";
                   
                }
            } catch (SQLException e) {
                // Handle any potential exceptions (e.g., log the error, display an error page)
                e.printStackTrace();

                // Deletion failed
                // You can redirect to an error page or perform any other desired actions
                System.out.println("Error");
            }
        }
        // Username is null or deletion failed, handle accordingly (e.g., redirect to an
        // error page)
        return "staff/stafforder";
    }*/
