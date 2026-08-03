package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

public interface UrlaubsantragSpeichernOutPort {

    Urlaubsantrag speichere(Urlaubsantrag urlaubsantrag);
}
