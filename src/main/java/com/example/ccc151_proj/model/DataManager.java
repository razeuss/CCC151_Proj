package com.example.ccc151_proj.model;

import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to the Database.
 */
public class DataManager {
    private static Connection connect;
    //TODO: Change the Academic Year often as I don't know how to change it automatically :<<
    private static String academic_year = "2023-2024";

    public DataManager() {
    }

    /**
     * Create a connection to the database.
     */
    public static void createConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // change for new connection (this is for my personal connection)
            String sql_name = "mysql";
            String connection_name = "127.0.0.1";
            String port = "3306";
            String schema_name = "student_payment_system";
            String username = "root";
            String password = "rootPassword";

            connect = DriverManager.getConnection("jdbc:" + sql_name + "://" + connection_name + ":" + port + "/" + schema_name, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Database Connection Error");
            connection_error.setHeaderText(null);
            connection_error.setContentText(e.toString());
            connection_error.showAndWait();
        }
    }

    /**
     * Get the connection from the database.
     *
     * @return connection to the database
     */
    public static Connection getConnect() {
        return connect;
    }

    public static String getAcademic_year() {
        return academic_year;
    }

    public static void setAcademic_year(String academic_year) {
        DataManager.academic_year = academic_year;
    }

}
