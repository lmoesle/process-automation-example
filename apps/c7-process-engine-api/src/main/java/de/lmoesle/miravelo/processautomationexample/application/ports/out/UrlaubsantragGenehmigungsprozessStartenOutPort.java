package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

import java.util.List;

public interface UrlaubsantragGenehmigungsprozessStartenOutPort {

    ProzessinstanzId starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag, List<BenutzerId> teamLeadIds);
}
