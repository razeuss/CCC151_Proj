package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.DataManager;
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

import java.io.InputStream;
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
                Blob receipt_image = result.getBlob("payer_receipt");
                if (transaction_payment_mode.getValue().equals("Cash"))
                    receipt_link.setText("No Receipt.");
                else
                    receipt_link.setText("Transaction #" + payment.getTransaction_id() + " Receipt.");
                receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
                receipt_link.setOnAction(e -> {
                    viewReceipt(receipt_image);
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
     * @param receipt_image
     */
    private void viewReceipt(Blob receipt_image){
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
                String accept_payment_query = "UPDATE `pays` SET `status` = \"Accepted\", `transaction_message` = ? " +
                        "WHERE `transaction_id` = ?;";
                PreparedStatement update_payment = connect.prepareStatement(accept_payment_query);
                update_payment.setString(1, transaction_comments.getText());
                update_payment.setLong(2, payment.getTransaction_id());
                update_payment.executeUpdate();

                Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                confirmation.setTitle("Confirmation Dialog");
                confirmation.setHeaderText(null);
                confirmation.setContentText("Payment (Transaction ID = " + payment.getTransaction_id() + ") is accepted.");
                confirmation.showAndWait();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            alert.close();
        }

        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();    // close the frame
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
                    String accept_payment_query = "UPDATE `pays` SET `status` = \"Rejected\", `transaction_message` = ? " +
                            "WHERE `transaction_id` = ?;";
                    PreparedStatement update_payment = connect.prepareStatement(accept_payment_query);
                    update_payment.setString(1, transaction_comments.getText());
                    update_payment.setLong(2, payment.getTransaction_id());
                    update_payment.executeUpdate();

                    Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                    confirmation.setTitle("Confirmation Dialog");
                    confirmation.setHeaderText(null);
                    confirmation.setContentText("Payment (Transaction ID = " + payment.getTransaction_id() + ") is rejected.");
                    confirmation.showAndWait();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                alert.close();
            }
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();    // close the frame
        } else {
            // reason/s for rejection is required.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Rejection Comment");
            alert.setHeaderText(null);
            alert.setContentText("Please provide reason.");
            alert.showAndWait();
        }
    }
}
