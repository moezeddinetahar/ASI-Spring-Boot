package tn.esprit.autoloc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AgenceTests {
    @Autowired
    private AgenceRepositoryMock agenceRepository;

    @Test
    public void addAgence(){
        Agence a = new Agence();
        a.setIdAgence(8L);
        a.setNom("Agence ariana");
        a.setAdresse("1 Rue Hedi");
        a.setVille("Tunis");
        a.setTelephone("71585874");

        Vehicule v1 = new Vehicule();
        v1.setIdVehicule(12L);
        v1.setCategorie(CategorieVehicule.SUV);
        v1.setImmatriculation("785414TU96");
        v1.setMarque("Isuzu");
        v1.setModele("DMax");
        v1.setStatut(StatutVehicule.MAINTENANCE);
        v1.setTarifJournalier(new BigDecimal(100));
        v1.setAgence(a); // 👈 Indispensable : lier le véhicule à l'agence

        Vehicule v2 = new Vehicule();
        v2.setIdVehicule(13L);
        v2.setCategorie(CategorieVehicule.UTILITAIRE);
        v2.setImmatriculation("785414TU95");
        v2.setMarque("Toyota");
        v2.setModele("Yaris");
        v2.setStatut(StatutVehicule.DISPONIBLE);
        v2.setTarifJournalier(new BigDecimal(80));
        v2.setAgence(a); // 👈 Indispensable : lier le véhicule à l'agence

        // Utilisation d'un HashSet mutable au lieu de Set.of()
        Set<Vehicule> vehicules = new HashSet<>();
        vehicules.add(v1);
        vehicules.add(v2);

        a.setVehicules(vehicules);
        agenceRepository.save(a);
    }

    @Test
    public void loadAgence() {
        Iterable<Agence> agences = agenceRepository.findAll();

        StringBuilder sb = new StringBuilder();

        for (Agence agence : agences) {
            sb.append(agence.getIdAgence()).append(" | ").append(agence.getNom()).append("\n");

            int count = (agence.getVehicules() != null) ? agence.getVehicules().size() : 0;
            sb.append("Vehicules Count : ").append(count).append("\n");

            if (agence.getVehicules() != null) {
                for (Vehicule v : agence.getVehicules()) {
                    sb.append("=== ").append(v.getIdVehicule()).append("|").append(v.getImmatriculation()).append("\n");
                }
            }
        }

        org.junit.jupiter.api.Assertions.fail(sb.toString().trim());
    }
}

interface AgenceRepositoryMock extends CrudRepository<Agence, Long>{
}