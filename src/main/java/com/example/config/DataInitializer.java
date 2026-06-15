package com.example.config;

import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.repository.PatientRepository;
import com.example.repository.PersonnelRepository;
import com.example.repository.SalleRepository;
import com.example.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
    private final DemandeSterilisationService demandeSterilisationService;
    private final ProcessusSterilisationService processusSterilisationService;
    private final IncidentSterilisationService incidentSterilisationService;

    public DataInitializer(PatientRepository patientRepository,
                           SalleRepository salleRepository,
                           PersonnelRepository personnelRepository,
                           MaterielService materielService,
                           BoiteChirurgicaleService boiteChirurgicaleService,
                           MachineService machineService,
                           UniteMaterielService uniteMaterielService,
                           DemandeSterilisationService demandeSterilisationService,
                           ProcessusSterilisationService processusSterilisationService,
                           IncidentSterilisationService incidentSterilisationService) {
        this.patientRepository = patientRepository;
        this.salleRepository = salleRepository;
        this.personnelRepository = personnelRepository;
        this.materielService = materielService;
        this.boiteChirurgicaleService = boiteChirurgicaleService;
        this.machineService = machineService;
        this.uniteMaterielService = uniteMaterielService;
        this.demandeSterilisationService = demandeSterilisationService;
        this.processusSterilisationService = processusSterilisationService;
        this.incidentSterilisationService = incidentSterilisationService;
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
        createDemoSterilisationScenarios();
    }

    private void createMaterielIfEmpty() {
        try {
            materielService.createMateriel("Scalpel", "Instrument chirurgical", 10, 8, 2);
            materielService.createMateriel("Pince chirurgicale", "Instrument chirurgical", 8, 8, 2);
            materielService.createMateriel("Respirateur", "Équipement médical", 3, 3, 1);
            materielService.createMateriel("Set de suture", "Consommable", 15, 15, 5);
        } catch (IllegalArgumentException ignored) {
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
                    List.of(unites.get(0).getId(), unites.get(1).getId(), unites.get(2).getId())
            );

            boiteChirurgicaleService.createBoite(
                    "BOX-ORTH-001",
                    "Boîte orthopédie",
                    PrioriteIntervention.URGENTE,
                    "Bloc opératoire",
                    "Orthopédie",
                    List.of(unites.get(3).getId(), unites.get(4).getId(), unites.get(5).getId())
            );

            boiteChirurgicaleService.createBoite(
                    "BOX-CARD-001",
                    "Boîte cardiologie",
                    PrioriteIntervention.URGENTE,
                    "Bloc opératoire",
                    "Cardiologie",
                    List.of(unites.get(0).getId(), unites.get(3).getId())
            );

            boiteChirurgicaleService.createBoite(
                    "BOX-NEUR-001",
                    "Boîte neurochirurgie",
                    PrioriteIntervention.URGENTE,
                    "Bloc opératoire",
                    "Neurochirurgie",
                    List.of(unites.get(1).getId(), unites.get(4).getId())
            );

            boiteChirurgicaleService.createBoite(
                    "BOX-UROL-001",
                    "Boîte urologie",
                    PrioriteIntervention.NORMALE,
                    "Bloc opératoire",
                    "Urologie",
                    List.of(unites.get(2).getId(), unites.get(5).getId())
            );

        } catch (IllegalArgumentException ignored) {
        }
    }

    private void createDemoSterilisationScenarios() {
        try {
            List<BoiteChirurgicale> boites = boiteChirurgicaleService.findAll();
            List<Machine> machines = machineService.findAll();

            if (boites.size() < 2 || machines.size() < 2) {
                return;
            }

            Machine laveur = machines.stream()
                    .filter(m -> m.getTypeMachine() == TypeMachine.LAVAGE)
                    .findFirst()
                    .orElse(null);

            Machine autoclave = machines.stream()
                    .filter(m -> m.getTypeMachine() == TypeMachine.STERILISATION)
                    .findFirst()
                    .orElse(null);

            if (laveur == null || autoclave == null) {
                return;
            }

            BoiteChirurgicale boiteRefusee = boites.get(0);
            BoiteChirurgicale boiteEchec = boites.get(1);

            demandeSterilisationService.createDemande(
                    "DEM-REFUS-001",
                    LocalDate.now().plusDays(1),
                    PrioriteIntervention.NORMALE,
                    boiteRefusee.getId(),
                    null,
                    "Demande de démonstration : refusée par le service de stérilisation"
            );

            DemandeSterilisation demandeRefusee = demandeSterilisationService.findAll().stream()
                    .filter(d -> d.getCodeDemande().equals("DEM-REFUS-001"))
                    .findFirst()
                    .orElseThrow();

            demandeSterilisationService.envoyerDemande(demandeRefusee.getId());
            demandeSterilisationService.refuserDemande(demandeRefusee.getId());


            demandeSterilisationService.createDemande(
                    "DEM-ECHEC-001",
                    LocalDate.now().plusDays(1),
                    PrioriteIntervention.URGENTE,
                    boiteEchec.getId(),
                    null,
                    "Demande de démonstration : cycle avec incident"
            );

            DemandeSterilisation demandeEchec = demandeSterilisationService.findAll().stream()
                    .filter(d -> d.getCodeDemande().equals("DEM-ECHEC-001"))
                    .findFirst()
                    .orElseThrow();

            demandeSterilisationService.envoyerDemande(demandeEchec.getId());
            demandeSterilisationService.accepterDemande(demandeEchec.getId());

            processusSterilisationService.creerProcessus(
                    demandeEchec.getId(),
                    laveur.getId(),
                    autoclave.getId(),
                    "Processus de démonstration avec incident"
            );

            ProcessusSterilisation processus = processusSterilisationService.findAll().stream()
                    .filter(p -> p.getDemandeSterilisation().getCodeDemande().equals("DEM-ECHEC-001"))
                    .findFirst()
                    .orElseThrow();

            processusSterilisationService.avancerProcessus(processus.getId()); // LAVAGE
            processusSterilisationService.avancerProcessus(processus.getId()); // CONDITIONNEMENT

            incidentSterilisationService.createIncident(
                    processus.getId(),
                    autoclave.getId(),
                    TypeIncidentSterilisation.EMBALLAGE_DECHIRE,
                    GraviteIncident.CRITIQUE,
                    "Emballage déchiré détecté avant passage à l'autoclave"
            );

            processusSterilisationService.mettreEnEchec(
                    processus.getId(),
                    "Échec du processus : emballage déchiré"
            );

        } catch (Exception ignored) {
        }
    }
}