package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IContratRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratServiceImplTest {

    @Mock
    private IContratRepository repository;

    @InjectMocks
    private ContratServiceImpl service;

    private static Contrat contrat(String montant) {
        Contrat c = new Contrat();
        c.setDateSignature(LocalDate.of(2026, 3, 1));
        c.setMontantTotal(montant == null ? null : new BigDecimal(montant));
        c.setValide(true);
        return c;
    }

    @Test
    void checkAccepteUnMontantPositif() {
        assertThat(service.check(contrat("450.00"), true)).isTrue();
    }

    @Test
    void checkRefuseUnMontantNulOuNegatif() {
        assertThat(service.check(contrat("0"), true)).isFalse();
        assertThat(service.check(contrat("-10"), true)).isFalse();
        assertThat(service.check(contrat(null), true)).isFalse();
    }

    @Test
    void checkRefuseUneDateDeSignatureNulle() {
        Contrat c = contrat("100");
        c.setDateSignature(null);
        assertThat(service.check(c, true)).isFalse();
    }

    @Test
    void updateNeCopiePasLesPaiements() {
        Contrat existant = contrat("100");
        existant.setIdContrat(1L);
        Set<Paiement> paiementsExistants = new HashSet<>(Set.of(new Paiement()));
        existant.setPaiements(paiementsExistants);
        when(repository.findById(1L)).thenReturn(Optional.of(existant));
        when(repository.save(existant)).thenReturn(existant);

        Contrat modification = contrat("900.50");
        modification.setValide(false);
        modification.setPaiements(new HashSet<>());

        Contrat resultat = service.update(1L, modification);

        assertThat(resultat.getMontantTotal()).isEqualByComparingTo("900.50");
        assertThat(resultat.isValide()).isFalse();
        assertThat(resultat.getPaiements()).isSameAs(paiementsExistants).hasSize(1);
    }

    @Test
    void findByIdIndiqueLeNomDeLEntite() {
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(4L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Contrat");
    }
}
