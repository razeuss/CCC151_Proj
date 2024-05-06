package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.DataManager;
import com.example.ccc151_proj.model.StudentPaymentInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * For recording transactions.
 */
public class TransactionProcess {
    @FXML private URL location;
    @FXML private ResourceBundle resources;
    @FXML private AnchorPane transaction_scene;
    @FXML private TextField transaction_label;
    @FXML private TextField transaction_payer_id;
    @FXML private TextField transaction_payer_name;
    @FXML private ComboBox<String> transaction_payer_year;
    @FXML private TextField transaction_amount;
    @FXML private TextField transaction_semester;
    @FXML private ComboBox<String> transaction_payment_mode;
    @FXML private TextField transaction_payer_receipt;

    private String contribution_code;
    private String org_code;
    private String semester;
    private String academic_year;
    private StudentPaymentInfo payer;

    public TransactionProcess(){}

    /**
     * Set up the initial frame.
     * @param contribution_code
     */
    public void initialize(String contribution_code) {
        //get the information of the contribution from its code
        this.contribution_code = contribution_code;
        String[] contribution_details = this.contribution_code.split("_");
        this.org_code = contribution_details[0];
        this.academic_year = contribution_details[1];
        this.semester = contribution_details[2];

        //set up the title
        transaction_label.setText(org_code + " Transaction");
        transaction_semester.setText(semester);

        //get the amount
        try {
            Connection connect = DataManager.getConnect();
            String amount_query = "SELECT `amount` FROM `contributions` WHERE `contribution_code` = \"" + this.contribution_code + "\";";
            PreparedStatement get_amount = connect.prepareStatement(amount_query);
            ResultSet result_id = get_amount.executeQuery();
            result_id.next();
            String amount = result_id.getString("amount");
            transaction_amount.setText(amount);
            result_id.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //add the year levels
        ObservableList<String> year_levels = FXCollections.observableArrayList();
        year_levels.add("1st Year");
        year_levels.add("2nd Year");
        year_levels.add("3rd Year");
        year_levels.add("4th Year");
        transaction_payer_year.setItems(year_levels);
        transaction_payer_year.getSelectionModel().select(payer.getYear_level());

        //add the modes of payment
        ObservableList<String> modes = FXCollections.observableArrayList();
        modes.add("Cash");
        modes.add("GCash");
        modes.add("Others");
        transaction_payment_mode.setItems(modes);
        transaction_payment_mode.getSelectionModel().select(0);
        transaction_payer_receipt.setDisable(true);
        //disable the receipt input if the mode is Cash
        transaction_payment_mode.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            //disable the input of receipt when the mode of payment is "Cash"
            transaction_payer_receipt.setDisable(transaction_payment_mode.getValue().equals("Cash"));
        });
    }

    /**
     * For recording of transactions.
     */
    @FXML
    private void recordTransaction(){
        //process here
        try {
            Connection connect = DataManager.getConnect();
            String update_payer_status_query = "INSERT INTO `payments` (`contribution_code`, `payer_id`, `payment_mode`, `status`)\n" +
                    "VALUES (?, ?, ?, \"Pending\");";
            PreparedStatement insert_payer_status = connect.prepareStatement(update_payer_status_query);
            insert_payer_status.setString(1, this.contribution_code);
            insert_payer_status.setString(2, payer.getId_number());
            insert_payer_status.setString(3, transaction_payment_mode.getValue());
            insert_payer_status.executeUpdate();
            insert_payer_status.close();

            String transaction_id_query = "SELECT `transaction_id` FROM `payments` WHERE `contribution_code` = \"" + this.contribution_code
                    + "\" AND `payer_id` = \"" + payer.getId_number() + "\";";
            PreparedStatement get_transaction_id = connect.prepareStatement(transaction_id_query);
            ResultSet result_id = get_transaction_id.executeQuery();
            result_id.next();
            String transaction_id = result_id.getString("transaction_id");
            result_id.close();

            Alert success_transaction = new Alert(Alert.AlertType.INFORMATION);
            success_transaction.setTitle("Transaction Successful");
            success_transaction.setHeaderText(null);
            success_transaction.setContentText("Transaction Successful (with Transaction ID = " + transaction_id +") for Student " + payer.getId_number() +
                    ". Please wait for verification from the BUFICOM Officers. Thank you.");
            success_transaction.showAndWait();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        payer.setFirst_sem_status("Pending");
        System.out.println(org_code + "|" + semester + "|" + transaction_payer_id.getText() + "|"
                + transaction_payer_name.getText() + "|" + transaction_payer_year.getValue() + "|"
                + transaction_amount.getText() + "|" + transaction_payment_mode.getValue());
        //close the window
        ((Stage) transaction_scene.getScene().getWindow()).close();
    }

    /*
    Setters and Getters.
     */

    /**
     * Automatically add the information of the payer from the table.
     * @param payer
     */
    public void setPayer(StudentPaymentInfo payer) {
        transaction_payer_id.setText(payer.getId_number());
        transaction_payer_name.setText(payer.getLast_name() + ", " + payer.getFirst_name() + " " + payer.getMiddle_name());
        this.payer = payer;
    }

    public String getOrg_code() {
        return org_code;
    }

    public void setOrg_code(String org_code) {

        this.org_code = org_code;
    }

    public String getSemester() {

        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public StudentPaymentInfo getPayer() {
        return payer;
    }

    public String getAcademic_year() {
        return academic_year;
    }

    public void setAcademic_year(String academic_year) {
        this.academic_year = academic_year;
    }
}
