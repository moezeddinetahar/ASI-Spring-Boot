package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmploye;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleEmploye role;


    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

    // Étape 10 : affectation symétrique (le nom `steAgence` était une faute de frappe)
    public void affecterAgence(Agence nouvelle) {
        if (this.agence != null) {
            this.agence.getEmployes().remove(this);   // quitte l'ancienne agence
        }
        this.agence = nouvelle;
        if (nouvelle != null) {
            nouvelle.getEmployes().add(this);
        }
    }
}
