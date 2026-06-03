package com.example.config;

import com.example.entity.Patient;
import com.example.entity.Personnel;
import com.example.entity.Salle;
import com.example.entity.enums.StatutSalle;
import com.example.repository.PatientRepository;
import com.example.repository.PersonnelRepository;
import com.example.repository.SalleRepository;
import com.example.service.MaterielService;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import com.example.service.BoiteChirurgicaleService;
import com.example.service.MachineService;
import com.example.service.UniteMaterielService;

import java.util.List;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final SalleRepository salleRepository;
    private final PersonnelRepository personnelRepository;
    private final MaterielService materielService;
    private final BoiteChirurgicaleService boiteChirurgicaleService;
    private final MachineService machineService;
    private final UniteMaterielService uniteMaterielService;

    public DataInitializer(PatientRepository patientRepository,
                           SalleRepository salleRepository,
                           PersonnelRepository personnelRepository,
                           MaterielService materielService,
                           BoiteChirurgicaleService boiteChirurgicaleService,
                           MachineService machineService,
                           UniteMaterielService uniteMaterielService) {
        this.patientRepository = patientRepository;
        this.salleRepository = salleRepository;
        this.personnelRepository = personnelRepository;
        this.materielService = materielService;
        this.boiteChirurgicaleService = boiteChirurgicaleService;
        this.machineService = machineService;
        this.uniteMaterielService = uniteMaterielService;
    }

    @Override
    public void run(@NotNull String... args) {

        if (patientRepository.count() == 0) {
            patientRepository.save(new Patient("BK123456", "Belhaj", "Yaniss", LocalDate.of(2004, 5, 12)));
            patientRepository.save(new Patient("AB987654", "El Amrani", "Sara", LocalDate.of(1998, 3, 20)));
            patientRepository.save(new Patient("CD456789", "Ait Ali", "Youssef", LocalDate.of(1985, 11, 8)));
        }

        if (salleRepository.count() == 0) {
            salleRepository.save(new Salle("SALLE-01", "Chirurgie générale", StatutSalle.DISPONIBLE));
            salleRepository.save(new Salle("SALLE-02", "Orthopédie", StatutSalle.DISPONIBLE));
            salleRepository.save(new Salle("SALLE-03", "Urgence", StatutSalle.MAINTENANCE));
        }

        if (personnelRepository.count() == 0) {
            personnelRepository.save(new Personnel("MED001", "Karimi", "Omar", "Chirurgie"));
            personnelRepository.save(new Personnel("MED002", "Bennani", "Nadia", "Anesthésie"));
            personnelRepository.save(new Personnel("INF001", "Mansouri", "Imane", "Infirmier bloc"));
            personnelRepository.save(new Personnel("INF002", "Rami", "Sofia", "Infirmier bloc"));
        }

        createMaterielIfEmpty();
        createMachinesIfEmpty();
        createBoitesIfEmpty();
    }

    private void createMaterielIfEmpty() {
        try {
            materielService.createMateriel("Scalpel", "Instrument chirurgical", 10, 8, 2);
            materielService.createMateriel("Pince chirurgicale", "Instrument chirurgical", 8, 8, 2);
            materielService.createMateriel("Respirateur", "Équipement médical", 3, 3, 1);
            materielService.createMateriel("Set de suture", "Consommable", 15, 15, 5);
        } catch (IllegalArgumentException ignored) {
            // Le matériel existe déjà, donc on ne fait rien.
        }
    }

    private void createMachinesIfEmpty() {
        try {
            machineService.createMachine("Laveur désinfecteur 01", TypeMachine.LAVAGE, 45, null, StatutMachine.IDLE);
            machineService.createMachine("Laveur désinfecteur 02", TypeMachine.LAVAGE, 50, null, StatutMachine.IDLE);
            machineService.createMachine("Autoclave vapeur 01", TypeMachine.STERILISATION, 90, null, StatutMachine.IDLE);
            machineService.createMachine("Autoclave vapeur 02", TypeMachine.STERILISATION, 100, null, StatutMachine.IDLE);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void createBoitesIfEmpty() {
        try {
            List<UniteMateriel> unites = uniteMaterielService.findAll();

            if (unites.size() < 6) {
                return;
            }

            boiteChirurgicaleService.createBoite(
                    "BOX-APP-001",
                    "Boîte appendicectomie",
                    PrioriteIntervention.NORMALE,
                    "Bloc opératoire",
                    "Chirurgie générale",
                    List.of(
                            unites.get(0).getId(),
                            unites.get(1).getId(),
                            unites.get(2).getId()
                    )
            );

            boiteChirurgicaleService.createBoite(
                    "BOX-ORTH-001",
                    "Boîte orthopédie",
                    PrioriteIntervention.URGENTE,
                    "Bloc opératoire",
                    "Orthopédie",
                    List.of(
                            unites.get(3).getId(),
                            unites.get(4).getId(),
                            unites.get(5).getId()
                    )
            );

        } catch (IllegalArgumentException ignored) {
        }
    }
}