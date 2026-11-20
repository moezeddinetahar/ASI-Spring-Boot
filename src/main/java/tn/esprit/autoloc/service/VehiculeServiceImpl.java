package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.calcul.OffreStrategy;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.util.Map;

@Service
public class VehiculeServiceImpl extends GenericServiceImpl<Vehicule, Long> implements IVehiculeService {

    // Spring injecte tous les beans OffreStrategy : clé = nom du bean (standard, weekend, longueDuree)
    private final Map<String, OffreStrategy> offres;
    private final IAgenceRepository agenceRepository;
    private final IEquipementRepository equipementRepository;

    public VehiculeServiceImpl(IVehiculeRepository repository, Map<String, OffreStrategy> offres , IAgenceRepository agenceRepository , IEquipementRepository equipementRepository) {
        super(repository);
        this.offres = offres;
        this.agenceRepository = agenceRepository;
        this.equipementRepository = equipementRepository;
    }

    @Override
    protected String getEntityName() {
        return "Vehicule";
    }

    @Override
    protected Long getId(Vehicule entity) {
        return entity.getIdVehicule();
    }

    @Override
    protected void setFields(Vehicule source, Vehicule target) {
        target.setImmatriculation(source.getImmatriculation());
        target.setMarque(source.getMarque());
        target.setModele(source.getModele());
        target.setCategorie(source.getCategorie());
        target.setTarifJournalier(source.getTarifJournalier());
        target.setStatut(source.getStatut());
        target.setAgence(source.getAgence());
    }

    @Override
    public boolean check(Vehicule entity, boolean isNew) {
        return Utils.isNotEmpty(entity.getImmatriculation())
                && Utils.isNotEmpty(entity.getMarque())
                && Utils.isNotEmpty(entity.getModele())
                && entity.getCategorie() != null
                && entity.getStatut() != null
                && entity.getTarifJournalier() != null
                && entity.getTarifJournalier().signum() > 0;
    }


    @Override
    public double calculTarif(Vehicule vehicule, int nbJours, VehiculeOffre offre) {
        OffreStrategy strategie = offres.get(offre.getBeanName());
        if (strategie == null) {
            throw new IllegalArgumentException("Offre non supportée : " + offre);
        }
        return strategie.calculerTotal(vehicule.getTarifJournalier().doubleValue(), nbJours);
    }

    @Override
    @Transactional
    public Vehicule desaffecte(Long id) {
        var vehicule = findById(id);
        vehicule.setAgence(null);
        return repository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule affecte(Long id, Long idAgence) {
        var vehicule = findById(id);
        var agence = agenceRepository.findById(idAgence).orElseThrow(() -> new ResourceNotFoundException("Agence", idAgence.toString()));
        vehicule.setAgence(agence);
        return repository.save(vehicule);
    }

    @Override
    @Transactional
    public Vehicule affecteEquipemment(Long id, Long idEquipement) {
        var vehicule = findById(id);
        var equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", idEquipement.toString()));
        // Vehicule est le côté PROPRIÉTAIRE (@JoinTable) : l'ajout se fait ici, pas sur Equipement
        vehicule.getEquipements().add(equipement);
        return repository.save(vehicule);
    }

}
