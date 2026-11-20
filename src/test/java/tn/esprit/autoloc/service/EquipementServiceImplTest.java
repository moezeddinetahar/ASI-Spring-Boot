package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IEquipementRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests du service Equipement. Ils couvrent aussi tout le comportement
 * commun de GenericServiceImpl (create, findById, findAll, update, deleteById).
 */
@ExtendWith(MockitoExtension.class)
class EquipementServiceImplTest {

    @Mock
    private IEquipementRepository repository;

    @InjectMocks
    private EquipementServiceImpl service;

    private static Equipement equipement(Long id, String libelle) {
        Equipement e = new Equipement();
        e.setIdEquipement(id);
        e.setLibelle(libelle);
        return e;
    }

    @Test
    void checkAccepteUnLibelleRenseigne() {
        assertThat(service.check(equipement(null, "GPS"), true)).isTrue();
    }

    @Test
    void checkRefuseUnLibelleVideOuNull() {
        assertThat(service.check(equipement(null, ""), true)).isFalse();
        assertThat(service.check(equipement(null, null), true)).isFalse();
    }

    @Test
    void createEnregistreUnEquipementValide() {
        Equipement nouveau = equipement(null, "GPS");
        when(repository.save(nouveau)).thenReturn(equipement(1L, "GPS"));

        Equipement cree = service.create(nouveau);

        assertThat(cree.getIdEquipement()).isEqualTo(1L);
        verify(repository).save(nouveau);
    }

    @Test
    void createRefuseUnIdentifiantDejaRenseigne() {
        Equipement avecId = equipement(5L, "GPS");

        assertThatThrownBy(() -> service.create(avecId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Equipement");
        verify(repository, never()).save(any());
    }

    @Test
    void createRefuseDesChampsObligatoiresManquants() {
        Equipement invalide = equipement(null, "");

        assertThatThrownBy(() -> service.create(invalide))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("champs obligatoires !!!");
        verify(repository, never()).save(any());
    }

    @Test
    void findByIdRetourneLEquipementExistant() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipement(1L, "GPS")));

        assertThat(service.findById(1L).getLibelle()).isEqualTo("GPS");
    }

    @Test
    void findByIdLeveUneExceptionSiIntrouvable() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Equipement introuvable avec l'identifiant 99");
    }

    @Test
    void findAllRetourneTousLesEquipements() {
        // même signature que l'appel fait dans GenericServiceImpl (CrudRepository.findAll)
        CrudRepository<Equipement, Long> crud = repository;
        when(crud.findAll()).thenReturn(List.of(equipement(1L, "GPS"), equipement(2L, "Siège bébé")));

        assertThat(service.findAll())
                .extracting(Equipement::getLibelle)
                .containsExactly("GPS", "Siège bébé");
    }

    @Test
    void updateCopieLesChampsEtEnregistre() {
        Equipement existant = equipement(1L, "Ancien");
        when(repository.findById(1L)).thenReturn(Optional.of(existant));
        when(repository.save(existant)).thenReturn(existant);

        Equipement modifie = service.update(1L, equipement(null, "Nouveau"));

        assertThat(modifie.getLibelle()).isEqualTo("Nouveau");
        assertThat(modifie.getIdEquipement()).isEqualTo(1L);
    }

    @Test
    void updateLeveUneExceptionSiIntrouvable() {
        Equipement modification = equipement(null, "Nouveau");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, modification))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateRefuseDesChampsInvalides() {
        Equipement invalide = equipement(null, "");
        when(repository.findById(1L)).thenReturn(Optional.of(equipement(1L, "Ancien")));

        assertThatThrownBy(() -> service.update(1L, invalide))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteByIdSupprimeUnEquipementExistant() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteByIdLeveUneExceptionSiIntrouvable() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
