package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;

public interface UrlaubsantragProzessinstanzSpeichernOutPort {

    void speichereProzessinstanzId(UrlaubsantragId urlaubsantragId, ProzessinstanzId prozessinstanzId);
}
