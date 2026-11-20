package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IPaiementRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceImplTest {

    @Mock
    private IPaiementRepository repository;

    @InjectMocks
    private PaiementServiceImpl service;

    private static Paiement paiement(Long id, String montant) {
        Paiement p = new Paiement();
        p.setIdPaiement(id);
        p.setMontant(new BigDecimal(montant));
        p.setModePaiement(ModePaiement.CARTE);
        return p;
    }

    @Test
    void findByIdRetourneLePaiementExistant() {
        when(repository.findById(1L)).thenReturn(Optional.of(paiement(1L, "120.00")));

        assertThat(service.findById(1L).getMontant()).isEqualByComparingTo("120.00");
    }

    @Test
    void findByIdLeveUneExceptionSiIntrouvable() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Paiement introuvable avec l'identifiant 42");
    }

    @Test
    void findAllRetourneTousLesPaiements() {
        when(repository.findAll()).thenReturn(List.of(paiement(1L, "50"), paiement(2L, "75")));

        assertThat(service.findAll()).hasSize(2);
    }
}
