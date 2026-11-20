package tn.esprit.autoloc.calcul;

public enum VehiculeOffre {
    STANDARD("standard"),
    WEEKEND("weekend"),
    LONGUE_DUREE("longueDuree");

    private final String beanName;

    VehiculeOffre(String beanName) {
        this.beanName = beanName;
    }

    /** Nom du bean Spring {@link OffreStrategy} associé à cette offre. */
    public String getBeanName() {
        return beanName;
    }
}
