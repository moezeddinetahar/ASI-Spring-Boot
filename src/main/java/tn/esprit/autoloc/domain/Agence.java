package tn.esprit.autoloc.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "agence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgence;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String ville;

    @Column(nullable = false, length = 100)
    private String adresse;

    @Column(length = 20)
    private String telephone;

    @OneToMany(mappedBy = "agence", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonIgnoreProperties("agence")
    private Set<Vehicule> vehicules;


    // WRITE_ONLY : accepté dans le JSON reçu (structure imbriquée), jamais renvoyé dans la réponse
    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Employe> employes = new HashSet<>();

    // Étape 11 : champ mappé entier
    @Column(nullable = false)
    private int effectif;

    // Étape 10 : méthodes qui gardent les deux côtés de la relation synchronisés
    public void addEmployee(Employe employe) {
        employes.add(employe);
        employe.setAgence(this);      // côté propriétaire (celui qui écrit la clé étrangère)
    }

    public void removeEmployee(Employe employe) {
        employes.remove(employe);
        employe.setAgence(null);
    }
}
