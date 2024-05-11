package com.example.ccc151_proj.control;

import com.example.ccc151_proj.model.DataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.*;

public class ReviewTransactionControl {
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

    public ReviewTransactionControl(){}

    public void initialize(String payer_id_number, String contribution_code){
        connect = DataManager.getConnect();

        String[] contribution_details = contribution_code.split("_");
        transaction_label.setText(contribution_details[0] + " Transaction View");
        transaction_payer_id.setText(payer_id_number);
        getPaymentInfo(payer_id_number, contribution_code);
    }

    private void getPaymentInfo(String payer_id_number, String contribution_code){
        try {
            String payment_info_query = "SELECT `id_number`, `first_name`, `middle_name`, `last_name`, `suffix_name`, `transaction_id`, `transaction_datetime`, " +
                    "`amount`, `payment_mode`, `payer_receipt`, `status`, `transaction_message`\n" +
                    "FROM `pays` AS p LEFT JOIN `contributions` AS c ON p.`contribution_code` = c.`contribution_code`\n" +
                    "LEFT JOIN `students` AS s ON p.`payer_id` = s.`id_number` \n" +
                    "WHERE c.`contribution_code` = \"" + contribution_code + "\"\n" +
                    "AND p.`payer_id` = \"" + payer_id_number + "\"\n" +
                    "ORDER BY `transaction_id` DESC;";
            PreparedStatement get_payment_info = connect.prepareStatement(payment_info_query);
            ResultSet result = get_payment_info.executeQuery();
            result.next();
            transaction_payer_name.setText(result.getString("last_name") + ", " + result.getString("first_name") + " "
                    + result.getString("middle_name") + " " + result.getString("suffix_name"));
            transaction_id.setText(result.getString("transaction_id"));
            transaction_datetime.setText(result.getTimestamp("transaction_datetime").toString());
            transaction_amount.setText(result.getString("amount"));
            ObservableList<String> payment_mode =  FXCollections.observableArrayList();
            payment_mode.add(result.getString("payment_mode"));
            transaction_payment_mode.setItems(payment_mode);
            transaction_payment_mode.getSelectionModel().select(0);
            if (transaction_payment_mode.getValue().equals("Cash"))
                receipt_link.setText("No Receipt.");
            else
                receipt_link.setText("Transaction #" +  result.getString("transaction_id") + " Receipt.");
            receipt_link.setDisable(transaction_payment_mode.getValue().equals("Cash"));
            Blob receipt_image = result.getBlob("payer_receipt");
            receipt_link.setOnAction(e -> {
                viewReceipt(receipt_image);
            });
            transaction_status.setText(result.getString("status"));
            String transaction_message = result.getString("transaction_message");
            if (!result.wasNull())
                transaction_comments.setText(transaction_message);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    @FXML
    private void close_button_clicked(ActionEvent event){
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
