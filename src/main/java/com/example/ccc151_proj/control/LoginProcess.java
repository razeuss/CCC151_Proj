package com.example.ccc151_proj.control;

import com.example.ccc151_proj.Main;
import com.example.ccc151_proj.model.DataManager;
import com.example.ccc151_proj.model.Security;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Facilitates the login process. Will check the information of the user then
 * display the appropriate frame of the user.
 */
public class LoginProcess implements Initializable {
    private static String academic_year;
    private static Connection connect;

    @FXML
    private TextField id_input;
    @FXML
    private PasswordField pass_input;
    @FXML
    private TextField pass_input_show;
    @FXML
    private RadioButton show_password_rbutton;

    public LoginProcess() {
    }

    /**
     * Will set up the initial connection to the database. Then fetch necessary
     * data.
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
     * Facilitates the login process and the display of the main frame based on the
     * user's position.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void loginOperation(ActionEvent event) {
        // if the user is listed and the password was changed
        if (isUserListed(this.id_input.getText(), passwordValue()) && !passwordValue().equals(this.id_input.getText())) {
            // close the login stage
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

            // choose the display based on the position of the user
            String user_position = getUserPosition();
            displayMainFrame(user_position);
        } else {
            // if the user is not listed in the database
            if (!isUserListed(this.id_input.getText(), passwordValue())) {
                ButtonType forgot_password = new ButtonType("Forgot Password", ButtonBar.ButtonData.OTHER);
                ButtonType ok_button = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                Alert connection_error = new Alert(Alert.AlertType.ERROR, "Wrong ID or Password.",ok_button, forgot_password);
                connection_error.setTitle("Login Unsuccessful");
                connection_error.setHeaderText(null);
                Optional<ButtonType> result = connection_error.showAndWait();
                result.ifPresent(res -> {
                    if (result.get() == forgot_password) {
                        Alert inform = new Alert(Alert.AlertType.INFORMATION);
                        inform.setTitle("Notify the Developer.");
                        inform.setHeaderText("Request for a password change.");
                        inform.setContentText("Please email c6918434@gmail.com your Student ID and request for password change. Thank You");
                        inform.showAndWait();
                        connection_error.close();
                    } else {
                        connection_error.close();
                    }
                });
                return;
            }

            // if the password isn't changed (id == password)
            if (passwordValue().equals(this.id_input.getText())) {
                // set up the alert
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
                Alert pass_not_changed = new Alert(Alert.AlertType.CONFIRMATION,
                        "Please change your password for better security.", yes, no);
                pass_not_changed.setTitle("Original Password still active.");
                pass_not_changed.setHeaderText("You haven't changed your password. Do you want to change it now?");
                Optional<ButtonType> result = pass_not_changed.showAndWait();
                result.ifPresent(res -> {
                    if (result.get() == yes) {
                        try {
                            // close the login stage
                            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

                            Stage change_pass_stage = new Stage();
                            // initialize the loader for the change password form
                            FXMLLoader change_pass_loader = new FXMLLoader(
                                    Main.class.getResource("change-password.fxml"));
                            Parent change_pass_parent = change_pass_loader.load();
                            // initialize the controller
                            ChangePasswordControl change_pass_control = change_pass_loader.getController();
                            change_pass_control.initialize(this.id_input.getText());

                            // create the scene
                            Scene change_pass_scene = new Scene(change_pass_parent);
                            change_pass_stage.setScene(change_pass_scene);
                            change_pass_stage.show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // if the user doesn't want to change the password yet
                        // close the login stage
                        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

                        // choose the display based on the position of the user
                        String user_position = getUserPosition();
                        displayMainFrame(user_position);
                    }
                });
            }
        }
    }

    /**
     * Show password functionality.
     */
    @FXML
    private void show_password_rbutton_clicked() {
        if (show_password_rbutton.isSelected()) {
            pass_input.setVisible(false);
            pass_input_show.setVisible(true);
            pass_input_show.setText(pass_input.getText());
        } else {
            pass_input.setVisible(true);
            pass_input_show.setVisible(false);
            pass_input.setText(pass_input_show.getText());
        }
    }

    /**
     * To properly get the data from the password field.
     *
     * @return password
     */
    private String passwordValue() {
        return show_password_rbutton.isSelected() ? pass_input_show.getText() : pass_input.getText();
    }

    /**
     * Choose and display the frame based on the user's position.
     *
     * @param user_position
     */
    private void displayMainFrame(String user_position) {
        try {
            // create a new stage for the main frame
            Stage main_stage = new Stage();
            main_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
            main_stage.setResizable(false);
            main_stage.setTitle("Contribution Payment System");
            main_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));

            if (user_position.equals("Classroom Representative")) {
                // initialize the loader
                FXMLLoader class_rep_loader = new FXMLLoader(Main.class.getResource("class-rep-frame.fxml"));
                Parent class_rep_parent = class_rep_loader.load();

                // initialize the controller
                ClassRepControl class_rep_control = class_rep_loader.getController();
                class_rep_control.initialize(this.id_input.getText(), LoginProcess.academic_year);

                // create the scene
                Scene class_rep_scene = new Scene(class_rep_parent);
                main_stage.setScene(class_rep_scene);
            } else {
                // set up the initial scene for the BUFICOM
                // initialize the loader
                FXMLLoader buficom_info_loader = new FXMLLoader(
                        Main.class.getResource("BUFICOM-FRAMES/buficom-info.fxml"));
                FXMLLoader dashboard_loader = new FXMLLoader(
                        Main.class.getResource("BUFICOM-FRAMES/verify-payments.fxml"));

                // load the panes
                AnchorPane info = buficom_info_loader.load();
                AnchorPane dashboard = dashboard_loader.load();

                // place the panes into one parent
                HBox buficom_parent = new HBox(0);
                buficom_parent.getChildren().add(info);
                buficom_parent.getChildren().add(dashboard);

                // initialize the controllers
                BuficomInfoControl buficom_info_control = buficom_info_loader.getController();
                buficom_info_control.initialize(buficom_parent, this.id_input.getText(), getUserPosition());
                VerifyPaymentsControl dashboard_control = dashboard_loader.getController();
                dashboard_control.initialize(buficom_info_control.getOrg_code());

                // create the scene
                Scene buficom_scene = new Scene(buficom_parent);
                main_stage.setScene(buficom_scene);
            }
            // show the stage
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
        String recorded_pass = null;
        try {
            // fetch the password from the database
            String check_query = "SELECT `password` FROM users WHERE `user_id` = '" + id_number + "';";
            PreparedStatement check_user_info = LoginProcess.connect.prepareStatement(check_query);
            ResultSet result = check_user_info.executeQuery();
            if (result.next())
                recorded_pass = result.getString("password");
            else
                return false; // if the user isn't listed
            check_user_info.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (id_number.equals(password))
            return password.equals(recorded_pass); // if the password wasn't changed
        else
            return Security.verifyPassword(password, recorded_pass); // if the password was changed
    }

    /**
     * Get the user's information/position.
     *
     * @return user_position
     */
    private String getUserPosition() {
        String user_position = "";
        try {
            // fetch the position of the user from the database
            String position_query = "SELECT `position` FROM `manages` WHERE `officer_id` = '"
                    + id_input.getText() + "';";
            PreparedStatement check_user_position = connect.prepareStatement(position_query);
            ResultSet result = check_user_position.executeQuery();
            if (result.next())
                user_position = result.getString("position");
            check_user_position.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user_position;
    }
}
