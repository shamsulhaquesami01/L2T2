package OFFLINE;

import java.util.HashMap;
import java.util.Map;

interface ResultCoordinator {
    void registerStudent(Student student);

    void submitDepartmentConfirmation(String studentId);

    void requestOfficeOrder(String studentId);

    void requestTestimonial(String studentId);

    void requestCertificate(String studentId);
}

class BUETResultCoordinator implements ResultCoordinator {
    private final Map<String, Student> studentList = new HashMap<>();
    private final Map<String, StudentStatus> statusList = new HashMap<>();

    @Override
    public void registerStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }

        String studentId = student.getStudentId();

        if (studentList.containsKey(studentId)) {
            throw new IllegalArgumentException(
                    "Student is already registered: " + studentId);
        }

        studentList.put(studentId, student);
        statusList.put(studentId, new StudentStatus());

        System.out.println(
                "Student " + studentId +
                        " successfully registered with the Coordinator.");
    }

    private StudentStatus getStatus(String studentId) {
        StudentStatus status = statusList.get(studentId);
        if (status == null) {
            throw new IllegalArgumentException("Student is not registered: " + studentId);
        }
        return status;
    }

    private Student getStudent(String studentId) {
        Student student = studentList.get(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student is not registered: " + studentId);
        }
        return student;
    }

    @Override
    public void submitDepartmentConfirmation(String studentId) {
        StudentStatus status = getStatus(studentId);
        if (status.isDepartmentConfirmation()) {
            System.out.println("Coordinator: Department Confirmation is already approved for: " + studentId);
            return;
        }

        status.setDepartmentConfirmation(true);
        System.out.println("Coordinator: Department Confirmation approved for: " + studentId);
    }

    @Override
    public void requestOfficeOrder(String studentId) {
        StudentStatus status = getStatus(studentId);
        Student student = getStudent(studentId);

        if (!status.isDepartmentConfirmation()) {
            System.out.println(
                    "Coordinator: Rejected. Cannot issue the Office Order because Department Confirmation is missing for: "
                            + studentId);
            return;
        }

        if (status.isOfficeOrder()) {
            System.out.println("Coordinator: Rejected. Office Order has already been issued for: " + studentId);
            return;
        }

        status.setOfficeOrder(true);
        System.out.println(
                "Coordinator: Final-result publication Office Order issued for: " + studentId);
        student.receiveNotification("Your Office Order has been published.");
    }

    @Override
    public void requestTestimonial(String studentId) {
        StudentStatus status = getStatus(studentId);
        Student student = getStudent(studentId);

        if (!status.isOfficeOrder()) {
            System.out.println(
                    "Coordinator: Rejected. Cannot issue the Testimonial because the Office Order is missing for: "
                            + studentId);
            return;
        }

        if (status.isTestimonial()) {
            System.out.println("Coordinator: Rejected. Testimonial has already been issued for: " + studentId);
            return;
        }

        status.setTestimonial(true);
        System.out.println("Coordinator: Testimonial issued for: " + studentId);
        student.receiveNotification("Your Testimonial has been published.");
    }

    @Override
    public void requestCertificate(String studentId) {
        StudentStatus status = getStatus(studentId);
        Student student = getStudent(studentId);

        if (!status.isTestimonial()) {
            System.out.println(
                    "Coordinator: Rejected. Cannot issue the Certificate and Academic Transcript because the Testimonial is missing for: "
                            + studentId);
            return;
        }

        if (status.isCertificate() || status.isTranscript()) {
            System.out.println(
                    "Coordinator: Rejected. Certificate and Academic Transcript have already been issued for: "
                            + studentId);
            return;
        }

        status.setCertificate(true);
        status.setTranscript(true);
        System.out.println(
                "Coordinator: Certificate and Academic Transcript issued for: " + studentId);
        student.receiveNotification("Your Certificate and Academic Transcript have been published.");
    }

    public void displayCurrentStatus(String studentId) {
        StudentStatus status = getStatus(studentId);

        System.out.println("Student ID: " + studentId);
        System.out.println("Current Processing Status");
        System.out.println("Department Confirmation issued: " + status.isDepartmentConfirmation());
        System.out.println("Office Order issued: " + status.isOfficeOrder());
        System.out.println("Testimonial published: " + status.isTestimonial());
        System.out.println("Certificate published: " + status.isCertificate());
        System.out.println("Transcript published: " + status.isTranscript());
    }
}

abstract class Admin {
    protected final ResultCoordinator resultCoordinator;

