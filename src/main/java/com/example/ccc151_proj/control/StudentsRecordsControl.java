package com.example.ccc151_proj.control;

import com.example.ccc151_proj.Main;
import com.example.ccc151_proj.model.ContributionProperties;
import com.example.ccc151_proj.model.DataManager;
import com.example.ccc151_proj.model.StudentPaymentInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Facilitates the process of the Class Rep frame.
 */
public class StudentsRecordsControl {
    private static Connection connect;
    @FXML
    private URL location;
    @FXML
    private ResourceBundle resources;
    @FXML
    private TableView<ContributionProperties> contribution_data_table;
    @FXML
    private TableColumn<ContributionProperties, String> code_column;
    @FXML
    private TableColumn<ContributionProperties, String> sem_column;
    @FXML
    private TableColumn<ContributionProperties, Integer> amount_column;
    @FXML
    private TableView<StudentPaymentInfo> student_data_table;
    @FXML
    private TableColumn<StudentPaymentInfo, String> id_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> first_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> middle_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> last_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> suffix_name_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> first_sem_column;
    @FXML
    private TableColumn<StudentPaymentInfo, String> second_sem_column;
    @FXML
    private TextField search_id;
    @FXML
    private MenuItem show_first_sem_transaction;
    @FXML
    private MenuItem show_second_sem_transaction;
    @FXML
    private ComboBox<String> program_code_combobox;
    @FXML
    private ComboBox<String> year_level_combobox;
    private String academic_year;
    private String org_code;

    public StudentsRecordsControl() {
    }

    /**
     * Set the initial display. Along with the information of the class rep user.
     *
     * @param org_code
     */
    public void initialize(String org_code) {
        connect = DataManager.getConnect();
        this.org_code = org_code;
        this.academic_year = DataManager.getAcademic_year();

        setupSearchBlock();
        setupContributionsData();
        setupStudentsData(getPayersList(null, null, null));
    }

