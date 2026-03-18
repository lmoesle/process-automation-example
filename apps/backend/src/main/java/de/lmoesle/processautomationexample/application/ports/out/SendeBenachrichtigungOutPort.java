package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

public interface SendeBenachrichtigungOutPort {

    void sendeBenachrichtigung(Urlaubsantrag urlaubsantrag);
}
