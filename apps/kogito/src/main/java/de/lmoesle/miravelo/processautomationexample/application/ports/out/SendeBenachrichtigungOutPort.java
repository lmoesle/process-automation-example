package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

public interface SendeBenachrichtigungOutPort {

    void sendeBenachrichtigung(Urlaubsantrag urlaubsantrag);
}
