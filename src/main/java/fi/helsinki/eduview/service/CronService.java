package fi.helsinki.eduview.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author: hpr
 * @date: 12/10/2018
 */
@Service
public class CronService {

    private static Logger logger = LogManager.getLogger(CronService.class);

    @Autowired private EmailService emailService;

    @Scheduled(cron = "${nightly-report-schedule}")
    public void emailTask() {
        emailService.generateNightlyReportMail();
    }

}
