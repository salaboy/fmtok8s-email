package com.salaboy.conferences.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaboy.conferences.email.model.Proposal;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SpringBootApplication
@RestController
@EnableZeebeClient
@Slf4j
public class EmailService {

    public static void main(String[] args) {
        SpringApplication.run(EmailService.class, args);
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${version:0.0.0}")
    private String version;

    @Value("${EXTERNAL_URL:http://fmtok8s-api-gateway-jx-staging.35.222.17.41.nip.io}")
    private String externalURL;

    @GetMapping("/info")
    public String infoWithVersion() {
        return "{ \"name\" : \"Email Service\", \"version\" : \"v" + version + "++\", \"source\": \"https://github.com/salaboy/fmtok8s-email/releases/tag/v" + version + "\" }";
    }

    @PostMapping("/")
    public void sendEmail(@RequestBody Map<String, String> email) {
        String toEmail = email.get("toEmail");
        String emailTitle = email.get("title");
        String emailContent = email.get("content");
        log.info("+-------------------------------------------------------------------+");
        printEmail(toEmail, emailTitle, emailContent);
        log.info("+-------------------------------------------------------------------+\n\n");
    }


    @PostMapping("/notification")
    public void sendEmailNotification(@RequestBody Proposal proposal) {
        sendEmailNotificationWithLink(proposal, false);
    }

    private void sendEmailNotificationWithLink(Proposal proposal, boolean withLink) {
        String emailBody = "Dear " + proposal.getAuthor() + ", \n";
        String emailTitle = "Conference Committee Communication";
        emailBody += "\t\t We are";
        if (proposal.isApproved()) {
            emailBody += " happy ";
        } else {
            emailBody += " sorry ";
        }
        emailBody += "to inform you that: \n";
        emailBody += "\t\t\t `" + proposal.getTitle() + "` -> `" + proposal.getDescription() + "`, \n";
        emailBody += "\t\t was";
        if (proposal.isApproved()) {
            emailBody += " approved ";
        } else {
            emailBody += " rejected ";
        }
        emailBody += "for this conference.";
        printProposalEmail(proposal, emailTitle, emailBody, withLink);
    }

    private void sendEmailToCommittee(Proposal proposal) {
        String emailTitle = "Conference Committee Please Review Proposal";
        String emailBody = "Dear Committee Member, \n" +
                "\t\t please review and accept or reject the following proposal \n";
        emailBody += "\t From Author: " + proposal.getAuthor() + "\n";
        emailBody += "\t With Id: " + proposal.getId() + "\n";
        emailBody += "\t Notification Sent at: " + new Date() + "\n";
        printEmail("committee@conference.org", emailTitle, emailBody);
    }

    private void printEmail(String toEmail, String title, String body) {
        log.info("\t Email Sent to: " + toEmail);
        log.info("\t Email Title: " + title);
        log.info("\t Email Body: " + body);
    }

    private void printProposalEmail(Proposal proposal, String title, String body, boolean withLink) {
        log.info("+-------------------------------------------------------------------+");
        printEmail(proposal.getEmail(), title, body);
        if (withLink) {
            log.info("\t Please CURL the following link to confirm \n" +
                    "\t\t that you are committing to speak in our conference: \n" +
                    "\t\t curl -X POST " + externalURL + "/speakers/" + proposal.getId());
        }
        log.info("+-------------------------------------------------------------------+\n\n");
    }

    @ZeebeWorker(name = "email-worker", type = "email")
    public void sendEmailNotification(final JobClient client, final ActivatedJob job) {
        Proposal proposal = objectMapper.convertValue(job.getVariablesAsMap().get("proposal"), Proposal.class);
        sendEmailNotification(proposal);
        client.newCompleteCommand(job.getKey()).send();
    }

    @ZeebeWorker(name = "email-worker", type = "email-with-link")
    public void sendEmailNotificationWithLink(final JobClient client, final ActivatedJob job) {
        Proposal proposal = objectMapper.convertValue(job.getVariablesAsMap().get("proposal"), Proposal.class);
        sendEmailNotificationWithLink(proposal, true);
        client.newCompleteCommand(job.getKey()).send();
    }

    @ZeebeWorker(name = "email-worker", type = "email-to-committee")
    public void sendEmailCommittee(final JobClient client, final ActivatedJob job) {
        Proposal proposal = objectMapper.convertValue(job.getVariablesAsMap().get("proposal"), Proposal.class);
        sendEmailToCommittee(proposal);
        client.newCompleteCommand(job.getKey()).send();
    }

}
