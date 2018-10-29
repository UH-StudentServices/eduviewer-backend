/*
 * This file is part of Eduviewer application.
 *
 * Eduviewer application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eduviewer application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Eduviewer application.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.helsinki.eduview.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
            for (String lv : lvs.split(",")) {
                message += lvNames.get(lv.trim()) + "\r\n\r\n" + studyStructureService.dataCheck(lv) + "\r\n\r\n";
            }
        } catch (Exception e) {
            message = "Error while generating report: " + e.getMessage();
        } finally {
            try {
                MimeMessage mail = javaMailSender.createMimeMessage();
                mail.setFrom(new InternetAddress(env.getProperty("nightly-report-from", "poliisimestari-sisu@helsinki.fi")));
                mail.addRecipient(Message.RecipientType.TO, new InternetAddress(env.getProperty("nightly-report-to")));
                mail.setText(message);
                mail.setSubject(subject);
                javaMailSender.send(mail);
            } catch (Exception e) {
                logger.error("error creating nightly email", e);
            }
        }

    }
}
