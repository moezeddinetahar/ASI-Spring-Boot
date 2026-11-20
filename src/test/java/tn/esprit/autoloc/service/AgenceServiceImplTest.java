package tn.esprit.autoloc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.io.Rapport;
import tn.esprit.autoloc.io.RapportFactory;
import tn.esprit.autoloc.repository.IAgenceRepository;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgenceServiceImplTest {

    @Mock
    private IAgenceRepository repository;
    @Mock
    private IVehiculeService vehiculeService;
    @Mock
    private RapportFactory rapportFactory;
    @Mock
    private IEmployeService employeService;
    @Mock
    private Rapport rapport;

    private AgenceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AgenceServiceImpl(repository, vehiculeService, rapportFactory, employeService);
    }

    private static Agence agence(Long id, String nom, String telephone) {
        Agence a = new Agence();
        a.setIdAgence(id);
        a.setNom(nom);
        a.setTelephone(telephone);
        a.setVille("Tunis");
        a.setAdresse("5 Rue du Lac");
        return a;
    }

    private static Employe employe(String nom) {
        Employe e = new Employe();
        e.setNom(nom);
        e.setPrenom("Ali");
        e.setRole(RoleEmploye.AGENT);
        return e;
    }

    @Test
    void checkExigeNomEtTelephone() {
        assertThat(service.check(agence(null, "Lac", "71123456"), true)).isTrue();
        assertThat(service.check(agence(null, "Lac", ""), true)).isFalse();
        assertThat(service.check(agence(null, null, "71123456"), true)).isFalse();
    }

    @Test
    void createEnregistreUneAgenceValide() {
        Agence nouvelle = agence(null, "Lac", "71123456");
        when(repository.save(nouvelle)).thenReturn(agence(1L, "Lac", "71123456"));

        assertThat(service.create(nouvelle).getIdAgence()).isEqualTo(1L);
    }

    @Test
    void updateNeCopiePasLesVehicules() {
        Agence existante = agence(1L, "Ancienne", "70000000");
        Set<Vehicule> vehicules = Set.of(new Vehicule());
        existante.setVehicules(vehicules);
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        when(repository.save(existante)).thenReturn(existante);

        Agence modification = agence(null, "Nouvelle", "71999999");
        modification.setVehicules(Set.of());

        Agence resultat = service.update(1L, modification);

        assertThat(resultat.getNom()).isEqualTo("Nouvelle");
        assertThat(resultat.getVehicules()).isSameAs(vehicules);
    }

    @Test
    void hasVehiculesVraiSiLAgenceADesVehicules() {
        Agence a = agence(1L, "Lac", "71123456");
        a.setVehicules(Set.of(new Vehicule()));
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        assertThat(service.hasVehicules(1L)).isTrue();
    }

    @Test
    void hasVehiculesFauxSansVehicules() {
        when(repository.findById(2L)).thenReturn(Optional.of(agence(2L, "Lac", "71123456")));

        assertThat(service.hasVehicules(2L)).isFalse();
    }

    @Test
    void simulerAdditionneLesTarifsDeTousLesVehicules() {
        Vehicule v1 = new Vehicule();
        Vehicule v2 = new Vehicule();
        Agence a = agence(1L, "Lac", "71123456");
        a.setVehicules(new LinkedHashSet<>(List.of(v1, v2)));

        when(rapportFactory.creer(any())).thenReturn(rapport);
        when(vehiculeService.calculTarif(v1, 3, VehiculeOffre.STANDARD)).thenReturn(100.0);
        when(vehiculeService.calculTarif(v2, 3, VehiculeOffre.STANDARD)).thenReturn(150.0);
        when(rapport.contenu()).thenReturn("rapport");

        String resultat = service.simuler(a, 3, VehiculeOffre.STANDARD);

        assertThat(resultat).isEqualTo("rapport");
        verify(rapport).entete(a);
        verify(rapport).total(250.0);
    }

    @Test
    void simulerSansVehiculeDonneUnTotalNul() {
        Agence a = agence(1L, "Lac", "71123456");
        a.setVehicules(null);
        when(rapportFactory.creer(any())).thenReturn(rapport);
        when(rapport.contenu()).thenReturn("vide");

        assertThat(service.simuler(a, 5, VehiculeOffre.WEEKEND)).isEqualTo("vide");
        verify(rapport).total(0.0);
    }

    @Test
    void updateAvecEmployesAjouteLesEmployesEtMetAJourLEffectif() {
        Agence existante = agence(1L, "Lac", "71123456");
        existante.setEmployes(new HashSet<>());
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        when(repository.save(existante)).thenReturn(existante);

        Employe e1 = employe("Haddad");
        Employe e2 = employe("Gharbi");
        Agence modification = agence(null, "Lac Nord", "71123456");
        modification.setEmployes(new HashSet<>(Set.of(e1, e2)));

        Agence resultat = service.updateAvecEmployes(1L, modification);

        assertThat(resultat.getNom()).isEqualTo("Lac Nord");
        assertThat(resultat.getEffectif()).isEqualTo(2);
        assertThat(resultat.getEmployes()).containsExactlyInAnyOrder(e1, e2);
        assertThat(e1.getAgence()).isSameAs(existante);
        assertThat(e2.getAgence()).isSameAs(existante);
        verify(employeService, times(2)).create(any(Employe.class));
        verify(repository, times(2)).save(existante);
    }

    @Test
    void updateAvecEmployesSansEmployesMetLEffectifAZero() {
        Agence existante = agence(1L, "Lac", "71123456");
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        when(repository.save(existante)).thenReturn(existante);

        Agence modification = agence(null, "Lac", "71123456");
        modification.setEmployes(null);

        Agence resultat = service.updateAvecEmployes(1L, modification);

        assertThat(resultat.getEffectif()).isZero();
        verify(employeService, never()).create(any());
    }
}
