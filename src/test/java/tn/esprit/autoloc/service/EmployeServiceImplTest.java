package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IEmployeRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeServiceImplTest {

    @Mock
    private IEmployeRepository repository;

    @InjectMocks
    private EmployeServiceImpl service;

    private static Employe employe(String nom, RoleEmploye role) {
        Employe e = new Employe();
        e.setNom(nom);
        e.setPrenom("Amine");
        e.setRole(role);
        return e;
    }

    @Test
    void checkAccepteUnEmployeComplet() {
        assertThat(service.check(employe("Haddad", RoleEmploye.AGENT), true)).isTrue();
    }

    @Test
    void checkRefuseUnRoleNul() {
        assertThat(service.check(employe("Haddad", null), true)).isFalse();
    }

    @Test
    void checkRefuseUnNomVide() {
        assertThat(service.check(employe("", RoleEmploye.MANAGER), true)).isFalse();
    }

    @Test
    void updateCopieLesChampsEtLAgence() {
        Employe existant = employe("Haddad", RoleEmploye.AGENT);
        existant.setIdEmploye(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existant));
        when(repository.save(existant)).thenReturn(existant);

        Agence agence = new Agence();
        agence.setIdAgence(2L);
        Employe modification = employe("Gharbi", RoleEmploye.MANAGER);
        modification.setAgence(agence);

        Employe resultat = service.update(1L, modification);

        assertThat(resultat.getNom()).isEqualTo("Gharbi");
        assertThat(resultat.getRole()).isEqualTo(RoleEmploye.MANAGER);
        assertThat(resultat.getAgence()).isSameAs(agence);
    }

    @Test
    void findByIdIndiqueLeNomDeLEntite() {
        when(repository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(8L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employe");
    }
}
