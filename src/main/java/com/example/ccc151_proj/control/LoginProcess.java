package com.example.ccc151_proj.control;

import com.example.ccc151_proj.Main;
import com.example.ccc151_proj.model.DataManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Facilitates the login process. Will check the information and display the appropriate frame of the user.
 */
public class LoginProcess implements Initializable {
    private static String academic_year;
    private static Connection connect;

    @FXML
    private TextField id_input;
    @FXML
    private PasswordField pass_input;

    public LoginProcess() {}

    /**
     * Will set up the initial connection to the database.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DataManager.createConnection();
        connect = DataManager.getConnect();
        academic_year = DataManager.getAcademic_year();
    }

    /**
     * Facilitates the login process and the display of the main frame based on the user's position.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void loginOperation(ActionEvent event) {
        // if the user is not listed in the database
        if (!isUserListed(this.id_input.getText(), this.pass_input.getText())) {
            Alert connection_error = new Alert(Alert.AlertType.ERROR);
            connection_error.setTitle("Login Unsuccessful");
            connection_error.setHeaderText(null);
            connection_error.setContentText("Wrong ID or Password.");
            connection_error.showAndWait();
            return;
        }

        // close the login stage
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();


        // choose the display based on the position of the user
        String user_position = getUserPosition();
        displayMainFrame(user_position);

    }

    private void displayMainFrame(String user_position) {
        try {
            // create a new stage for the main frame
            Stage main_stage = new Stage();
            main_stage.setTitle("Contribution Payment System");
            main_stage.setResizable(false);
            FXMLLoader fxmlLoader;

            if (user_position.equals("Classroom Representative")) {
                // initialize the loader
                fxmlLoader = new FXMLLoader(Main.class.getResource("class-rep-frame.fxml"));
                Parent class_rep_parent = fxmlLoader.load();

                // initialize the controller
                ClassRepControl class_rep_control = fxmlLoader.getController();
                class_rep_control.initialize(this.id_input.getText(), LoginProcess.academic_year);

                // create the scene
                Scene class_rep_scene = new Scene(class_rep_parent);
                main_stage.setScene(class_rep_scene);
            } else {
                //TODO: Add display for the other positions and admin.
                //frame_path = "buficom-frame.fxml";
                System.out.println(user_position);
            }
            main_stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the user is in the database.
     *
     * @param id_number
     * @param password
     * @return true if the user is listed in the database, false otherwise.
     */
    private boolean isUserListed(String id_number, String password) {
        String recorded_pass = "";
        try {
            String check_query = "SELECT `password` FROM users WHERE `user_id` = \"" + id_number + "\";";
            PreparedStatement check_user_info = LoginProcess.connect.prepareStatement(check_query);
            ResultSet result = check_user_info.executeQuery();
            while (result.next()) recorded_pass = result.getString("password");
            check_user_info.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return password.equals(recorded_pass);
    }

    /**
     * Get the user's information/position.
     *
     * @return user_position
     */
    private String getUserPosition() {
        String user_position = "";
        try {
            Connection connect = DataManager.getConnect();
            String position_query = "SELECT `position` FROM `manages` WHERE `officer_id` = \"" + this.id_input.getText() + "\";";
            PreparedStatement check_user_position = connect.prepareStatement(position_query);
            ResultSet result = check_user_position.executeQuery();
            while (result.next()) user_position = result.getString("position");
            check_user_position.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user_position;
    }
}
