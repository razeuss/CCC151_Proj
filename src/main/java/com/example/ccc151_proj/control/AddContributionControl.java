package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.ContributionProperties;
import com.example.ccc151_proj.model.DataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddContributionControl {
    private static Connection connect;
    private String org_code;
    private String academic_year;
    @FXML
    private TextField organization_code_textfield;
    @FXML
    private TextField academic_year_textfield;
    @FXML
    private ComboBox<String> semester_combobox;
    @FXML
    private TextField amount_textfield;
    @FXML
    private TableView<ContributionProperties> contribution_data_table;
    @FXML
    private TableColumn<ContributionProperties, String> code_column;
    @FXML
    private TableColumn<ContributionProperties, String> academic_year_column;
    @FXML
    private TableColumn<ContributionProperties, String> sem_column;
    @FXML
    private TableColumn<ContributionProperties, Integer> amount_column;
    @FXML
    private Button clear_selection_button;
    @FXML
    private Button edit_contribution_button;
    public AddContributionControl(){}

    public void initialize(String org_code, String academic_year){
        connect = DataManager.getConnect();
        this.org_code = org_code;
        this.academic_year = academic_year;

        organization_code_textfield.setText(this.org_code);
        academic_year_textfield.setText(this.academic_year);
        ObservableList<String> semester_list = FXCollections.observableArrayList();
        semester_list.add("1");
        semester_list.add("2");
        semester_combobox.setItems(semester_list);
        semester_combobox.getSelectionModel().selectFirst();
        ObservableList<ContributionProperties> contribution_list = FXCollections.observableArrayList();
        try {
            String contribution_info_query = "SELECT `contribution_code`, `semester`, `amount` FROM `contributions` " +
                    "WHERE `collecting_org_code` = \"" + this.org_code + "\" " +
                    "AND `academic_year` = \"" + this.academic_year + "\";";
            PreparedStatement get_contribution_info = connect.prepareStatement(contribution_info_query);
            ResultSet result = get_contribution_info.executeQuery();
            while (result.next()){
                String contribution_code = result.getString("contribution_code");
                String semester = result.getString("semester");
                Integer amount = result.getInt("amount");

                contribution_list.add(new ContributionProperties(contribution_code, this.academic_year, semester, amount));
            }
            setupContributionData(contribution_list);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupContributionData(ObservableList<ContributionProperties> data){
        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        academic_year_column.setCellValueFactory(new PropertyValueFactory<>("academic_year"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        academic_year_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        contribution_data_table.setOnMouseClicked(event -> {
            // Make sure the user clicked on a populated item
            ContributionProperties contribution = contribution_data_table.getSelectionModel().getSelectedItem();
            if (contribution != null) {
                organization_code_textfield.setText(contribution.getContribution_code().split("_")[0]);
                academic_year_textfield.setText(contribution.getAcademic_year());
                semester_combobox.getItems().clear();
                semester_combobox.getItems().add(contribution.getContribution_sem());
                semester_combobox.getSelectionModel().selectFirst();
                amount_textfield.setText(String.valueOf(contribution.getContribution_amount()));
                edit_contribution_button.setDisable(false);
            }
        });

        contribution_data_table.setItems(data);
    }

    @FXML
    private void clear_selection_button_clicked(){
        semester_combobox.getSelectionModel().selectFirst();
        amount_textfield.clear();
        edit_contribution_button.setDisable(true);
    }

    @FXML
    private void edit_contribution_button_clicked(){
        try {
            String edit_contribution_query = "UPDATE `contributions` SET `semester` = ?, `amount` = ? WHERE `contribution_code` = ?;";
            PreparedStatement edit_contribution = connect.prepareStatement(edit_contribution_query);
            edit_contribution.setString(1, semester_combobox.getValue());
            edit_contribution.setInt(2, Integer.parseInt(amount_textfield.getText()));
            edit_contribution.setString(3, contribution_data_table.getSelectionModel().getSelectedItem().getContribution_code());
            edit_contribution.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initialize(this.org_code, this.academic_year);
        clear_selection_button_clicked();
    }
}
