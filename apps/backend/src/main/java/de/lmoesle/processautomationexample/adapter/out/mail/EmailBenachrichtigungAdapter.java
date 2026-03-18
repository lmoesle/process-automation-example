package de.lmoesle.processautomationexample.adapter.out.mail;

import de.lmoesle.processautomationexample.application.ports.out.SendeBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.application.ports.out.SendeBenutzeraufgabenBenachrichtigungOutPort;
import de.lmoesle.processautomationexample.domain.benutzer.Benutzer;
import de.lmoesle.processautomationexample.domain.tasklist.UserTask;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailBenachrichtigungAdapter implements SendeBenachrichtigungOutPort, SendeBenutzeraufgabenBenachrichtigungOutPort {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final JavaMailSender javaMailSender;
    private final EmailBenachrichtigungProperties properties;

    @Override
    public void sendeBenachrichtigung(Urlaubsantrag urlaubsantrag) {
        Assert.notNull(urlaubsantrag, "urlaubsantrag darf nicht null sein");

        EmailInhalt emailInhalt = emailInhaltFuer(urlaubsantrag);
        sendeEmail(List.of(urlaubsantrag.antragsteller().email()), "Urlaubsantrag " + urlaubsantrag.id().value(), emailInhalt);
    }

    @Override
    public void sendeBenutzeraufgabenBenachrichtigung(UserTask userTask, List<Benutzer> empfaenger) {
        Assert.notNull(userTask, "userTask darf nicht null sein");
        Assert.notNull(empfaenger, "empfaenger darf nicht null sein");

        empfaenger.forEach(benutzer -> {
            EmailInhalt emailInhalt = new EmailInhalt(
                "Neue Aufgabe bereit",
                "Neue Aufgabe bereit",
                "Hallo %s,".formatted(benutzer.name()),
                "Es liegt fuer Sie eine neue Aufgabe bereit.",
                "Aufgaben-ID",
                userTask.id().value(),
                "Aufgabe im Frontend oeffnen",
                properties.getFrontendBaseUrl()
            );
            sendeEmail(List.of(benutzer.email()), "Aufgabe " + userTask.id().value(), emailInhalt);
        });
    }

    private void sendeEmail(List<String> empfaengerEmail, String referenz, EmailInhalt emailInhalt) {
        String template = ladeTemplate(properties.getStandardTemplate());
        String renderteEmail = render(template, emailInhalt);
        var mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getFromAddress());
            helper.setTo(empfaengerEmail.toArray(String[]::new));
            helper.setSubject(emailInhalt.betreff());
            helper.setText(renderteEmail, true);
        } catch (MessagingException exception) {
            throw new IllegalStateException(
                "Email fuer " + referenz + " konnte nicht erstellt werden.",
                exception
            );
        }
        javaMailSender.send(mimeMessage);
    }

    private EmailInhalt emailInhaltFuer(Urlaubsantrag urlaubsantrag) {
        Assert.notNull(urlaubsantrag.status(), "status darf nicht null sein");
        String zeitraum = "dein Urlaubsantrag fuer den Zeitraum vom %s bis %s".formatted(
            urlaubsantrag.zeitraum().von().format(DATE_FORMATTER),
            urlaubsantrag.zeitraum().bis().format(DATE_FORMATTER)
        );
        String hinweisZurEntscheidung = kommentarZumAktuellenStatus(urlaubsantrag);
        return switch (urlaubsantrag.status()) {
            case GENEHMIGT -> new EmailInhalt(
                "Urlaubsantrag genehmigt",
                "Urlaubsantrag genehmigt",
                "Hallo %s,".formatted(urlaubsantrag.antragsteller().name()),
                zeitraum + " wurde genehmigt.",
                "Hinweis zur Entscheidung",
                hinweisZurEntscheidung,
                "Genehmigten Antrag im Frontend ansehen",
                properties.getFrontendBaseUrl()
            );
            case ABGELEHNT -> new EmailInhalt(
                "Urlaubsantrag abgelehnt",
                "Urlaubsantrag abgelehnt",
                "Hallo %s,".formatted(urlaubsantrag.antragsteller().name()),
                zeitraum + " wurde abgelehnt.",
                "Hinweis zur Entscheidung",
                hinweisZurEntscheidung,
                "Abgelehnten Antrag im Frontend ansehen",
                properties.getFrontendBaseUrl()
            );
            default -> throw new IllegalArgumentException(
                "Es gibt kein Email-Template fuer den Status " + urlaubsantrag.status()
            );
        };
    }

    private String ladeTemplate(Resource resource) {
        try (var inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Email-Template konnte nicht geladen werden: " + resource.getDescription(),
                exception
            );
        }
    }

    private String kommentarZumAktuellenStatus(Urlaubsantrag urlaubsantrag) {
        String entscheidungskommentar = urlaubsantrag.statusHistorie().getLast().kommentar();
        return entscheidungskommentar == null || entscheidungskommentar.isBlank()
            ? "Es wurde kein zusaetzlicher Kommentar hinterlegt."
            : entscheidungskommentar;
    }

    private String render(String template, EmailInhalt emailInhalt) {
        return template
            .replace("{{title}}", escapeHtml(emailInhalt.titel()))
            .replace("{{greeting}}", escapeHtml(emailInhalt.anrede()))
            .replace("{{message}}", escapeHtml(emailInhalt.nachricht()))
            .replace("{{detailsLabel}}", escapeHtml(emailInhalt.detailsLabel()))
            .replace("{{detailsText}}", formatHtmlText(emailInhalt.detailsText()))
            .replace("{{actionLabel}}", escapeHtml(emailInhalt.actionLabel()))
            .replace("{{actionUrl}}", escapeHtml(emailInhalt.actionUrl()));
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value, StandardCharsets.UTF_8.name());
    }

    private String formatHtmlText(String value) {
        return escapeHtml(value).replace("\n", "<br/>");
    }

    private record EmailInhalt(
        String betreff,
        String titel,
        String anrede,
        String nachricht,
        String detailsLabel,
        String detailsText,
        String actionLabel,
        String actionUrl
    ) {
    }
}