    public Admin(ResultCoordinator resultCoordinator) {
        if (resultCoordinator == null) {
            throw new IllegalArgumentException("Result Coordinator cannot be null.");
        }
        this.resultCoordinator = resultCoordinator;
    }
}

class DepartmentOffice extends Admin {
    public DepartmentOffice(ResultCoordinator resultCoordinator) {
        super(resultCoordinator);

        System.out.println(
                "Department Office successfully registered with the Coordinator.");
    }

    public void submitDepartmentConfirmation(String studentId) {
        System.out.println("Department Office: Submitting confirmation for " + studentId);
        resultCoordinator.submitDepartmentConfirmation(studentId);
    }
}

class ControllerOffice extends Admin {
    public ControllerOffice(ResultCoordinator resultCoordinator) {
        super(resultCoordinator);

        System.out.println(
                "Controller of Examinations successfully registered with the Coordinator.");
    }

    public void requestOfficeOrder(String studentId) {
        System.out.println("Controller: Attempting to issue Office Order for " + studentId);
        resultCoordinator.requestOfficeOrder(studentId);
    }

    public void requestCertificate(String studentId) {
        System.out.println("Controller: Attempting to issue Certificate and Academic Transcript for " + studentId);
        resultCoordinator.requestCertificate(studentId);
    }
}

class DSW extends Admin {
    public DSW(ResultCoordinator resultCoordinator) {
        super(resultCoordinator);

        System.out.println(
                "DSW successfully registered with the Coordinator.");
    }

    public void requestTestimonial(String studentId) {
        System.out.println("DSW: Attempting to issue Testimonial for " + studentId);
        resultCoordinator.requestTestimonial(studentId);
    }
}

class Student {
    private final String studentId;

    public Student(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID cannot be null or blank.");
        }
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void receiveNotification(String message) {
        System.out.println("Notification to " + studentId + ": " + message);
    }
}

class StudentStatus {
    private boolean departmentConfirmation;
    private boolean officeOrder;
    private boolean testimonial;
    private boolean certificate;
    private boolean transcript;

    public boolean isDepartmentConfirmation() {
        return departmentConfirmation;
    }

    public boolean isOfficeOrder() {
        return officeOrder;
    }

    public boolean isTestimonial() {
        return testimonial;
    }

    public boolean isCertificate() {
        return certificate;
    }

    public boolean isTranscript() {
        return transcript;
    }

    public void setDepartmentConfirmation(boolean departmentConfirmation) {
        this.departmentConfirmation = departmentConfirmation;
    }

    public void setOfficeOrder(boolean officeOrder) {
        this.officeOrder = officeOrder;
    }

    public void setTestimonial(boolean testimonial) {
        this.testimonial = testimonial;
    }

    public void setCertificate(boolean certificate) {
        this.certificate = certificate;
    }

    public void setTranscript(boolean transcript) {
        this.transcript = transcript;
    }
}

public class BUETResultSystem {
    public static void main(String[] args) {
        System.out.println("--- SYSTEM INITIALIZATION ---");

        // Mediator
        BUETResultCoordinator coordinator = new BUETResultCoordinator();

        // Create participants
        DepartmentOffice departmentOffice = new DepartmentOffice(coordinator);
        ControllerOffice controllerOffice = new ControllerOffice(coordinator);
        DSW dsw = new DSW(coordinator);
        Student student = new Student("2305055");

        coordinator.registerStudent(student);

        System.out.println(" 1. Attempt to publish a result before departmental confirmation ---");
        controllerOffice.requestOfficeOrder(student.getStudentId());
        System.out.println();

        System.out.println("2. Submission of departmental confirmation ---");
        departmentOffice.submitDepartmentConfirmation(student.getStudentId());
        System.out.println();

        System.out.println(" 3. An early attempt to issue the certificate or transcript ---");
        controllerOffice.requestCertificate(student.getStudentId());
        System.out.println();

        System.out.println(" 4. Issuance of the final-result office order ---");
        controllerOffice.requestOfficeOrder(student.getStudentId());
        System.out.println();

        System.out.println(" 5. Issuance of the testimonial ---");
        dsw.requestTestimonial(student.getStudentId());
        System.out.println();

        System.out.println(" 6. Issuance of the certificate and transcript ---");
        controllerOffice.requestCertificate(student.getStudentId());
        System.out.println();

        System.out.println(" 7. Student notifications and the final status ---");
        coordinator.displayCurrentStatus(student.getStudentId());
    }
}