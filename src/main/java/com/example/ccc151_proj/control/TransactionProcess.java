package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.DataManager;
import com.example.ccc151_proj.model.StudentPaymentInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * For recording transactions.
 */
public class TransactionProcess {
    private final Connection connect = DataManager.getConnect();
    @FXML
    private URL location;
    @FXML
    private ResourceBundle resources;
    @FXML
    private AnchorPane transaction_scene;
    @FXML
    private TextField transaction_label;
    @FXML
    private TextField transaction_payer_id;
    @FXML
    private TextField transaction_payer_name;
    @FXML
    private ComboBox<String> transaction_payer_year;
    @FXML
    private TextField transaction_amount;
    @FXML
    private TextField transaction_semester;
    @FXML
    private ComboBox<String> transaction_payment_mode;
    @FXML
    private Button add_receipt_button;
    @FXML
    private Hyperlink receipt_link;
    private String contribution_code;
    private String org_code;
    private String semester;
    private String academic_year;
    private StudentPaymentInfo payer;

    public TransactionProcess() {
    }

    /**
     * Set up the initial frame.
     *
     * @param contribution_code
     */
    public void initialize(String contribution_code, boolean transact) {
        // get the information of the contribution from its code
        this.contribution_code = contribution_code;
        String[] contribution_details = this.contribution_code.split("_");
        org_code = contribution_details[0];
        academic_year = contribution_details[1];
        semester = contribution_details[2];

        // set up the title
        transaction_label.setText(org_code + " Transaction");
        transaction_semester.setText(semester);

        if (transact)
            setupTransaction();
        else
            viewTransaction();
    }

