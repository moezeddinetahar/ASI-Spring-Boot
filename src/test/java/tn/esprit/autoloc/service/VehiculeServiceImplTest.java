package tn.esprit.autoloc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.calcul.LongueDureeOffre;
import tn.esprit.autoloc.calcul.OffreStrategy;
import tn.esprit.autoloc.calcul.StandardOffre;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.calcul.WeekendOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private IVehiculeRepository repository;
    @Mock
    private IAgenceRepository agenceRepository;
    @Mock
    private IEquipementRepository equipementRepository;

    private VehiculeServiceImpl service;

    @BeforeEach
    void setUp() {
        // vraies stratégies, avec les mêmes noms de beans que dans Spring
        Map<String, OffreStrategy> offres = Map.of(
                "standard", new StandardOffre(),
                "weekend", new WeekendOffre(),
                "longueDuree", new LongueDureeOffre());
        service = new VehiculeServiceImpl(repository, offres, agenceRepository, equipementRepository);
    }

    private static Vehicule vehicule(Long id, String tarif) {
        Vehicule v = new Vehicule();
        v.setIdVehicule(id);
        v.setImmatriculation("123TU4567");
        v.setMarque("Toyota");
        v.setModele("Yaris");
        v.setCategorie(CategorieVehicule.CITADINE);
        v.setStatut(StatutVehicule.DISPONIBLE);
        v.setTarifJournalier(tarif == null ? null : new BigDecimal(tarif));
        return v;
    }

    // ---------- check ----------

    @Test
    void checkAccepteUnVehiculeComplet() {
        assertThat(service.check(vehicule(null, "80"), true)).isTrue();
    }

    @Test
    void checkRefuseUnTarifNulOuNegatif() {
        assertThat(service.check(vehicule(null, "0"), true)).isFalse();
        assertThat(service.check(vehicule(null, "-5"), true)).isFalse();
        assertThat(service.check(vehicule(null, null), true)).isFalse();
    }

    @Test
    void checkRefuseUneImmatriculationVide() {
        Vehicule v = vehicule(null, "80");
        v.setImmatriculation("");
        assertThat(service.check(v, true)).isFalse();
    }

    // ---------- calculTarif ----------

    @Test
    void calculTarifOffreStandard() {
        assertThat(service.calculTarif(vehicule(1L, "100"), 3, VehiculeOffre.STANDARD))
                .isCloseTo(300.0, within(0.001));
    }

    @Test
    void calculTarifOffreWeekendAppliqueMoins20Pourcent() {
        assertThat(service.calculTarif(vehicule(1L, "100"), 3, VehiculeOffre.WEEKEND))
                .isCloseTo(240.0, within(0.001));
    }

    @Test
    void calculTarifOffreLongueDureeAppliqueMoins25Pourcent() {
        assertThat(service.calculTarif(vehicule(1L, "100"), 10, VehiculeOffre.LONGUE_DUREE))
                .isCloseTo(750.0, within(0.001));
    }

    @Test
    void calculTarifOffreLongueDureeAjouteMoins5PourcentAuDelaDe2000() {
        // 100 x 0.75 x 30 = 2250 > 2000 -> 2250 x 0.95 = 2137.5
        assertThat(service.calculTarif(vehicule(1L, "100"), 30, VehiculeOffre.LONGUE_DUREE))
                .isCloseTo(2137.5, within(0.001));
    }

    @Test
    void calculTarifRefuseUneOffreNonSupportee() {
        VehiculeServiceImpl sansOffres =
                new VehiculeServiceImpl(repository, Map.of(), agenceRepository, equipementRepository);
        Vehicule v = vehicule(1L, "100");

        assertThatThrownBy(() -> sansOffres.calculTarif(v, 2, VehiculeOffre.STANDARD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offre non supportée");
    }

    // ---------- affectations ----------

    @Test
    void desaffecteRetireLAgence() {
        Vehicule v = vehicule(1L, "100");
        v.setAgence(new Agence());
        when(repository.findById(1L)).thenReturn(Optional.of(v));
        when(repository.save(v)).thenReturn(v);

        assertThat(service.desaffecte(1L).getAgence()).isNull();
    }

    @Test
    void affecteRattacheLeVehiculeALAgence() {
        Vehicule v = vehicule(1L, "100");
        Agence agence = new Agence();
        agence.setIdAgence(5L);
        when(repository.findById(1L)).thenReturn(Optional.of(v));
        when(agenceRepository.findById(5L)).thenReturn(Optional.of(agence));
        when(repository.save(v)).thenReturn(v);

        assertThat(service.affecte(1L, 5L).getAgence()).isSameAs(agence);
    }

    @Test
    void affecteLeveUneExceptionSiAgenceIntrouvable() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicule(1L, "100")));
        when(agenceRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.affecte(1L, 9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agence");
        verify(repository, never()).save(any());
    }

    @Test
    void affecteEquipemmentAjouteLEquipement() {
        Vehicule v = vehicule(1L, "100");
        v.setEquipements(new HashSet<>());
        Equipement gps = new Equipement();
        gps.setIdEquipement(3L);
        gps.setLibelle("GPS");
        when(repository.findById(1L)).thenReturn(Optional.of(v));
        when(equipementRepository.findById(3L)).thenReturn(Optional.of(gps));
        when(repository.save(v)).thenReturn(v);

        assertThat(service.affecteEquipemment(1L, 3L).getEquipements()).containsExactly(gps);
    }

    @Test
    void affecteEquipemmentLeveUneExceptionSiEquipementIntrouvable() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicule(1L, "100")));
        when(equipementRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.affecteEquipemment(1L, 9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equipement");
        verify(repository, never()).save(any());
    }

    @Test
    void updateCopieTousLesChamps() {
        Vehicule existant = vehicule(1L, "100");
        when(repository.findById(1L)).thenReturn(Optional.of(existant));
        when(repository.save(existant)).thenReturn(existant);

        Vehicule modification = vehicule(null, "150");
        modification.setMarque("Kia");
        modification.setStatut(StatutVehicule.LOUE);

        Vehicule resultat = service.update(1L, modification);

        assertThat(resultat.getIdVehicule()).isEqualTo(1L);
        assertThat(resultat.getMarque()).isEqualTo("Kia");
        assertThat(resultat.getStatut()).isEqualTo(StatutVehicule.LOUE);
        assertThat(resultat.getTarifJournalier()).isEqualByComparingTo("150");
    }
}
