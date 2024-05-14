package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.DataManager;
import com.example.ccc151_proj.model.EmailSender;
import com.example.ccc151_proj.model.UnverifiedPayment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.activation.FileDataSource;
import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Optional;

/**
 * Facilitates the verification form.
 */
public class VerifyTransactionControl {
    private static Connection connect;
    @FXML
    private TextField transaction_label;
    @FXML
    private TextField transaction_payer_id;
    @FXML
    private TextField transaction_payer_name;
    @FXML
    private TextField transaction_id;
    @FXML
    private TextField transaction_datetime;
    @FXML
    private TextField transaction_amount;
    @FXML
    private ComboBox<String> transaction_payment_mode;
    @FXML
    private Hyperlink receipt_link;
    @FXML
    private TextField transaction_status;
    @FXML
    private TextArea transaction_comments;
    private UnverifiedPayment payment;
    private Blob receipt_image;

    public VerifyTransactionControl(){}

    /**
     * Initialize the necessary data and connection for the transaction.
     * @param payment
     */
    public void initialize(UnverifiedPayment payment){
        connect = DataManager.getConnect();
        this.payment = payment;
        setupData();
    }

    /**
     * Set up the data of the transaction.
     */
    private void setupData(){
        try {
            String payment_info_query = "SELECT p.`contribution_code`, `transaction_datetime`, `amount`, `payment_mode`, `payer_receipt`, `transaction_message`\n" +
                    "FROM `pays` AS p LEFT JOIN `contributions` AS c ON p.`contribution_code` = c.`contribution_code`\n" +
                    "WHERE p.`transaction_id` =" + payment.getTransaction_id() + ";";
            PreparedStatement get_payment_info = connect.prepareStatement(payment_info_query);
            ResultSet result = get_payment_info.executeQuery();
            if (result.next()){
                transaction_label.setText(result.getString("contribution_code") + " Contribution");
                transaction_payer_id.setText(payment.getId_number());
                transaction_payer_name.setText(payment.getLast_name() + ", " + payment.getFirst_name() + " " + payment.getMiddle_name() + " " + payment.getSuffix_name());
                transaction_id.setText(String.valueOf(payment.getTransaction_id()));
                transaction_datetime.setText(result.getTimestamp("transaction_datetime").toString());
                transaction_amount.setText(String.valueOf(result.getInt("amount")));
                ObservableList<String> payment_mode =  FXCollections.observableArrayList();
                payment_mode.add(result.getString("payment_mode"));
                transaction_payment_mode.setItems(payment_mode);
                transaction_payment_mode.getSelectionModel().selectFirst();
                receipt_image = result.getBlob("payer_receipt");
                if (transaction_payment_mode.getValue().equals("Cash"))
                    receipt_link.setText("No Receipt.");
                else
                    receipt_link.setText("Transaction #" + payment.getTransaction_id() + " Receipt.");
                receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
                receipt_link.setOnAction(e -> {
                    viewReceipt();
                });
                transaction_status.setText(payment.getStatus());
                String transaction_message = result.getString("transaction_message");
                if (!result.wasNull())
                    transaction_comments.setText(transaction_message);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set up the frame that will display the receipt of the payment.
     *
     */
    private void viewReceipt(){
        Stage receipt_stage = new Stage();
        receipt_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
        receipt_stage.setResizable(false);
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
            receipt_stage.show();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Facilitates the transaction when accepted.
     * @param event
     */
    @FXML
    private void accept_button_clicked(ActionEvent event){
        // ask for confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Is everything good to accept?");
        alert.setContentText("Finalize the payment?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            try {
                // if confirmed
                String accept_payment_query = "UPDATE `pays` SET `status` = 'Accepted', `transaction_message` = ? " +
                        "WHERE `transaction_id` = ?;";
                PreparedStatement update_payment = connect.prepareStatement(accept_payment_query);
                update_payment.setString(1, transaction_comments.getText());
                update_payment.setLong(2, payment.getTransaction_id());
                update_payment.executeUpdate();
                update_payment.close();

                Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                confirmation.setTitle("Confirmation Dialog");
                confirmation.setHeaderText(null);
                confirmation.setContentText("Payment (Transaction ID = " + payment.getTransaction_id() + ") is accepted.");
                confirmation.showAndWait();

                // notify the payer via email
                notifyPayer("Thank you for paying.", "Accepted");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();    // close the frame
        } else {
            alert.close();
        }
    }

    /**
     * Facilitates the transaction when rejected.
     * @param event
     */
    @FXML
    private void reject_button_clicked(ActionEvent event){
        if (!transaction_comments.getText().isEmpty()){
            // ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Is everything good to reject?");
            alert.setContentText("Finalize the payment?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                try {
                    // if confirmed
                    String accept_payment_query = "UPDATE `pays` SET `status` = 'Rejected', `transaction_message` = ? " +
                            "WHERE `transaction_id` = ?;";
                    PreparedStatement update_payment = connect.prepareStatement(accept_payment_query);
                    update_payment.setString(1, transaction_comments.getText());
                    update_payment.setLong(2, payment.getTransaction_id());
                    update_payment.executeUpdate();
                    update_payment.close();

                    Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                    confirmation.setTitle("Confirmation Dialog");
                    confirmation.setHeaderText(null);
                    confirmation.setContentText("Payment (Transaction ID = " + payment.getTransaction_id() + ") is rejected.");
                    confirmation.showAndWait();

                    // notify the payer via email
                    notifyPayer("Please contact your Classroom Representative.", "Rejected");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();    // close the frame
            } else {
                alert.close();
            }
        } else {
            // reason/s for rejection is required.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Rejection Comment");
            alert.setHeaderText(null);
            alert.setContentText("Please provide reason.");
            alert.showAndWait();
        }
    }

    /**
     * Notify the payer via Email about the result of the transaction.
     * @param end_message
     * @param status
     */
    private void notifyPayer(String end_message, String status){
        try {
            // get the email address of the payer
            String payer_email_query = "SELECT `email` FROM `students` WHERE `id_number` = '" + transaction_payer_id.getText() + "';";
            PreparedStatement get_payer_email = connect.prepareStatement(payer_email_query);
            ResultSet result = get_payer_email.executeQuery();
            result.next();
            String payer_email = result.getString("email");

            // create the subject of the message
            String[] contribution_code_data = transaction_label.getText().replace(" Contribution", "").split("_");
            String subject_message = "Payment for " + contribution_code_data[0] + " A.Y. " + contribution_code_data[1] + ", Semester " + contribution_code_data[2];

            // create the body of the message, along with the receipt photo if available
            String receipt_id = " null ";
            File file = null;
            // since receipt photo is only available when payment mode isn't Cash
            if (!transaction_payment_mode.getValue().equals("Cash")){
                receipt_id = "<img src=\"cid:image\">";     // html format for the image insertion
                // create a temporary file where the receipt will be stored
                file = File.createTempFile("temp_receipt", ".png");
                // output the Blob from the database to the temporary file
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(receipt_image.getBinaryStream().readAllBytes());
            }
            String message_to_student = "<h1>Transaction Report </h1><br>" +
                    "<b>Transaction ID </b> = " + transaction_id.getText() + "<br>" +
                    "<b>Student ID </b> = " + transaction_payer_id.getText() + "<br>" +
                    "<b>Student Name </b> = " + transaction_payer_name.getText() + "<br>" +
                    "<b>Amount </b> = " + transaction_amount.getText() + "<br>" +
                    "<b>Payment Mode </b> = " + transaction_payment_mode.getValue() + "<br>" +
                    "<b>Receipt </b> = " + receipt_id + "<br>" +
                    "<b>Status </b> = " + status + "<br>" +
                    "<b>Comments </b> = " + transaction_comments.getText() + "<br><br><br>" +
                    "<b><i>Remarks </i></b> :" + end_message + "<br>" +
                    "-- " + contribution_code_data[0] + " BUFICOM :>";

            EmailSender.sendEmail(payer_email, subject_message, message_to_student, file);
        } catch (IOException | SQLException e){
            throw new RuntimeException(e);
        }
    }
}
