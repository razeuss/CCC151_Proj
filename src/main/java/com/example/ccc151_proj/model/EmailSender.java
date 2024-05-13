package com.example.ccc151_proj.model;

import javafx.scene.control.Alert;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use to notify the payer regarding their payments.
 */
public class EmailSender {
    /**
     * Sends email to the payer
     * @param payer_email
     * @param subject_message
     * @param message_to_student
     * @param payment_receipt_file
     */
    public static void sendEmail(String payer_email, String subject_message, String message_to_student, File payment_receipt_file){
        try{
            // set the properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");

            // app email gayud ni siya, ing na rako kung mangayo mo password HAHAHAHA
            String developerEmail = "c6918434@gmail.com";
            String passwordKey = "aknh vprq xmhy ydci";

            // create a session with the mail
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(developerEmail, passwordKey);
                }
            });

            // convert the File to FileDataSource (necessary for sending email)
            FileDataSource payment_receipt = null;
            if (payment_receipt_file != null){
                payment_receipt = new FileDataSource(payment_receipt_file.getPath());
            }

            // create the message
            Message message = prepareMessage(session, developerEmail, payer_email, subject_message, message_to_student, payment_receipt);

            // alert the user so that they won't forcibly close the app
            Alert loading = new Alert(Alert.AlertType.WARNING, "The Verification Form will automatically close after sending the email.");
            loading.setHeaderText("Please Wait.");
            loading.setTitle("Sending Email");
            loading.showAndWait();

            // if the message is successfully created, send it
            if (message != null) {
                Transport.send(message);
                if (payment_receipt_file != null) {
                    payment_receipt_file.delete();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to notify the student").show();
            }
        } catch (MessagingException e){
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Prepares the message with receipt photo.
     * @param session
     * @param myEmail
     * @param recipientEmail
     * @param subject_message
     * @param message_to_student
     * @param payment_receipt
     * @return message
     */
    private static Message prepareMessage(Session session, String myEmail, String recipientEmail, String subject_message,
                                                     String message_to_student, FileDataSource payment_receipt){
        try {
            // initiate the message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject(subject_message);

            // create a multipart for the message content
            MimeMultipart multipart = new MimeMultipart();

            // this is for the text part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message_to_student, "text/html");    // use html format
            multipart.addBodyPart(messageBodyPart);

            // if there is a photo to be sent
            if (payment_receipt != null){
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(payment_receipt));
                messageBodyPart.setHeader("Content-ID", "<image>");
                multipart.addBodyPart(messageBodyPart);
            }

            // add the multipart content to the message
            message.setContent(multipart);
            return message;
        } catch (Exception e){
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
}
