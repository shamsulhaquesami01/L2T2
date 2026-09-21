
package other_section;
import java.util.*;

// --- Colleagues & Mediator Interfaces ---

interface EmergencyMediator {
    void registerDoctor(Doctor doctor);
    void registerPathologyLab(PathologyLab lab);
    void registerRadiologyUnit(RadiologyUnit unit);
    void registerPatient(Patient patient);

    void requestInvestigation(String patientId, List<String> tests);
    void submitResult(String patientId, String testType, String result);
}

abstract class MedicalUnit {
    protected EmergencyMediator mediator;

    public MedicalUnit(EmergencyMediator mediator) {
        this.mediator = mediator;
    }
}

// --- Colleague Implementations ---

class Doctor extends MedicalUnit {
    public Doctor(EmergencyMediator mediator) {
        super(mediator);
    }

    public void examineAndRequest(String patientId, List<String> tests) {
        mediator.requestInvestigation(patientId, tests);
    }

    public void receiveUrgentAlert(String patientId, String message) {
        System.out.printf("URGENT notification sent to Doctor: [%s for %s]%n", message, patientId);
    }

    public void receiveCompleteResults(String patientId, Map<String, String> results) {
        System.out.printf("Complete results sent to Doctor for %s: %s%n", patientId, results);
    }
}

class Patient {
    private final String id;

    public Patient(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void receiveUrgentAlert(String message) {
        System.out.printf("URGENT notification sent to Patient %s: [%s]%n", id, message);
    }

    public void receiveCompleteResults(Map<String, String> results) {
        System.out.printf("Complete results sent to Patient %s: %s%n", id, results);
    }
}

class PathologyLab extends MedicalUnit {
    public PathologyLab(EmergencyMediator mediator) {
        super(mediator);
    }

    public void performTest(String patientId, String result) {
        // Simulates lab execution and submits result back to the mediator
        mediator.submitResult(patientId, "Pathology", result);
    }
}

class RadiologyUnit extends MedicalUnit {
    public RadiologyUnit(EmergencyMediator mediator) {
        super(mediator);
    }

    public void performInvestigation(String patientId, String result) {
        // Simulates investigation execution and submits result back to the mediator
        mediator.submitResult(patientId, "Radiology", result);
    }
}

// --- Per-Patient State Tracker ---

class PatientRecord {
    private final Set<String> requestedTests = new HashSet<>();
    private final Map<String, String> completedResults = new HashMap<>();

    public PatientRecord(List<String> tests) {
        this.requestedTests.addAll(tests);
    }

    public void recordResult(String testType, String result) {
        completedResults.put(testType, result);
    }

    public boolean isAllCompleted() {
        return completedResults.keySet().containsAll(requestedTests);
    }

    public Map<String, String> getCompletedResults() {
        return Collections.unmodifiableMap(completedResults);
    }
}

// --- Concrete Mediator ---

class EmergencyCenter implements EmergencyMediator {
    private Doctor doctor;
    private PathologyLab pathologyLab;
    private RadiologyUnit radiologyUnit;
    private final Map<String, Patient> patients = new HashMap<>();

    // Tracks workflow states per patient ID
    private final Map<String, PatientRecord> patientRecords = new HashMap<>();

    @Override
    public void registerDoctor(Doctor doctor) { this.doctor = doctor; }

    @Override
    public void registerPathologyLab(PathologyLab lab) { this.pathologyLab = lab; }

    @Override
    public void registerRadiologyUnit(RadiologyUnit unit) { this.radiologyUnit = unit; }

    @Override
    public void registerPatient(Patient patient) {
        patients.put(patient.getId(), patient);
    }

    @Override
    public void requestInvestigation(String patientId, List<String> tests) {
        patientRecords.put(patientId, new PatientRecord(tests));

        for (String test : tests) {
            if ("Pathology".equalsIgnoreCase(test)) {
                System.out.printf("Pathology test requested for Patient %s.%n", patientId);
            } else if ("Radiology".equalsIgnoreCase(test)) {
                System.out.printf("Radiology investigation requested for Patient %s.%n", patientId);
            }
        }
    }

    @Override
    public void submitResult(String patientId, String testType, String result) {
        PatientRecord record = patientRecords.get(patientId);
        if (record == null) {
            System.err.println("No record found for patient: " + patientId);
            return;
        }

        record.recordResult(testType, result);

        // Display test reception status
        if ("CRITICAL".equalsIgnoreCase(result)) {
            System.out.printf("Critical pathology result received for Patient %s.%n", patientId);
        } else {
            System.out.printf("%s result received for Patient %s.%n", testType, patientId);
        }

        // Urgent result handling: check immediate triggers
        boolean isUrgent = ("Pathology".equalsIgnoreCase(testType) && "CRITICAL".equalsIgnoreCase(result))
                || ("Radiology".equalsIgnoreCase(testType) && "NOT OK".equalsIgnoreCase(result));

        if (isUrgent) {
            String alertMessage = testType + " result is " + result;
            if (doctor != null) doctor.receiveUrgentAlert(patientId, alertMessage);
            Patient p = patients.get(patientId);
            if (p != null) p.receiveUrgentAlert(alertMessage);
        }

        // Workflow coordination: check if all requested tests have finished
        if (record.isAllCompleted()) {
            System.out.printf("All requested investigations completed for Patient %s.%n", patientId);
            if (doctor != null) doctor.receiveCompleteResults(patientId, record.getCompletedResults());
            Patient p = patients.get(patientId);
            if (p != null) p.receiveCompleteResults(record.getCompletedResults());
        }
    }
}

// --- Main Execution ---

public class C1_online {
    public static void main(String[] args) {
        EmergencyCenter center = new EmergencyCenter();

        Doctor doctor = new Doctor(center);
        PathologyLab pathologyLab = new PathologyLab(center);
        RadiologyUnit radiologyUnit = new RadiologyUnit(center);
        Patient p101 = new Patient("P101");

        center.registerDoctor(doctor);
        center.registerPathologyLab(pathologyLab);
        center.registerRadiologyUnit(radiologyUnit);
        center.registerPatient(p101);

        // Doctor requests both investigations
        doctor.examineAndRequest("P101", List.of("Pathology", "Radiology"));

        // Pathology finishes first with CRITICAL
        pathologyLab.performTest("P101", "CRITICAL");

        // Radiology finishes later with OK
        radiologyUnit.performInvestigation("P101", "OK");
    }
}