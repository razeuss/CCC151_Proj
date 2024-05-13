package com.example.ccc151_proj.control;

import com.example.ccc151_proj.Main;
import com.example.ccc151_proj.model.DataManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Facilitates the process of the BUFICOM frame.
 */
public class BuficomInfoControl {
    private static Connection connect;
    private String user_id_number;
    @FXML
    private TextField user_position_textfield;
    @FXML
    private TextField org_code_textfield;
    @FXML
    private TextField org_name_textfield;
    @FXML
    private ImageView org_image;
    @FXML
    private Button verify_payments_button;
    @FXML
    private Button transaction_history_button;
    @FXML
    private Button students_records_button;
    private HBox parent;

    public BuficomInfoControl() {
    }

    /**
     * Set the initial display. Along with the information of the BUFICOM user.
     *
     * @param parent
     * @param user_id_number
     * @param user_position
     */
    public void initialize(HBox parent, String user_id_number, String user_position) {
        this.parent = parent;

        // just use this default photo because we don't have enough time to implement
        // changing of profile. Bitaw kapoy na :<<
        File file = new File("src/src/default-org-logo.jpg");
        Image image = new Image(file.toURI().toString());
        org_image.setImage(image);

        connect = DataManager.getConnect();
        this.user_id_number = user_id_number;
        user_position_textfield.setText(user_position);
        getUserOrgInfo();
    }

    public String getOrg_code() {
        return org_code_textfield.getText();
    }

    /**
     * Facilitates the changing of the dashboard to Verify Payments Dashboard
     */
    @FXML
    private void verify_payments_clicked() {
        verify_payments_button.setDisable(true);
        transaction_history_button.setDisable(false);
        students_records_button.setDisable(false);
        ObservableList<Node> children = parent.getChildren();
        parent.getChildren().remove(children.get(1));
        try {
            FXMLLoader dashboard_loader = new FXMLLoader(
                    Main.class.getResource("BUFICOM-FRAMES/verify-payments.fxml"));
            AnchorPane dashboard = dashboard_loader.load();
            VerifyPaymentsControl dashboard_control = dashboard_loader.getController();
            dashboard_control.initialize(org_code_textfield.getText());
            parent.getChildren().add(dashboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Facilitates the changing of the dashboard to Transaction History Dashboard
     */
    @FXML
    private void transaction_history_clicked() {
        verify_payments_button.setDisable(false);
        transaction_history_button.setDisable(true);
        students_records_button.setDisable(false);
        ObservableList<Node> children = parent.getChildren();
        parent.getChildren().remove(children.get(1));
        try {
            FXMLLoader dashboard_loader = new FXMLLoader(
                    Main.class.getResource("BUFICOM-FRAMES/transaction-history.fxml"));
            AnchorPane dashboard = dashboard_loader.load();
            TransactionHistoryControl dashboard_control = dashboard_loader.getController();
            dashboard_control.initialize(org_code_textfield.getText());
            parent.getChildren().add(dashboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Facilitates the changing of the dashboard to Students' Records Dashboard
     */
    @FXML
    private void students_records_clicked() {
        verify_payments_button.setDisable(false);
        transaction_history_button.setDisable(false);
        students_records_button.setDisable(true);
        ObservableList<Node> children = parent.getChildren();
        parent.getChildren().remove(children.get(1));
        try {
            FXMLLoader dashboard_loader = new FXMLLoader(
                    Main.class.getResource("BUFICOM-FRAMES/students-records.fxml"));
            AnchorPane dashboard = dashboard_loader.load();
            StudentsRecordsControl dashboard_control = dashboard_loader.getController();
            dashboard_control.initialize(org_code_textfield.getText());
            parent.getChildren().add(dashboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Display and facilitates the Add Contribution form.
     */
    @FXML
    private void edit_contribution_button_clicked() {
        try {
            Stage add_contribution_stage = new Stage();
            add_contribution_stage.setResizable(false);

            add_contribution_stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader add_contribution_loader = new FXMLLoader(
                    Main.class.getResource("BUFICOM-FRAMES/add-contribution-form.fxml"));
            Parent add_contribution_parent = add_contribution_loader.load();
            AddContributionControl add_contribution_control = add_contribution_loader.getController();
            add_contribution_control.initialize(org_code_textfield.getText());

            Scene add_contribution_scene = new Scene(add_contribution_parent);
            add_contribution_stage.setScene(add_contribution_scene);
            add_contribution_stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the organization code of the user.
     */
    private void getUserOrgInfo() {
        try {
            String org_code_query = "SELECT * FROM `manages` AS m LEFT JOIN `organizations` AS o "
                    + "ON m.`organization_code` = o.`organization_code`\n"
                    + "WHERE `officer_id` =  '" + user_id_number + "';";
            PreparedStatement get_org_code = connect.prepareStatement(org_code_query);
            ResultSet result = get_org_code.executeQuery();
            if (result.next()) {
                org_code_textfield.setText(result.getString(1));
                org_name_textfield.setText(result.getString("organization_name"));
            }
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
