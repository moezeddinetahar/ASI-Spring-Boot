package tn.esprit.autoloc.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.io.Rapport;
import tn.esprit.autoloc.io.RapportConsoleActif;
import tn.esprit.autoloc.io.RapportFactory;
import tn.esprit.autoloc.repository.IAgenceRepository;

@Service
@Primary
@RapportConsoleActif   // Decorator : l'en-tête sera affiché sur la console
public class AgenceServiceImpl extends GenericServiceImpl<Agence, Long> implements IAgenceService, IAgenceSimulation, IAgenceEmployes {
    private final IVehiculeService vehiculeService;
    private final RapportFactory rapportFactory;
    private final IEmployeService employeService;

    public AgenceServiceImpl(IAgenceRepository repository, IVehiculeService vehiculeService,
                             RapportFactory rapportFactory, IEmployeService employeService) {
        super(repository);
        this.vehiculeService = vehiculeService;
        this.rapportFactory = rapportFactory;
        this.employeService = employeService;
    }

    @Override
    protected String getEntityName() {
        return "Agence";
    }

    @Override
    protected Long getId(Agence agence) {
        return agence.getIdAgence();
    }

    @Override
    protected void setFields(Agence source, Agence target) {
        target.setNom(source.getNom());
        target.setAdresse(source.getAdresse());
        target.setVille(source.getVille());
        target.setTelephone(source.getTelephone());
        // les véhicules ne sont pas copiés : ils se gèrent via leur propre service
    }

    @Override
    public boolean check(Agence agence, boolean isNew) {
        return Utils.isNotEmpty(agence.getNom())
                && Utils.isNotEmpty(agence.getTelephone());
    }

    @Override
    public boolean hasVehicules(Long id) {
        Agence agence = findById(id);
        return agence.getVehicules() != null && !agence.getVehicules().isEmpty();
    }

    @Override
    public String simuler(Agence agence, int nbJours, VehiculeOffre offre) {
        // Factory : le type de rapport dépend des annotations de cette classe
        Rapport rapport = rapportFactory.creer(getClass());
        rapport.entete(agence);

        double total = 0;
        if (agence.getVehicules() != null) {
            for (Vehicule vehicule : agence.getVehicules()) {
                total += vehiculeService.calculTarif(vehicule, nbJours, offre);
            }
        }

        rapport.total(total);
        return rapport.contenu();
    }

    /**
     * Étape 12 : mise à jour de l'agence + insertion de ses nouveaux employés.
     * Étape 17 : @Transactional -> tout réussit ou tout est annulé (rollback sur RuntimeException).
     */
    @Override
    @Transactional
    public Agence updateAvecEmployes(Long id, Agence agence) {
        // 1. mettre à jour d'abord l'agence (lève 404 / 400 si id inconnu ou champs invalides)
        Agence existante = update(id, agence);

        // 2. insérer les employés via le service des employés
        //    (lève IllegalArgumentException si un employé est invalide)
        var nouveaux = agence.getEmployes() == null ? new java.util.ArrayList<Employe>()
                                                    : new java.util.ArrayList<>(agence.getEmployes());
        for (Employe employe : nouveaux) {
            existante.addEmployee(employe);        // symétrie + côté propriétaire renseigné
            employeService.create(employe);
        }

        // 3. mettre à jour l'effectif avec la taille de la liste
        existante.setEffectif(nouveaux.size());
        return repository.save(existante);
    }
}
