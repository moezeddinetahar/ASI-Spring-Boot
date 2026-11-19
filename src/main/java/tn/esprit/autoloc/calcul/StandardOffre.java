package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;

@Component("STANDARD")   // nom du bean = nom de la valeur de l'énumération
public class StandardOffre implements OffreStrategy {
    @Override
    public double calculerTotal(double tarifJournalier, int nbJours) {
        return tarifJournalier * nbJours;
    }
}