    /**
     * Get the contribution details of each organization during the current A.Y.
     *
     * @return the list of contributions for the specific class.
     */
    private ObservableList<ContributionProperties> getContributions() {
        ObservableList<ContributionProperties> contributions_list = FXCollections.observableArrayList();
        try {
            String contribution_data_query = "SELECT `contribution_code`, `semester`,`amount` FROM `contributions` WHERE `academic_year` = '"
                    + academic_year + "' AND `collecting_org_code` = '" + org_code
                    + "' ORDER BY `contribution_code` ASC;";

            PreparedStatement get_contributions_data = connect.prepareStatement(contribution_data_query);
            ResultSet result = get_contributions_data.executeQuery();

            while (result.next()) {
                String contribution_code = result.getString("contribution_code");
                String semester = result.getString("semester");
                Integer amount = result.getInt("amount");
                contributions_list.add(new ContributionProperties(contribution_code, semester, amount));
            }
            get_contributions_data.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contributions_list;
    }

    /**
     * Set up the contributions data needed for the frame.
     */
    private void setupContributionsData() {
        code_column.setCellValueFactory(new PropertyValueFactory<>("contribution_code"));
        sem_column.setCellValueFactory(new PropertyValueFactory<>("contribution_sem"));
        amount_column.setCellValueFactory(new PropertyValueFactory<>("contribution_amount"));
        code_column.setStyle("-fx-alignment: CENTER;");
        sem_column.setStyle("-fx-alignment: CENTER;");
        amount_column.setStyle("-fx-alignment: CENTER;");

        ObservableList<ContributionProperties> details_contribution_ec = getContributions();
        contribution_data_table.setItems(details_contribution_ec);
    }

    /**
     * Set up the necessary parameters for searching blocks.
     */
    private void setupSearchBlock() {
        // store the programs affiliated with the organization here
        ObservableList<String> block_list = FXCollections.observableArrayList();
        block_list.add("--Select Program--");
        try {
            String program_code_query = "SELECT DISTINCT `program_code` \n"
                    + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number`\n"
                    + "WHERE m.`organization_code` = '" + org_code + "';";
            PreparedStatement get_program_code = connect.prepareStatement(program_code_query);
            ResultSet result = get_program_code.executeQuery();
            while (result.next()) {
                block_list.add(result.getString("program_code"));
            }
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        program_code_combobox.setItems(block_list);
        program_code_combobox.getSelectionModel().selectFirst();

        // store the year levels here
        ObservableList<String> year_list = FXCollections.observableArrayList();
        year_list.add("--Select Year--");
        year_list.add("1st Year");
        year_list.add("2nd Year");
        year_list.add("3rd Year");
        year_list.add("4th Year");
        year_level_combobox.setItems(year_list);
        year_level_combobox.getSelectionModel().selectFirst();
    }

    /**
     * Facilitates the searching of the block based on the parameters.
     */
    @FXML
    private void search_block_button_clicked() {
        if (!program_code_combobox.getValue().equals("--Select Program--")
                && !year_level_combobox.getValue().equals("--Select Year--")) {
            student_data_table.getItems().clear();
            search_id.clear();
            ObservableList<StudentPaymentInfo> member_list = getPayersList(null, program_code_combobox.getValue(),
                    year_level_combobox.getValue());
            setupStudentsData(member_list);
        } else {
            Alert success_transaction = new Alert(Alert.AlertType.ERROR);
            success_transaction.setTitle("Empty ID");
            success_transaction.setHeaderText(null);
            success_transaction.setContentText("Please Input ID Number.");
            success_transaction.showAndWait();
        }
    }

    /**
     * Set up the data needed for the frame.
     */
    private void setupStudentsData(ObservableList<StudentPaymentInfo> details_students) {
        student_data_table.getItems().clear();

        id_column.setCellValueFactory(new PropertyValueFactory<>("id_number"));
        first_name_column.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        middle_name_column.setCellValueFactory(new PropertyValueFactory<>("middle_name"));
        last_name_column.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        suffix_name_column.setCellValueFactory(new PropertyValueFactory<>("suffix_name"));
        first_sem_column.setCellValueFactory(new PropertyValueFactory<>("first_sem_status"));
        second_sem_column.setCellValueFactory(new PropertyValueFactory<>("second_sem_status"));

        id_column.setStyle("-fx-alignment: CENTER;");
        first_name_column.setStyle("-fx-alignment: CENTER;");
        middle_name_column.setStyle("-fx-alignment: CENTER;");
        last_name_column.setStyle("-fx-alignment: CENTER;");
        first_sem_column.setStyle("-fx-alignment: CENTER;");
        second_sem_column.setStyle("-fx-alignment: CENTER;");

        student_data_table.setItems(details_students);
    }

    /**
     * Fetches the data of the students that needs to pay the contributions.
     *
     * @param id_number_search
     * @param program_code_search
     * @param year_level_search
     * @return the list of students/members of the organization
     */
    private ObservableList<StudentPaymentInfo> getPayersList(String id_number_search, String program_code_search,
                                                             String year_level_search) {
        // store the data here
        ObservableList<StudentPaymentInfo> member_list = FXCollections.observableArrayList();
        String members_data_query;
        if (id_number_search == null && program_code_search == null && year_level_search == null) {
            // get the list of every member in the organization
            members_data_query = "SELECT `id_number`, `first_name`,`middle_name`, `last_name`, `suffix_name`, `year_level` "
                    + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number` "
                    + "WHERE m.`organization_code` = '" + org_code + "' "
                    + "ORDER BY `last_name` ASC, `first_name` ASC, `middle_name` ASC, `program_code` ASC, `year_level` ASC;";
        } else if (program_code_search == null && year_level_search == null) {
            // get the list of every student with matching id number
            members_data_query = "SELECT `id_number`, `first_name`,`middle_name`, `last_name`, `suffix_name`, `year_level` "
                    + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number` "
                    + "WHERE m.`organization_code` = '" + org_code + "' "
                    + "AND s.`id_number` LIKE '%" + id_number_search + "%' "
                    + "ORDER BY `last_name` ASC, `first_name` ASC, `middle_name` ASC;";
        } else if (id_number_search == null) {
            // get the list of every student in the searched block
            members_data_query = "SELECT `id_number`, `first_name`,`middle_name`, `last_name`, `suffix_name`, `year_level` "
                    + "FROM `members` AS m LEFT JOIN `students` AS s ON m.`member_id` = s.`id_number` "
                    + "WHERE m.`organization_code` = '" + org_code + "' "
                    + "AND s.`program_code` = '" + program_code_search + "' "
                    + "AND s.`year_level` = '" + year_level_search + "' "
                    + "ORDER BY `last_name` ASC, `first_name` ASC, `middle_name` ASC;";
        } else {
            return null;
        }

        try {
            PreparedStatement get_members_data = connect.prepareStatement(members_data_query);
            ResultSet result = get_members_data.executeQuery();
            while (result.next()) {
                String id_number = result.getString("id_number");
                String first_name = result.getString("first_name");
                String middle_name = result.getString("middle_name");
                String last_name = result.getString("last_name");
                String suffix_name = result.getString("suffix_name");
                String year_level = result.getString("year_level");

                /*
                 * set the initial status to Not Paid in case there are no data to be fetched
                 * since there was no transaction made still
                 */
                String first_sem_status = "Not Paid";
                String second_sem_status = "Not Paid";
                String payment_status_query;
                PreparedStatement get_payment_status;
                ResultSet result_2;
                if (contribution_data_table.getItems().get(0).getContribution_amount() <= 0) {
                    first_sem_status = "Paid"; // if there is no contribution to be paid
                } else {
                    // get their status for first sem
                    payment_status_query = "SELECT `status` FROM `pays` "
                            + "WHERE `payer_id` = '" + id_number + "' "
                            + "AND `contribution_code` = '"
                            + contribution_data_table.getItems().get(0).getContribution_code() + "' "
                            + "ORDER BY `transaction_id` DESC;";

                    get_payment_status = connect.prepareStatement(payment_status_query);
                    result_2 = get_payment_status.executeQuery();
                    if (result_2.next()) {
                        first_sem_status = switch (result_2.getString("status")) {
                            case "Accepted" -> "Paid";
                            default -> result_2.getString("status");
                        };
                    }
                    result_2.close();
                }

                if (contribution_data_table.getItems().get(1).getContribution_amount() <= 0) {
                    second_sem_status = "Paid"; // if there is no contribution to be paid
                } else {
                    // get their status for second sem
                    payment_status_query = "SELECT `status` FROM `pays` WHERE `payer_id` = '" + id_number + "' "
                            + "AND `contribution_code` = '"
                            + contribution_data_table.getItems().get(1).getContribution_code() + "' "
                            + "ORDER BY `transaction_id` DESC;";
                    get_payment_status = connect.prepareStatement(payment_status_query);
                    result_2 = get_payment_status.executeQuery();
                    if (result_2.next()) {
                        second_sem_status = switch (result_2.getString("status")) {
                            case "Accepted" -> "Paid";
                            default -> result_2.getString("status");
                        };
                    }
                    result_2.close();
                }

                member_list.add(new StudentPaymentInfo(id_number, first_name, middle_name, last_name, suffix_name,
                        year_level, first_sem_status, second_sem_status));
            }
            get_members_data.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return member_list;
    }

    /**
     * Facilitates the payment form for the first sem.
     *
     * @throws IOException
     */
    @FXML
    private void menu_first_sem() throws IOException {
        // get the selected payer
        StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
        if (payer != null) {
            if (payer.getFirst_sem_status().equals("Paid")) {
                // if the payer already paid
                Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
                non_selected.setTitle("Student Already Paid.");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Student Already Paid. Select another student.");
                non_selected.showAndWait();
            } else if (payer.getFirst_sem_status().equals("Pending")) {
                // if the recent payment is still pending
                Alert non_selected = new Alert(Alert.AlertType.ERROR);
                non_selected.setTitle("Multiple Transaction Error");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Previous payment is still unverified, try again after verification.");
                non_selected.showAndWait();
            } else {
                Stage transaction_stage = new Stage();
                transaction_stage.setResizable(false);
                transaction_stage.initModality(Modality.APPLICATION_MODAL);

                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("transaction-form.fxml"));
                Parent transaction_parent = fxmlLoader.load();
                TransactionProcess transaction_process = fxmlLoader.getController();
                transaction_process.setPayer(payer);
                transaction_process.initialize(contribution_data_table.getItems().get(0).getContribution_code());

                Scene transaction_scene = new Scene(transaction_parent);
                transaction_stage.setTitle("Contribution Payment Transaction");
                transaction_stage.setScene(transaction_scene);
                transaction_stage.show();
            }
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Student Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Student.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the review of the payment for the second sem.
     *
     * @throws IOException
     */
    @FXML
    void view_first_sem() throws IOException {
        if (contribution_data_table.getItems().get(0).getContribution_amount() > 0) {
            StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
            Stage transaction_stage = new Stage();
            transaction_stage.setResizable(false);
            transaction_stage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("review-transaction-form.fxml"));
            Parent transaction_parent = fxmlLoader.load();
            ReviewTransactionControl transaction_process = fxmlLoader.getController();
            transaction_process.initialize(payer.getId_number(),
                    contribution_data_table.getItems().get(0).getContribution_code());

            Scene transaction_scene = new Scene(transaction_parent);
            transaction_stage.setTitle("View Transaction");
            transaction_stage.setScene(transaction_scene);
            transaction_stage.show();
        } else {
            Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
            non_selected.setTitle("Contribution Not Payable");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Contribution is not being collected. Amount = 0.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the payment form for the second sem.
     *
     * @throws IOException
     */
    @FXML
    private void menu_second_sem() throws IOException {
        // get the selected payer
        StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
        if (payer != null) {
            if (payer.getSecond_sem_status().equals("Paid")) {
                // if the payer already paid
                Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
                non_selected.setTitle("Student Already Paid.");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Student Already Paid. Select another student.");
                non_selected.showAndWait();
            } else if (payer.getSecond_sem_status().equals("Pending")) {
                // if the recent payment is still pending
                Alert non_selected = new Alert(Alert.AlertType.ERROR);
                non_selected.setTitle("Multiple Transaction Error");
                non_selected.setHeaderText(null);
                non_selected.setContentText("Previous payment is still unverified, try again after verification.");
                non_selected.showAndWait();
            } else {
                Stage transaction_stage = new Stage();
                transaction_stage.setResizable(false);
                transaction_stage.initModality(Modality.APPLICATION_MODAL);

                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("transaction-form.fxml"));
                Parent transaction_parent = fxmlLoader.load();
                TransactionProcess transaction_process = fxmlLoader.getController();
                transaction_process.setPayer(payer);
                transaction_process.initialize(contribution_data_table.getItems().get(1).getContribution_code());

                Scene transaction_scene = new Scene(transaction_parent);
                transaction_stage.setTitle("Contribution Payment Transaction");
                transaction_stage.setScene(transaction_scene);
                transaction_stage.show();
            }
        } else {
            Alert non_selected = new Alert(Alert.AlertType.ERROR);
            non_selected.setTitle("No Student Selected");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Please Select a Student.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the review of the payment for the second sem.
     *
     * @throws IOException
     */
    @FXML
    void view_second_sem() throws IOException {
        if (contribution_data_table.getItems().get(1).getContribution_amount() > 0) {
            StudentPaymentInfo payer = student_data_table.getSelectionModel().getSelectedItem();
            Stage transaction_stage = new Stage();
            transaction_stage.setResizable(false);
            transaction_stage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("review-transaction-form.fxml"));
            Parent transaction_parent = fxmlLoader.load();
            ReviewTransactionControl transaction_process = fxmlLoader.getController();
            transaction_process.initialize(payer.getId_number(),
                    contribution_data_table.getItems().get(1).getContribution_code());

            Scene transaction_scene = new Scene(transaction_parent);
            transaction_stage.setTitle("View Transaction");
            transaction_stage.setScene(transaction_scene);
            transaction_stage.show();
        } else {
            Alert non_selected = new Alert(Alert.AlertType.INFORMATION);
            non_selected.setTitle("Contribution Not Payable");
            non_selected.setHeaderText(null);
            non_selected.setContentText("Contribution is not being collected. Amount = 0.");
            non_selected.showAndWait();
        }
    }

    /**
     * Facilitates the searching of a student based on id_number.
     */
    @FXML
    private void searchID() {
        if (!search_id.getText().isEmpty()) {
            student_data_table.getItems().clear();
            program_code_combobox.getSelectionModel().selectFirst();
            year_level_combobox.getSelectionModel().selectFirst();
            ObservableList<StudentPaymentInfo> member_list = getPayersList(search_id.getText(), null, null);
            setupStudentsData(member_list);
        } else {
            Alert success_transaction = new Alert(Alert.AlertType.ERROR);
            success_transaction.setTitle("Empty ID");
            success_transaction.setHeaderText(null);
            success_transaction.setContentText("Please Input ID Number.");
            success_transaction.showAndWait();
        }
    }

    /**
     * Refreshes the display.
     */
    @FXML
    private void resetSearch() {
        student_data_table.getItems().clear();
        search_id.clear();
        program_code_combobox.getSelectionModel().selectFirst();
        year_level_combobox.getSelectionModel().selectFirst();
        setupContributionsData();
        ObservableList<StudentPaymentInfo> details_students = getPayersList(null, null, null);
        student_data_table.setItems(details_students);
    }

    /**
     * Disables menus when certain conditions aren't meet.
     */
    @FXML
    private void setupContextMenu() {
        StudentPaymentInfo student_selected = student_data_table.getSelectionModel().getSelectedItem();
        if (student_selected == null) {
            show_first_sem_transaction.setDisable(true);
            show_second_sem_transaction.setDisable(true);
        } else {
            show_first_sem_transaction.setDisable(student_selected.getFirst_sem_status().equals("Not Paid"));
            show_second_sem_transaction.setDisable(student_selected.getSecond_sem_status().equals("Not Paid"));
        }
    }
}