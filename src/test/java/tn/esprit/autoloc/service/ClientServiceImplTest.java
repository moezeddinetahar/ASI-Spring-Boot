package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IClientRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private IClientRepository repository;

    @InjectMocks
    private ClientServiceImpl service;

    private static Client client() {
        Client c = new Client();
        c.setNom("Ben Ali");
        c.setPrenom("Sami");
        c.setEmail("sami@mail.tn");
        c.setTelephone("22111222");
        c.setNumPermis("P-12345");
        c.setDateInscription(LocalDate.of(2026, 1, 15));
        return c;
    }

    @Test
    void checkAccepteUnClientComplet() {
        assertThat(service.check(client(), true)).isTrue();
    }

    @Test
    void checkRefuseUnEmailManquant() {
        Client c = client();
        c.setEmail("");
        assertThat(service.check(c, true)).isFalse();
    }

    @Test
    void checkRefuseUneDateInscriptionNulle() {
        Client c = client();
        c.setDateInscription(null);
        assertThat(service.check(c, true)).isFalse();
    }

    @Test
    void checkAccepteUnTelephoneNonRenseigne() {
        Client c = client();
        c.setTelephone(null);
        assertThat(service.check(c, true)).isTrue();
    }

    @Test
    void createRefuseUnIdentifiantDejaRenseigne() {
        Client c = client();
        c.setIdClient(3L);

        assertThatThrownBy(() -> service.create(c))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client");
    }

    @Test
    void updateCopieTousLesChamps() {
        Client existant = client();
        existant.setIdClient(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existant));
        when(repository.save(existant)).thenReturn(existant);

        Client modification = client();
        modification.setNom("Trabelsi");
        modification.setEmail("nouveau@mail.tn");
        modification.setNumPermis("P-99999");

        Client resultat = service.update(1L, modification);

        assertThat(resultat.getIdClient()).isEqualTo(1L);
        assertThat(resultat.getNom()).isEqualTo("Trabelsi");
        assertThat(resultat.getEmail()).isEqualTo("nouveau@mail.tn");
        assertThat(resultat.getNumPermis()).isEqualTo("P-99999");
    }

    @Test
    void findByIdIndiqueLeNomDeLEntite() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client introuvable avec l'identifiant 7");
    }
}
