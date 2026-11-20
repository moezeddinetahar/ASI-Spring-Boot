package tn.esprit.autoloc.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.autoloc.domain.Agence;

public class RapportConsole implements Rapport {
    // java:S106 : un logger remplace System.out
    private static final Logger LOGGER = LoggerFactory.getLogger(RapportConsole.class);

    private final Rapport delegue;

    public RapportConsole(Rapport delegue) { this.delegue = delegue; }

    @Override
    public void entete(Agence a) {
        LOGGER.info("Agence #{} - {} - Tel : {}", a.getIdAgence(), a.getNom(), a.getTelephone());
    }

    @Override
    public void total(double t) { delegue.total(t); }

    @Override
    public String contenu() { return delegue.contenu(); }
}
