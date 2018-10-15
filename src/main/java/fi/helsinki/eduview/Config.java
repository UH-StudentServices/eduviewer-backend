package fi.helsinki.eduview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: Hannu-Pekka Rajaniemi (h-p@iki.fi)
 * @date: 20/02/2018
 */
@Configuration
@PropertySource("classpath:edu.properties")
@EnableScheduling
public class Config {

    @Autowired protected Environment env;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(env.getProperty("spring-mail-host"));
        sender.setPort(Integer.parseInt(env.getProperty("spring-mail-port")));
        return sender;
    }
}
