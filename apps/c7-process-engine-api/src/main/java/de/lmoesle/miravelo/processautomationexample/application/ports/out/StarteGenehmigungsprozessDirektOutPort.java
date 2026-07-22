package de.lmoesle.miravelo.processautomationexample.application.ports.out;

import de.lmoesle.miravelo.processautomationexample.domain.benutzer.BenutzerId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.miravelo.processautomationexample.domain.urlaubsantrag.UrlaubsantragId;

import java.util.List;

public interface StarteGenehmigungsprozessDirektOutPort {

    ProzessinstanzId starteGenehmigungsprozess(UrlaubsantragId urlaubsantragId, List<BenutzerId> teamLeadIds);
}
