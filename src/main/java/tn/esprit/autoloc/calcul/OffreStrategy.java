package tn.esprit.autoloc.calcul;

/** Stratégie de calcul du total d'une location selon l'offre. */
public interface OffreStrategy {
    double calculerTotal(double tarifJournalier, int nbJours);
}
