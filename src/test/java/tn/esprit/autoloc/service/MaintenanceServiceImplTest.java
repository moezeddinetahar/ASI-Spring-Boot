package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IMaintenanceRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceImplTest {

    @Mock
    private IMaintenanceRepository repository;

    @InjectMocks
    private MaintenanceServiceImpl service;

    private static Maintenance maintenance(LocalDate debut, LocalDate fin) {
        Maintenance m = new Maintenance();
        m.setDateDebut(debut);
        m.setDateFin(fin);
        m.setDescription("Vidange");
        m.setVehicule(new Vehicule());
        return m;
    }

    @Test
    void checkAccepteDesDatesCoherentes() {
        assertThat(service.check(maintenance(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3)), true)).isTrue();
    }

    @Test
    void checkAccepteUneMaintenanceSurUneSeuleJournee() {
        LocalDate jour = LocalDate.of(2026, 5, 1);
        assertThat(service.check(maintenance(jour, jour), true)).isTrue();
    }

    @Test
    void checkRefuseUneDateDeFinAvantLeDebut() {
        assertThat(service.check(maintenance(LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 1)), true)).isFalse();
    }

    @Test
    void checkRefuseUnVehiculeNul() {
        Maintenance m = maintenance(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2));
        m.setVehicule(null);
        assertThat(service.check(m, true)).isFalse();
    }

    @Test
    void checkRefuseDesDatesNulles() {
        assertThat(service.check(maintenance(null, LocalDate.of(2026, 5, 2)), true)).isFalse();
        assertThat(service.check(maintenance(LocalDate.of(2026, 5, 1), null), true)).isFalse();
    }

    @Test
    void updateCopieTousLesChamps() {
        Maintenance existante = maintenance(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2));
        existante.setIdMaintenance(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        when(repository.save(existante)).thenReturn(existante);

        Maintenance modification = maintenance(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 12));
        modification.setDescription("Changement des pneus");

        Maintenance resultat = service.update(1L, modification);

        assertThat(resultat.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(resultat.getDateFin()).isEqualTo(LocalDate.of(2026, 6, 12));
        assertThat(resultat.getDescription()).isEqualTo("Changement des pneus");
        assertThat(resultat.getVehicule()).isSameAs(modification.getVehicule());
    }
}
