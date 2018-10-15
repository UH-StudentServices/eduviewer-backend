package fi.helsinki.eduview.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author: hpr
 * @date: 12/10/2018
 */
@Service
public class EmailService {

    private Logger logger = LogManager.getLogger(EmailService.class);

    @Autowired private StudyStructureService studyStructureService;
    @Autowired protected Environment env;
    @Autowired private JavaMailSender javaMailSender;

    public void generateNightlyReportMail() {
        logger.info("generating nightly report");
        Map<String, String> lvNames = studyStructureService.generateLvMap();
        String lvs = env.getProperty("nightly-report-lvs");
        String message = "";
        String subject = "Eduviewer: Yöajo lukuvuosille " + Arrays.stream(lvs.split(",")).map(lvNames::get).collect(Collectors.joining(", "));
        try {
            for(String lv : lvs.split(",")) {
                message += lvNames.get(lv.trim()) + "\r\n\r\n" + studyStructureService.dataCheck(lv) + "\r\n\r\n";
            }
        } catch(Exception e) {
            message = "Error while generating report: " + e.getMessage();
        } finally {
            try {
                MimeMessage mail = javaMailSender.createMimeMessage();
                mail.setFrom(new InternetAddress(env.getProperty("nightly-report-from", "poliisimestari-sisu@helsinki.fi")));
                mail.addRecipient(Message.RecipientType.TO, new InternetAddress(env.getProperty("nightly-report-to")));
                mail.setText(message);
                mail.setSubject(subject);
                javaMailSender.send(mail);
            } catch(Exception e) {
                logger.error("error creating nightly email", e);
            }
        }

    }
}
