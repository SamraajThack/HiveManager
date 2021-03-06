package com.example.hivemanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;

import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DatabaseHelper {
    static Connection con;

    /**
     * Connects to the database. Should be initialized exactly once.
     * If called when connection is already initialized, will return the existing connection.
     *
     * @return con a connection to the database.
     */
    public static Connection establishConnection() {

        // Returns con if it already exists.
        if (con != null) return con;

        // Thread policy.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Establishes a connection with the SQL server.
        con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://uwhivemanager506.cmnpa3ypkmwq.us-east-2.rds.amazonaws.com:3306/hive_manager", "admin", "Hivemanager123");

        }
        // Returns an error message if connection fails.
        catch (Exception e) {
            Log.e("SQL Connection Error : ", e.getMessage());

        }

        // Returns the connection.
        return con;

    }

    //returns an ArrayList of all the user fields in the format[username,firstname,email,lastname,phone_number,ppr,password,address,zipcode]
    public static ArrayList getUserData(String userName) throws SQLException {
        Statement stmt;
        Connection con = establishConnection();

        String sql = "SELECT * FROM Beekeeper WHERE Username = '" + userName.toString() + "'";
        stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        ArrayList userlist = new ArrayList();
        while (rs.next()) {

            String username = rs.getString("Username");
            userlist.add(username);
            String firstname = rs.getString("First_Name");
            userlist.add(firstname);
            String email = rs.getString("Email");
            userlist.add(email);
            String lastname = rs.getString("Last_name");
            userlist.add(lastname);
            String phone_number = rs.getString("Phone_Number");
            userlist.add(phone_number);
            String ppr = rs.getString("ProfilePicReference");
            userlist.add(ppr);
            String password = rs.getString("password");
            userlist.add(password);
            String address = rs.getString("Address");
            userlist.add(address);
            String zipcode = rs.getString("Zipcode");
            userlist.add(zipcode);


        }


        return userlist;

    }

    public static Profile initUser(String userName) throws SQLException {
        Profile user = new Profile();
        Statement stmt;
        //Blob blob;
        Connection con = establishConnection();

        String sql = "SELECT * FROM Beekeeper WHERE Username = '" + userName.toString() + "'";
        stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {

            user.setUsername(rs.getString("Username"));
            user.setFirstname(rs.getString("First_Name"));
            user.setEmail(rs.getString("Email"));
            user.setLastname(rs.getString("Last_name"));
            user.setPhone(rs.getString("Phone_Number"));
           // user.setProfilePhoto(rs.getString("ProfilePicReference")); //TODO ask about this
            user.setAddress(rs.getString("Address"));
            user.setZipcode(rs.getString("Zipcode"));
            InputStream input =rs.getBinaryStream("profile_pic");

            //get blob, then turn it into byte array, then turn into bitmap
            //set bitmap to
            Blob blob = rs.getBlob("profile_pic");
            byte b[]=blob.getBytes(1,(int)blob.length());
            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0,b.length);
            user.setProfilePhoto(bmp);


        }
        return user;
    }

    /**
     * Returns a list of Apiaries associated with the inputed user.
     *
     * The Apiaries will be initialised with Address and Zipcode, Hives and Equipment will be left
     * empty.
     *
     * @param user the user to retrieve Apiaries for.
     * @return a list of Apiaries associated with the inputed user.
     * @throws SQLException if a SQL query fails.
     */
    public static ArrayList<Apiary> getApiaries(String user) throws SQLException {
        String sql;
        Statement stmt;
        ResultSet results;
        ArrayList<Apiary> apiaries = new ArrayList<>();

        // Issues a SQL query to find all Apiaries associated with user.
        sql = "SELECT * " +
                "FROM Apiary " +
                "WHERE Username = \"" + user + "\""
        ;
        stmt = con.createStatement();
        results = stmt.executeQuery(sql);

        // Fills the ArrayList of Apiaries.
        while (results.next()) {
            apiaries.add(new Apiary(results.getString("Address"),
                    results.getString("Zipcode")));

        }

        // Returns the list of apiaries associated with user.
        return apiaries;

    }

    /**
     * Returns a list of Hives associated with the inputed address.
     *
     * The Hives will be initialised with all data except equipment and inspections.
     *
     * @param address the address to retrieve Hives for.
     * @return the Hives associated with the inputed address.
     * @throws SQLException if a SQL query fails.
     */
    public static ArrayList<Hive> getHivesAddr(String address) throws SQLException {
        String sql;
        Statement stmt;
        ResultSet results;
        ArrayList<Hive> hives = new ArrayList<Hive>();

        // Issues a SQL query to find all Hives associated with address.
        sql = "SELECT * " +
                "FROM Hive " +
                "WHERE Address = \"" + address + "\""
        ;
        stmt = con.createStatement();
        results = stmt.executeQuery(sql);

        // Fills the ArrayList of Hives.
        while (results.next()) {
            hives.add(new Hive(
                    Integer.parseInt(results.getString("HiveId")),
                    Integer.parseInt(results.getString("Health")),
                    null,
                    Integer.parseInt(results.getString("Honey_stores")),
                    Integer.parseInt(results.getString("Queen_Production")),
                    null,
                    Integer.parseInt(results.getString("Losses")),
                    Integer.parseInt(results.getString("Gains"))));

        }

        // Returns the ArrayList of Hives associated with address.
        return hives;

    }

    /**
     * Returns a list of pieces of Equipment associated with the inputed Hive.
     *
     * @param hiveID the Hive to retrieve Equipment for.
     * @return the Equipment associated with the inputed Hive.
     * @throws SQLException if a SQL query fails.
     */
    public static ArrayList<Equipment> getHiveEquipment(String hiveID) throws SQLException {
        String sql;
        Statement stmt;
        ResultSet results;
        ArrayList<Equipment> equipment = new ArrayList<Equipment>();

        // Issues a SQL query to find all Equipment associated with hiveID.
        sql = "SELECT * " +
                "FROM Equipment " +
                "WHERE HiveId = \"" + hiveID + "\""
        ;
        stmt = con.createStatement();
        results = stmt.executeQuery(sql);

        // Fills the ArrayList of Equipment.
        while (results.next()) {
            equipment.add(new Equipment(results.getInt("EquipmentId"),
                    results.getString("Equipment_name")));

        }

        // Returns the ArrayList of Equipment associated with hiveID.
        return equipment;

    }

    /**
     * Returns a list of inspections associated with the inputed hiveID.
     *
     * @param hiveID the Hive to retrieve inspections for.
     * @return the inspections associated with the inputed Hive.
     * @throws SQLException if a SQL query fails.
     */
    public static ArrayList<Inspection> getHiveInspections(String hiveID) throws SQLException {
        String sql;
        Statement stmt;
        ResultSet results;
        ArrayList<Inspection> inspections = new ArrayList<Inspection>();

        // Issues a SQL query to find all Inspections associated with hiveID.
        sql = "SELECT * " +
                "FROM Inspections " +
                "WHERE HiveId = \"" + hiveID + "\""
        ;
        stmt = con.createStatement();
        results = stmt.executeQuery(sql);

        // Fills the ArrayList of Inspections.
        while (results.next()) {
            inspections.add(new Inspection(results.getInt("InspectionId"),
                    results.getInt("HiveId"), results.getDate("Inspection_date"),
                    results.getString("Inspection_notes")));

        }

        // Returns the ArrayList of Inspections associated with hiveID.
        return inspections;

    }

    //return a list of hive IDs
    // with the same address i.e. same Apiary
    public static ArrayList getHives(String addr) throws SQLException {
        Statement stmt;
        Connection con = establishConnection();

        String sql = "SELECT * FROM Hive WHERE Address = '" + addr.toString() + "'";
        stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        ArrayList hivelist = new ArrayList();


        while(rs.next()){

            String hiveid = rs.getString(("HiveId"));
            hivelist.add(hiveid);

        }

        return hivelist;

    }

    public static ArrayList getHiveInfo(int hiveID) throws SQLException {
        Statement stmt;
        Connection con = establishConnection();

        String sql = "SELECT * FROM Hive WHERE HiveId = '" + hiveID + "'";
        stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        ArrayList hiveInfo = new ArrayList();
        while (rs.next()) {

            String hiveId = rs.getString("HiveId");
            hiveInfo.add(hiveId);
            String health = rs.getString("Health");
            hiveInfo.add(health);
            String honey_stores = rs.getString("Honey_stores");
            hiveInfo.add(honey_stores);
            String queenproduction1 = rs.getString("Queen_Production");
            hiveInfo.add(queenproduction1);
            String gains = rs.getString("Gains");
            hiveInfo.add(gains);
            String losses = rs.getString("Losses");
            hiveInfo.add(losses);
            String address = rs.getString("Address");
            hiveInfo.add(address);
            String zipcode = rs.getString("Zipcode");
            hiveInfo.add(zipcode);


        }


        return hiveInfo;

    }

    //adds an apiary into the database, CANNOT ADD INTO APIARY IF THERE IS NO USER WITH THE SAME USERNAME
    public static void addApiary(String username, String address, String zipcode) throws SQLException {
        Statement stmt;
        Connection con = establishConnection();

        String sql = "INSERT INTO Apiary VALUES ('" + address + "','" + username + "','" + zipcode + "')";

        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static void deleteApiary(String Address) throws SQLException {

        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "DELETE FROM Apiary WHERE Address = '"+ Address +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    //adds an hive in to the database, increments HiveId by 1, returns the hiveId of the added hive as an integer
    public static int addHive(String Health, String Honey_stores, String Queen_production, String Gains, String Losses, String Address, String zipcode) throws SQLException {

        Connection con1;
        Connection con2;
        Statement stmt1;
        Statement stmt2;



        con1 = establishConnection();
        String sql1 = "SELECT MAX(HiveId) FROM Hive";
        stmt1 = con1.createStatement();
        ResultSet rs = stmt1.executeQuery(sql1);

        int currMax = 0;

        while(rs.next()) {
            currMax = rs.getInt(1);
        }

        int nextIndex;
        nextIndex  = currMax + 1;

        con2 = establishConnection();
        String sql2 = "INSERT INTO Hive VALUES ('"+nextIndex+"','" + Health + "','" + Honey_stores + "','"+ Queen_production +"','"+ Gains +"','"+Losses+"','"+Address+"','"+zipcode+"')";
        stmt2 = con1.createStatement();
        stmt2.executeUpdate(sql2);


        return nextIndex;

    }

    public static void deleteHive(Integer hiveID) throws SQLException {
        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "DELETE FROM Hive WHERE HiveId = '"+ hiveID +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);
    }

    /**
     * Places a piece of equipment into the database.
     *
     * @param hiveID the hive that this Equipment is attached to.
     * @param address the address where this Equipment is stored.
     * @param name the name of this piece of Equipment.
     * @return the ID of this piece of Equipment in the database.
     * @throws SQLException if a SQL query fails.
     */
    public static int addEquipment(String hiveID, String address, String name) throws SQLException {
        int maxID;
        String sql;
        Statement stmt;
        ResultSet results;

        // Finds the appropriate ID for the new piece of equipment.
        sql = "SELECT MAX(EquipmentId) FROM Equipment";
        stmt = con.createStatement();
        results = stmt.executeQuery(sql);
        results.next();
        maxID = results.getInt(1) + 1;

        // Issues a SQL query to add the piece of Equipment to the database.
        sql = "INSERT INTO Equipment " +
                "Values (" + maxID + ", " + hiveID + ", \"" + address + "\", \"" + name + "\")";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

        // Returns the ID for this piece of Equipment.
        return maxID;

    }

    /**
     * Removes a piece of Equipment from the database.
     *
     * @param ID the ID of the piece of Equipment to be removed.
     * @throws SQLException if a SQL query fails.
     */
    public static void deleteEquipment(int ID) throws SQLException {
        String sql;
        Statement stmt;

        // Issues a SQL query to remove the piece of Equipment from the database.
        sql = "DELETE FROM Equipment WHERE EquipmentId = " + ID;
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static void editApiary(String username, String oldaddress, String newaddress, String zipcode) throws SQLException {
        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "UPDATE Apiary SET Address = '"+ newaddress +"', Zipcode = '"+ zipcode +"' WHERE Address = '"+ oldaddress +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);
    }

    public static void editHive(int hiveid, String Health, String Honey_stores, String Queen_production, String Gains, String Losses, String Address, String zipcode) throws SQLException {

        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "UPDATE Hive SET Health = '"+ Health +"', Honey_stores = '"+ Honey_stores +"', Queen_production = '"+ Queen_production +"', Gains ='"+ Gains +"', Losses = '"+ Losses +"', Address = '"+ Address +"', Zipcode = '"+ zipcode+ "'  WHERE HiveId = '"+ hiveid +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static void createUser(String username, String first_name, String email, String lastname, String phonenumber,String password, String address, String zipcode, String ppref) throws SQLException {
        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "INSERT INTO Beekeeper VALUES ('" + username + "','" + first_name + "','" + email + "','" + lastname + "','" + phonenumber + "','" + password + "','" + address + "','" + zipcode + "', '"+ppref+"')";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static void editUser(String username, String first_name, String lastname, String address, String zipcode, String phonenumber, String email) throws SQLException {

        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "UPDATE Beekeeper SET First_name = '"+ first_name +"', Email = '"+ email +"', Last_name = '"+ lastname +"', Phone_number ='"+ phonenumber +"', Address = '"+ address +"', Zipcode = '"+ zipcode+ "'  WHERE Username = '"+ username +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static int addInspection(int hiveID, String inspection_notes, String inspection_date) throws ParseException, SQLException {


        Connection con1;
        Connection con2;
        Statement stmt1;
        Statement stmt2;


        con1 = establishConnection();
        String sql1 = "SELECT MAX(InspectionId) FROM Inspections";
        stmt1 = con1.createStatement();
        ResultSet rs = stmt1.executeQuery(sql1);

        int currMax = 0;

        while (rs.next()) {
            currMax = rs.getInt(1);
        }

        int newIndex;
        newIndex = currMax + 1;

        SimpleDateFormat sdf = new SimpleDateFormat(("dd-MM-yyyy"));
        java.util.Date date = sdf.parse(inspection_date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());




        String sql = "INSERT INTO Inspections VALUES ('"+ newIndex +"','" + hiveID + "','" + inspection_notes + "','" + sqlDate + "')";
        con2 = establishConnection();
        stmt2 = con2.createStatement();
        stmt2.executeUpdate(sql);


        return newIndex;

    }



    public static void editInspection(int inspectionID, int Hiveid, String inspection_notes, String inspection_date) throws SQLException, ParseException {

        Connection con;
        Statement stmt;

        SimpleDateFormat sdf = new SimpleDateFormat(("dd-MM-yyyy"));
        java.util.Date date = sdf.parse(inspection_date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

        con = establishConnection();
        String sql = "UPDATE Inspections SET HiveId = '"+ Hiveid +"', Inspection_notes = '"+ inspection_notes +"', Inspection_date = '"+ sqlDate +"' WHERE InspectionId = '"+ inspectionID +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static void deleteInspection(int inspectionID) throws SQLException {
        Connection con;
        Statement stmt;

        con = establishConnection();
        String sql = "DELETE FROM Inspections WHERE InspectionId = '"+ inspectionID +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }

    public static void editEquipment(int equipmentId, int HiveID, String Address, String Equipment_name) throws SQLException {
        Connection con;
        Statement stmt;


        con = establishConnection();
        String sql = "UPDATE Equipment SET HiveId = '"+ HiveID +"', Address = '"+ Address +"', Equipment_name = '"+ Equipment_name +"' WHERE equipmentId = '"+ equipmentId +"'";
        stmt = con.createStatement();
        stmt.executeUpdate(sql);

    }









}