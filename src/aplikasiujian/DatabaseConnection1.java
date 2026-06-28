/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aplikasiujian;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author user
 */
public class DatabaseConnection1 {
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/simple_math_exam";
                String user = "root";
                String password = ""; 
                
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Connection Failed: " + e.getMessage());
            }
        }
        return connection;
    }
}
