package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Agence;

public interface IAgenceEmployes {
    /** Met à jour l'agence puis insère ses nouveaux employés ; met à jour l'effectif. */
    Agence updateAvecEmployes(Long id, Agence agence);
}
