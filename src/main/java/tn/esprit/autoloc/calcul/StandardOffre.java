package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;

@Component("standard")   // nom du bean = beanName de VehiculeOffre
public class StandardOffre implements OffreStrategy {
    @Override
    public double calculerTotal(double tarifJournalier, int nbJours) {
        return tarifJournalier * nbJours;
    }
}
