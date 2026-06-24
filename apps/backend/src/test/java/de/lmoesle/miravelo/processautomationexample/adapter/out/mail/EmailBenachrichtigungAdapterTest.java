package de.lmoesle.miravelo.processautomationexample.adapter.out.mail;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.tasklist.UserTaskTestdaten;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EmailBenachrichtigungAdapterTest {

    private JavaMailSender javaMailSender;
    private MimeMessage mimeMessage;
    private EmailBenachrichtigungAdapter emailBenachrichtigungAdapter;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        final EmailBenachrichtigungProperties properties = new EmailBenachrichtigungProperties();
        properties.setFromAddress("no-reply@miravelo.local");
        properties.setStandardTemplate(new ClassPathResource("mail/standard-benachrichtigung.mail.html"));
        properties.setFrontendBaseUrl("http://localhost:3000");
        emailBenachrichtigungAdapter = new EmailBenachrichtigungAdapter(javaMailSender, properties);
    }

    @Test
    void sendsApprovalEmailUsingStandardHtmlTemplate() throws Exception {
        final Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantragWithStartedProcess();
        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(true);
        urlaubsantrag.genehmigeDurchVorgesetzten("Vertretung ist organisiert.");

        emailBenachrichtigungAdapter.sendeBenachrichtigung(urlaubsantrag);

        verify(javaMailSender).send(mimeMessage);
        mimeMessage.saveChanges();

        assertThat(mimeMessage.getFrom()).hasSize(1);
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("no-reply@miravelo.local");
        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo(UrlaubsantragTestData.antragsteller().email());
        assertThat(mimeMessage.getSubject()).isEqualTo("Urlaubsantrag genehmigt");
        assertThat(mimeMessage.getContentType()).contains("text/html");
        assertThat(mimeMessage.getContent()).isInstanceOf(String.class);
        final String html = (String) mimeMessage.getContent();
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("Urlaubsantrag genehmigt");
        assertThat(html).contains("Hallo " + UrlaubsantragTestData.antragsteller().name() + ",");
        assertThat(html).contains("dein Urlaubsantrag fuer den Zeitraum vom 01.07.2026 bis 10.07.2026 wurde genehmigt.");
        assertThat(html).contains("Vertretung ist organisiert.");
        assertThat(html).contains("href=\"http://localhost:3000\"");
        assertThat(html).contains(">Genehmigten Antrag im Frontend ansehen<");
    }

    @Test
    void sendsRejectionEmailUsingStandardHtmlTemplate() throws Exception {
        final Urlaubsantrag urlaubsantrag = UrlaubsantragTestData.urlaubsantrag();
        urlaubsantrag.starteAutomatischePruefung();
        urlaubsantrag.schliesseAutomatischePruefungAb(false);

        emailBenachrichtigungAdapter.sendeBenachrichtigung(urlaubsantrag);

        verify(javaMailSender).send(mimeMessage);
        mimeMessage.saveChanges();

        assertThat(mimeMessage.getSubject()).isEqualTo("Urlaubsantrag abgelehnt");
        assertThat(mimeMessage.getContentType()).contains("text/html");
        assertThat(mimeMessage.getContent()).isInstanceOf(String.class);
        final String html = (String) mimeMessage.getContent();
        assertThat(html).contains("Urlaubsantrag abgelehnt");
        assertThat(html).contains("dein Urlaubsantrag fuer den Zeitraum vom 01.07.2026 bis 10.07.2026 wurde abgelehnt.");
        assertThat(html).contains("Es wurde kein zusaetzlicher Kommentar hinterlegt.");
        assertThat(html).contains("href=\"http://localhost:3000\"");
        assertThat(html).contains(">Abgelehnten Antrag im Frontend ansehen<");
    }

    @Test
    void sendsTaskNotificationUsingStandardHtmlTemplate() throws Exception {
        emailBenachrichtigungAdapter.sendeBenutzeraufgabenBenachrichtigung(
            UserTaskTestdaten.userTask(),
            java.util.List.of(BenutzerTestdaten.carla())
        );

        verify(javaMailSender).send(mimeMessage);
        mimeMessage.saveChanges();

        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo(BenutzerTestdaten.carla().email());
        assertThat(mimeMessage.getSubject()).isEqualTo("Neue Aufgabe bereit");
        assertThat(mimeMessage.getContentType()).contains("text/html");
        assertThat(mimeMessage.getContent()).isInstanceOf(String.class);
        final String html = (String) mimeMessage.getContent();
        assertThat(html).contains("Neue Aufgabe bereit");
        assertThat(html).contains("Hallo " + BenutzerTestdaten.carla().name() + ",");
        assertThat(html).contains("Es liegt fuer Sie eine neue Aufgabe bereit.");
        assertThat(html).contains("Aufgaben-ID");
        assertThat(html).contains(UserTaskTestdaten.TASK_ID);
        assertThat(html).contains("href=\"http://localhost:3000\"");
        assertThat(html).contains(">Aufgabe im Frontend oeffnen<");
    }

    @Test
    void rejectsUnsupportedStatusForNotificationEmail() {
        assertThatThrownBy(() -> emailBenachrichtigungAdapter.sendeBenachrichtigung(UrlaubsantragTestData.urlaubsantrag()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Es gibt kein Email-Template fuer den Status ANTRAG_GESTELLT");

        verifyNoInteractions(javaMailSender);
    }
}
