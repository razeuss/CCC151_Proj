package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.StudentPaymentInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
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
        String[] contribution_details = contribution_code.split("_");
        this.org_code = contribution_details[0];
        this.academic_year = contribution_details[1];
        this.semester = contribution_details[2];

        //set up the title
        transaction_label.setText(org_code + " Transaction");
        transaction_semester.setText(semester);

        //add the year levels
        ObservableList<String> year_levels = FXCollections.observableArrayList();
        year_levels.add("1st Year");
        year_levels.add("2nd Year");
        year_levels.add("3rd Year");
        year_levels.add("4th Year");
        transaction_payer_year.setItems(year_levels);
        transaction_payer_year.getSelectionModel().select(0);

        //add the modes of payment
        ObservableList<String> modes = FXCollections.observableArrayList();
        modes.add("Cash");
        modes.add("GCash");
        modes.add("Others");
        transaction_payment_mode.setItems(modes);
        transaction_payment_mode.getSelectionModel().select(0);
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
        payer.setFirst_sem_status("Pending.");
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
