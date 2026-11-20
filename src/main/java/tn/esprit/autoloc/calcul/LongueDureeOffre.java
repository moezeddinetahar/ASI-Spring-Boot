package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;

@Component("longueDuree")
public class LongueDureeOffre implements OffreStrategy {
    @Override
    public double calculerTotal(double tarifJournalier, int nbJours) {
        double total = tarifJournalier * 0.75 * nbJours;  // -25 % sur le tarif journalier
        if (total > 2000) {
            total *= 0.95;                                // -5 % si le total dépasse 2000
        }
        return total;
    }
}
