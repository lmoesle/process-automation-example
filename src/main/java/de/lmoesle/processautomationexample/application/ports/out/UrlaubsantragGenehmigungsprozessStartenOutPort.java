package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.Urlaubsantrag;

public interface UrlaubsantragGenehmigungsprozessStartenOutPort {

    ProzessinstanzId starteGenehmigungsprozessFuer(Urlaubsantrag urlaubsantrag);
}
