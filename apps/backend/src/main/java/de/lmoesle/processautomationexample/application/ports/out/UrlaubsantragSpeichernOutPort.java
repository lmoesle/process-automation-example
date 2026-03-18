package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

public interface UrlaubsantragSpeichernOutPort {

    Urlaubsantrag speichere(Urlaubsantrag urlaubsantrag);
}
