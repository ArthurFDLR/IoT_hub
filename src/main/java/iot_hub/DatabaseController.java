package iot_hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseController {
    
    private Connection connection = null;
    private PreparedStatement ps_insertGroup = null;
    private PreparedStatement ps_deleteMembers = null;
    private PreparedStatement ps_deleteGroup = null;
    private PreparedStatement ps_insertMember = null;
    private PreparedStatement ps_getMembers = null;
    private PreparedStatement ps_getGroups = null;
    private PreparedStatement ps_insertPlug = null;
    private PreparedStatement ps_insertPower = null;

    public DatabaseController(String databasePath) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+databasePath);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS groups (name STRING NOT NULL PRIMARY KEY)");
            ps_insertGroup = connection.prepareStatement("INSERT INTO groups VALUES(?)");
            ps_deleteGroup = connection.prepareStatement("DELETE FROM groups WHERE name=?");
            ps_getGroups = connection.prepareStatement("SELECT name FROM groups");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS plugs (name STRING NOT NULL PRIMARY KEY)");
            ps_insertPlug = connection.prepareStatement("INSERT INTO plugs VALUES(?)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS groups_plugs "
                                   +"(groupName STRING, plugName STRING,"
                                   +"FOREIGN KEY(groupName) REFERENCES groups(name),"
                                   +"FOREIGN KEY(plugName) REFERENCES plugs(name),"
                                   +"PRIMARY key(groupName, plugName))");
            ps_insertMember = connection.prepareStatement("INSERT INTO groups_plugs VALUES(?,?)");
            ps_deleteMembers = connection.prepareStatement("DELETE FROM groups_plugs WHERE groupName=?");
            ps_getMembers = connection.prepareStatement("SELECT plugName FROM groups_plugs WHERE groupName=?");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS plugs_power"
                                   +"(name STRING NOT NULL, date STRING NOT NULL, power REAL,"
                                   +"FOREIGN KEY(name) REFERENCES plugs(name),"
                                   +"PRIMARY KEY(name, date))");
            ps_insertPower = connection.prepareStatement("INSERT INTO plugs_power VALUES(?,?,?)");
        } catch(SQLException e) { logger.error(e.getMessage()); }
    }

    public void createGroup(String groupName, ArrayList<String> plugsName) {
        // Error 19 is associated to the UNIQUE constraint
        try { // Create group if doesnt exist
            ps_insertGroup.setString(1, groupName);
            ps_insertGroup.executeUpdate();
        } catch(SQLException e) {
            if(e.getErrorCode() != 19){logger.error(e.getMessage());}
        }
        try { // Delete members
            ps_deleteMembers.setString(1, groupName);
            ps_deleteMembers.executeUpdate();
        } catch(SQLException e) { logger.error(e.getMessage()); }
        for (String plug : plugsName) { // Add members
            try {
                ps_insertPlug.setString(1, plug);
                ps_insertPlug.executeUpdate();
            } catch(SQLException e) {
                if(e.getErrorCode() != 19){logger.error(e.getMessage());}
            }
            try {
                ps_insertMember.setString(1, groupName);
                ps_insertMember.setString(2, plug);
                ps_insertMember.executeUpdate();
            } catch(SQLException e) {
                if(e.getErrorCode() != 19){logger.error(e.getMessage());}
            }
        }
    }

    public void removeGroup(String groupName) {
        try {
            ps_deleteMembers.setString(1, groupName);
            ps_deleteMembers.executeUpdate();
            ps_deleteGroup.setString(1, groupName);
            ps_deleteGroup.executeUpdate();
        } catch(SQLException e) { logger.error(e.getMessage()); }
    }

    public void insertPower(String plugName, Float power) {
        // Timestamp should be passed as parameter for better time accuracy (added to mqtt message)
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strDate = sdfDate.format(new Date());
        try {
            ps_insertPower.setString(1, plugName);
            ps_insertPower.setString(2, strDate);
            ps_insertPower.setFloat(3, power);
            ps_insertPower.executeUpdate();
        } catch(SQLException e) { logger.error(e.getMessage()); }
    }

    synchronized public List<String> getGroups() {
        List<String> groups = new ArrayList<String>();
        try {
            ResultSet rs = ps_getGroups.executeQuery();
            while (rs.next()) {
                groups.add(rs.getString("name"));
            }
        } catch(SQLException e) { logger.error(e.getMessage()); }
        return groups;
    }

    synchronized public List<String> getMembers(String group) {
        List<String> members = new ArrayList<String>();
        try {
            ps_getMembers.setString(1, group);
            ResultSet rs = ps_getMembers.executeQuery();
            while (rs.next()) {
                members.add(rs.getString("plugName"));
            }
        } catch(SQLException e) { logger.error(e.getMessage()); }
        return members;
    }

    public void clear() {
        try {
            connection.createStatement().executeUpdate("DELETE FROM groups_plugs");
            connection.createStatement().executeUpdate("DELETE FROM groups");
            connection.createStatement().executeUpdate("DELETE FROM plugs");
        } catch(SQLException e) { logger.error(e.getMessage()); }
    }

    public void close() {
        try { ps_insertGroup.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { ps_deleteMembers.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { ps_deleteGroup.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { ps_insertMember.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { ps_getMembers.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { ps_getGroups.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { ps_insertPlug.close(); } catch (Exception e) { logger.error(e.getMessage()); }
        try { connection.close(); } catch (Exception e) { logger.error(e.getMessage()); }
    }

    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
}
