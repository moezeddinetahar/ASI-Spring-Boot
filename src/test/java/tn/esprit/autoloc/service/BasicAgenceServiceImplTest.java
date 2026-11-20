package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicAgenceServiceImplTest {

    @Mock
    private IAgenceRepository repository;

    @InjectMocks
    private BasicAgenceServiceImpl service;

    private static Agence agence(Long id, String nom, String telephone) {
        Agence a = new Agence();
        a.setIdAgence(id);
        a.setNom(nom);
        a.setTelephone(telephone);
        a.setVille("Tunis");
        a.setAdresse("1 Rue Hedi");
        return a;
    }

    @Test
    void checkExigeNomEtTelephone() {
        assertThat(service.check(agence(null, "Lac", "71123456"), true)).isTrue();
        assertThat(service.check(agence(null, "", "71123456"), true)).isFalse();
        assertThat(service.check(agence(null, "Lac", null), true)).isFalse();
    }

    @Test
    void createEnregistreUneAgenceValide() {
        Agence nouvelle = agence(null, "Lac", "71123456");
        when(repository.save(nouvelle)).thenReturn(agence(1L, "Lac", "71123456"));

        assertThat(service.create(nouvelle).getIdAgence()).isEqualTo(1L);
    }

    @Test
    void createRefuseUnIdentifiantRenseigne() {
        Agence avecId = agence(3L, "Lac", "71123456");

        assertThatThrownBy(() -> service.create(avecId)).isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void createRefuseDesChampsManquants() {
        Agence invalide = agence(null, "", "");

        assertThatThrownBy(() -> service.create(invalide))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("champs obligatoires !!!");
    }

    @Test
    void findByIdLeveUneExceptionSiIntrouvable() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agence introuvable avec l'identifiant 9");
    }

    @Test
    void findAllRetourneToutesLesAgences() {
        when(repository.findAll()).thenReturn(List.of(agence(1L, "A", "1"), agence(2L, "B", "2")));

        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void updateCopieLesChamps() {
        Agence existante = agence(1L, "Ancienne", "70000000");
        when(repository.findById(1L)).thenReturn(Optional.of(existante));
        when(repository.save(existante)).thenReturn(existante);

        Agence modification = agence(null, "Nouvelle", "71999999");
        modification.setVille("Sousse");

        Agence resultat = service.update(1L, modification);

        assertThat(resultat.getNom()).isEqualTo("Nouvelle");
        assertThat(resultat.getTelephone()).isEqualTo("71999999");
        assertThat(resultat.getVille()).isEqualTo("Sousse");
    }

    @Test
    void updateRefuseDesChampsInvalides() {
        Agence invalide = agence(null, "", "");
        when(repository.findById(1L)).thenReturn(Optional.of(agence(1L, "A", "1")));

        assertThatThrownBy(() -> service.update(1L, invalide)).isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteByIdSupprimeUneAgenceExistante() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteByIdLeveUneExceptionSiIntrouvable() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(9L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void hasVehiculesSelonLaListeDesVehicules() {
        Agence avec = agence(1L, "A", "1");
        avec.setVehicules(Set.of(new Vehicule()));
        Agence vide = agence(2L, "B", "2");
        vide.setVehicules(Set.of());
        Agence nulle = agence(3L, "C", "3");
        when(repository.findById(1L)).thenReturn(Optional.of(avec));
        when(repository.findById(2L)).thenReturn(Optional.of(vide));
        when(repository.findById(3L)).thenReturn(Optional.of(nulle));

        assertThat(service.hasVehicules(1L)).isTrue();
        assertThat(service.hasVehicules(2L)).isFalse();
        assertThat(service.hasVehicules(3L)).isFalse();
    }
}
