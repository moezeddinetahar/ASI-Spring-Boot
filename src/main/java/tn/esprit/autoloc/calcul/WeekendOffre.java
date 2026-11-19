package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;

@Component("WEEKEND")
public class WeekendOffre implements OffreStrategy {
    @Override
    public double calculerTotal(double tarifJournalier, int nbJours) {
        return tarifJournalier * 0.80 * nbJours;          // -20 % sur le tarif journalier
    }
}
