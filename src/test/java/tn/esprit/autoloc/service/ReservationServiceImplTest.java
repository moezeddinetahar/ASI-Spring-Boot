package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IReservationRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private IReservationRepository repository;

    @InjectMocks
    private ReservationServiceImpl service;

    private static Reservation reservation() {
        Reservation r = new Reservation();
        r.setDateDebut(LocalDate.of(2026, 7, 1));
        r.setDateFin(LocalDate.of(2026, 7, 5));
        r.setStatut(StatutReservation.EN_ATTENTE);
        r.setClient(new Client());
        r.setVehicule(new Vehicule());
        return r;
    }

    @Test
    void checkAccepteUneReservationComplete() {
        assertThat(service.check(reservation(), true)).isTrue();
    }

    @Test
    void checkAccepteUneReservationSansContrat() {
        Reservation r = reservation();
        r.setContrat(null);
        assertThat(service.check(r, true)).isTrue();
    }

    @Test
    void checkRefuseUneDateDeFinAvantLeDebut() {
        Reservation r = reservation();
        r.setDateFin(LocalDate.of(2026, 6, 30));
        assertThat(service.check(r, true)).isFalse();
    }

    @Test
    void checkRefuseUnStatutUnClientOuUnVehiculeManquant() {
        Reservation sansStatut = reservation();
        sansStatut.setStatut(null);
        Reservation sansClient = reservation();
        sansClient.setClient(null);
        Reservation sansVehicule = reservation();
        sansVehicule.setVehicule(null);

        assertThat(service.check(sansStatut, true)).isFalse();
        assertThat(service.check(sansClient, true)).isFalse();
        assertThat(service.check(sansVehicule, true)).isFalse();
    }

    @Test
    void updateCopieTousLesChampsDontLeContrat() {
        Reservation existante = reservation();
        existante.setIdReservation(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        when(repository.save(existante)).thenReturn(existante);

        Reservation modification = reservation();
        modification.setStatut(StatutReservation.CONFIRMEE);
        Contrat contrat = new Contrat();
        modification.setContrat(contrat);

        Reservation resultat = service.update(1L, modification);

        assertThat(resultat.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
        assertThat(resultat.getContrat()).isSameAs(contrat);
        assertThat(resultat.getClient()).isSameAs(modification.getClient());
        assertThat(resultat.getVehicule()).isSameAs(modification.getVehicule());
    }
}
