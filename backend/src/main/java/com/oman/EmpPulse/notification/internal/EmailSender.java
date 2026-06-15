package com.oman.EmpPulse.notification.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

  private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

  private final ObjectProvider<JavaMailSender> mailSenderProvider;
  private final String fromAddress;
  private final boolean mailEnabled;

  public EmailSender(
      ObjectProvider<JavaMailSender> mailSenderProvider,
      @Value("${app.mail.from}") String fromAddress,
      @Value("${app.mail.enabled}") boolean mailEnabled) {
    this.mailSenderProvider = mailSenderProvider;
    this.fromAddress = fromAddress;
    this.mailEnabled = mailEnabled;
  }

  public void send(String to, String subject, String body) {
    if (!mailEnabled) {
      log.info("Mail disabled; would send email to={} subject={} body={}", to, subject, body);
      return;
    }

    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
    if (mailSender == null) {
      log.warn(
          "JavaMailSender not available; would send email to={} subject={} body={}",
          to,
          subject,
          body);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
    log.debug("Sent email to={} subject={}", to, subject);
  }
}
