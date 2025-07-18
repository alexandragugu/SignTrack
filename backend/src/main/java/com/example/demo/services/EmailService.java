package com.example.demo.services;

import com.example.demo.utils.FileActionType;
import com.fasterxml.jackson.core.JsonFactory;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.oer.Switch;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.demo.utils.FileActionType.TO_SIGN;


@Service
public class EmailService {

    private JavaMailSender mailSender;

    private String NotificationSubject="Notification";

    private String templateAdminPath="/templates/admin.html";

    private String templatePath="templates/emailTemplate.html";

    private String stylesPath="templates/emailStyle.css";

    @Value("${spring.mail.username}")
    private String emailAddress;

    @Value("${spring.frontend.url}")
    private String frontendUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    private TokenService tokenService;


    private String generateText(String from, FileActionType action, String filename){
        String text = "";
        String userHtml = "<strong style='color:#ffffff'>" + from + "</strong>";
        String fileHtml = "<span style='color:#ffffff; font-weight: bold;'>" + filename + "</span>";
        String end = "<br/><span style='color:#ffffff;'>Please login to the platform to see the file status:</span>";


        switch (action) {
            case TO_APPROVE -> text = "User " + userHtml + " just requested your approval for the file: " + fileHtml + "." + end;
            case APPROVED   -> text = "User " + userHtml + " just approved your file: " + fileHtml + "." + end;
            case SIGNED     -> text = "User " + userHtml + " just signed your file: " + fileHtml + "." + end;
            case TO_SIGN    -> text = "User " + userHtml + " just requested your signature for the file: " + fileHtml + "." + end;
            case DECLINED   -> text = "User " + userHtml + " just declined your file: " + fileHtml + "." + end;
            case TO_VIEW    -> text = "User " + userHtml + " just requested your view for the file: " + fileHtml + "." + end;
            case VIEWED     -> text = "User " + userHtml + " just viewed your file: " + fileHtml + "." + end;
            case ADMIN_DELETE -> text = "The flow related to your file " + fileHtml + " has been <strong style='color:#ffffff'>deleted by an administrator</strong>.";
        }
        return text;
    }



    @Async
    public void sendEmailNotification(String to, String from, String filename, FileActionType fileActionType, UUID fileId, UUID receiverId){
        try{
            String htmlTemplate= loadTemplate(templatePath);
            String cssStyles=loadTemplate(stylesPath);
            String messageContent=generateText(from, fileActionType, filename);

            String token=tokenService.generateFileToken(fileId.toString(),receiverId.toString(),to, fileActionType);
            String secureLink=generateLinkUrl(fileActionType)+"?token="+token;

            String emailContent = htmlTemplate
                    .replace("{{messageContent}}", messageContent)
                    .replace("{{secureLink}}", secureLink)
                    .replace("<link rel=\"stylesheet\" href=\"emailStyle.css\">", "<style>" + cssStyles + "</style>");

            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper= new MimeMessageHelper(message,"utf-8");
            helper.setTo(to);
            helper.setSubject(NotificationSubject);
            helper.setFrom(emailAddress);

            helper.setText(emailContent,true);

            mailSender.send(message);

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Eroare la trimiterea email-ului");
        }

    }

    @Async
    public void sendAdminNotification(String to, String from, String filename, FileActionType fileActionType, UUID fileId, UUID receiverId){
        try{
            String htmlTemplate= loadTemplate(templateAdminPath);
            String cssStyles=loadTemplate(stylesPath);
            String messageContent=generateText(from, fileActionType, filename);


            String emailContent = htmlTemplate
                    .replace("{{messageContent}}", messageContent)
                    .replace("<link rel=\"stylesheet\" href=\"emailStyle.css\">", "<style>" + cssStyles + "</style>");

            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper= new MimeMessageHelper(message,"utf-8");
            helper.setTo(to);
            helper.setSubject(NotificationSubject);
            helper.setFrom(emailAddress);

            helper.setText(emailContent,true);

            mailSender.send(message);

        }catch (Exception e){
            throw new RuntimeException("Eroare la trimiterea email-ului");
        }

    }

    private String generateLinkUrl(FileActionType fileActionType){
        String url="";
        switch(fileActionType){
            case TO_APPROVE -> {
                url=frontendUrl+"/signRequest";
            }
            case TO_VIEW -> {
                url=frontendUrl+"/signRequest";
            }
            case TO_SIGN -> {
                url=frontendUrl+"/signRequest";
            }
            case SIGNED -> {
                url=frontendUrl+"/fileStatus";
            }
            case APPROVED -> {
                url=frontendUrl+"/fileStatus";
            }
            case DECLINED -> {
                url=frontendUrl+"/fileStatus";
            }

            case VIEWED -> {
                url=frontendUrl+"/fileStatus";
            }
        }
        return url;
    }

     String loadTemplate(String path){
        try{
            InputStream inputStream= new ClassPathResource(path).getInputStream();
            try(BufferedReader reader= new BufferedReader((new InputStreamReader(inputStream, StandardCharsets.UTF_8)))){
                return reader.lines().collect(Collectors.joining("\n"));
            }catch (Exception e){
                throw new RuntimeException("Eroare la citirea template-ului");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