    private void setupTransaction(){
        // get the amount
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

        // add the year levels
        ObservableList<String> year_levels = FXCollections.observableArrayList();
        year_levels.add("1st Year");
        year_levels.add("2nd Year");
        year_levels.add("3rd Year");
        year_levels.add("4th Year");
        transaction_payer_year.setItems(year_levels);
        transaction_payer_year.getSelectionModel().select(payer.getYear_level());

        // add the modes of payment
        ObservableList<String> modes = FXCollections.observableArrayList();
        modes.add("Cash");
        modes.add("GCash");
        modes.add("Others");
        transaction_payment_mode.setItems(modes);
        transaction_payment_mode.getSelectionModel().select(0);
        add_receipt_button.setDisable(true);

        // disable the receipt input if the mode is Cash
        transaction_payment_mode.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            // disable the input of receipt when the mode of payment is "Cash"
            add_receipt_button.setDisable(transaction_payment_mode.getValue().equals("Cash"));
            receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
        });
    }

    /**
     * For recording of transactions.
     */
    @FXML
    private void recordTransaction() {
        try {
            String update_payer_status_query = "INSERT INTO `pays` (`contribution_code`, `payer_id`, `payment_mode`, `payer_receipt`, `status`)\n" +
                    "VALUES (?, ?, ?, ?, \"Pending\");";
            PreparedStatement insert_payer_status = connect.prepareStatement(update_payer_status_query);
            insert_payer_status.setString(1, contribution_code);
            insert_payer_status.setString(2, payer.getId_number());
            insert_payer_status.setString(3, transaction_payment_mode.getValue());
            if (transaction_payment_mode.getValue().equals("Cash")){
                insert_payer_status.setNull(4, Types.NULL);
            } else {
                try {
                    File receipt_image = new File(receipt_link.getText());
                    FileInputStream receipt_image_fin = new FileInputStream(receipt_image);
                    insert_payer_status.setBinaryStream(4, receipt_image_fin, (int) receipt_image.length());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            insert_payer_status.executeUpdate();
            insert_payer_status.close();

            String transaction_id_query = "SELECT `transaction_id` FROM `pays` WHERE `contribution_code` = \"" + contribution_code
                    + "\" AND `payer_id` = \"" + payer.getId_number() + "\";";
            PreparedStatement get_transaction_id = connect.prepareStatement(transaction_id_query);
            ResultSet result_id = get_transaction_id.executeQuery();
            result_id.next();
            String transaction_id = result_id.getString("transaction_id");
            result_id.close();

            Alert success_transaction = new Alert(Alert.AlertType.INFORMATION);
            success_transaction.setTitle("Transaction Successful");
            success_transaction.setHeaderText(null);
            success_transaction.setContentText("Transaction Successful (with Transaction ID = " + transaction_id + ") for Student " + payer.getId_number() +
                    ". Please wait for verification from the BUFICOM Officers. Thank you.");
            success_transaction.showAndWait();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (semester.equals("1"))
            payer.setFirst_sem_status("Pending");
        else
            payer.setSecond_sem_status("Pending");
        //close the window
        ((Stage) transaction_scene.getScene().getWindow()).close();
    }

    private void viewTransaction(){
        try {
            String latest_transaction_query = "SELECT `id_number`, `first_name`, `middle_name`, `last_name`, `suffix_name`, "
                    + "`year_level`, `amount`, `semester`, `transaction_id`, `payment_mode`, `payer_receipt`\n"
                    + "FROM `pays` AS p LEFT JOIN `contributions` AS c ON p.`contribution_code` = c.`contribution_code`\n"
                    + "LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` "
                    + "WHERE c.`contribution_code` = \"" + contribution_code + "\" "
                    + "AND p.`payer_id` = \"" + payer.getId_number() + "\" "
                    + "ORDER BY `transaction_id` DESC;";
            PreparedStatement get_latest_transaction = connect.prepareStatement(latest_transaction_query);
            ResultSet result = get_latest_transaction.executeQuery();
            result.next();
            transaction_payer_id.setText(result.getString("id_number"));
            transaction_payer_name.setText(result.getString("last_name") + ", " + result.getString("first_name") + " "
                    + result.getString("middle_name") + " " + result.getString("suffix_name"));
            ObservableList<String> payer_year =  FXCollections.observableArrayList();
            payer_year.add(result.getString("year_level"));
            transaction_payer_year.setItems(payer_year);
            transaction_payer_year.getSelectionModel().select(0);
            transaction_amount.setText(result.getString("amount"));
            transaction_semester.setText(result.getString("semester"));
            ObservableList<String> payment_mode =  FXCollections.observableArrayList();
            payment_mode.add(result.getString("payment_mode"));
            transaction_payment_mode.setItems(payment_mode);
            transaction_payment_mode.setDisable(true);
            transaction_payment_mode.getSelectionModel().select(0);
            receipt_link.setText("Transaction #" + result.getString("transaction_id") + " Receipt");
            receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
            Blob receipt_image = result.getBlob("payer_receipt");
            receipt_link.setOnAction(e -> {
                Stage receipt_stage = new Stage();
                receipt_stage.initModality(Modality.APPLICATION_MODAL);
                try {
                    InputStream stream = receipt_image.getBinaryStream();
                    Image image = new Image(stream);
                    //Creating the image view
                    ImageView imageView = new ImageView();
                    //Setting image to the image view
                    imageView.setImage(image);
                    //Setting the image view parameters
                    imageView.setX(0);
                    imageView.setY(0);
                    imageView.setFitHeight(600);
                    imageView.setFitWidth(500);
                    imageView.setPreserveRatio(true);
                    //Setting the Scene object
                    Group root = new Group(imageView);
                    Scene scene = new Scene(root);
                    receipt_stage.setTitle("Payment Receipt.");
                    receipt_stage.setScene(scene);
                    receipt_stage.setResizable(false);
                    receipt_stage.show();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void addReceiptAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Insert Receipt Photo.");
        // open the FileChooser on the folder of the currently selected photo
        if (!receipt_link.getText().equals("No File Chosen")) {
            String path = receipt_link.getText().replace('\\', '/');
            String[] folder_paths = path.split("/");
            StringBuilder folder_path = new StringBuilder();
            for (int folder = 0; folder < folder_paths.length - 1; folder++) {
                folder_path.append(folder_paths[folder]).append("\\");
            }
            fileChooser.setInitialDirectory(new File(folder_path.toString()));
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.jpeg")
        );
        File receipt_file = fileChooser.showOpenDialog(((Stage) transaction_scene.getScene().getWindow()).getOwner());
        if (receipt_file != null)
            receipt_link.setText(receipt_file.getAbsolutePath());
    }

    @FXML
    private void receiptDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasImage() || dragboard.hasFiles()) {
            receipt_link.setText(dragboard.getFiles().get(0).getPath());
        }
        event.consume();
    }

    @FXML
    private void receiptDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        final boolean isAccepted = dragboard.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                || dragboard.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || dragboard.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");
        if ((dragboard.hasImage() || dragboard.hasFiles()) && isAccepted) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void receiptViewer() {
        if (!receipt_link.getText().equals("No File Chosen")) {
            Stage receipt_stage = new Stage();
            receipt_stage.initModality(Modality.APPLICATION_MODAL);
            try {
                InputStream stream = new FileInputStream(receipt_link.getText());
                Image image = new Image(stream);
                //Creating the image view
                ImageView imageView = new ImageView();
                //Setting image to the image view
                imageView.setImage(image);
                //Setting the image view parameters
                imageView.setX(0);
                imageView.setY(0);
                imageView.setFitHeight(600);
                imageView.setFitWidth(500);
                imageView.setPreserveRatio(true);
                //Setting the Scene object
                Group root = new Group(imageView);
                Scene scene = new Scene(root);
                receipt_stage.setTitle("Payment Receipt.");
                receipt_stage.setScene(scene);
                receipt_stage.setResizable(false);
                receipt_stage.show();
            } catch (FileNotFoundException e) {
                Alert file_not_found = new Alert(Alert.AlertType.INFORMATION);
                file_not_found.setTitle("File Not Found.");
                file_not_found.setHeaderText(null);
                file_not_found.setContentText("Please check the file if exist.");
                file_not_found.showAndWait();
            }
        }
    }

    /*
    Setters and Getters.
     */

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

    /**
     * Automatically add the information of the payer from the table.
     *
     * @param payer
     */
    public void setPayer(StudentPaymentInfo payer) {
        transaction_payer_id.setText(payer.getId_number());
        transaction_payer_name.setText(payer.getLast_name() + ", " + payer.getFirst_name() + " " + payer.getMiddle_name()
                + " " + payer.getSuffix_name());
        this.payer = payer;
    }

    public String getAcademic_year() {
        return academic_year;
    }

    public void setAcademic_year(String academic_year) {
        this.academic_year = academic_year;
    }
}
