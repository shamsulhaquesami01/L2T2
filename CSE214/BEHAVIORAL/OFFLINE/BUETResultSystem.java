package OFFLINE;

import java.util.HashMap;
import java.util.Map;

interface ResultCoOrdinator{
    void submitDepartmentConfirmatrion(String StudentId);
    void requestOfficeOrder(String StudentId);
    void resuestTestimonial(String StudentId);
    void requestCertificate(String StudentId);
    void registerStudent(Student student);
}
class BUETResultCoOrdinator implements ResultCoOrdinator{

    private Map<String,Student> studentList = new HashMap<>();
    private Map<String, StudentStatus> statusList = new HashMap<>();

    @Override
    public void registerStudent(Student student){
        studentList.put(student.getStudentId(), student);
        statusList.put(student.getStudentId(), new StudentStatus());
    }


    @Override
    public void submitDepartmentConfirmatrion(String StudentId) {
        StudentStatus status = statusList.get(StudentId);
        status.deptconfirm=true;
        System.out.println("Coordinator: Department Confirmation Approved for: "+StudentId);
    }

    @Override
    public void requestOfficeOrder(String StudentId) {
        StudentStatus status = statusList.get(StudentId);
        Student student = studentList.get(StudentId);
        if(status.deptconfirm){
            status.officeorder=true;
            System.out.println("Coordinator: Office Order issued for: "+StudentId);
            student.receivenotification("Your Office Order has been published");
        }
        else{
              System.out.println("Coordinator: Rejected.Can't Issue Office order as Dept Confirmation is missing for "+StudentId);
        }
    }

    @Override
    public void resuestTestimonial(String StudentId) {
        StudentStatus status = statusList.get(StudentId);
        Student student = studentList.get(StudentId);
        if(status.officeorder){
            status.testimonial=true;
            System.out.println("Coordinator: Testimonial issued for: "+StudentId);
            student.receivenotification("Your Testimonial has been published");
        }
        else{
              System.out.println("Coordinator: Rejected.Can't Issue Testimonial as Office Order is missing for "+StudentId);
        }
    }

    @Override
    public void requestCertificate(String StudentId) {
           StudentStatus status = statusList.get(StudentId);
            Student student = studentList.get(StudentId);
        if(status.testimonial){
            status.certifiate=true;
            System.out.println("Coordinator: Certificate issued for: "+StudentId);
            student.receivenotification("Your Certificate has been published");
        }
        else{
              System.out.println("Coordinator: Rejected.Can't Issue Certificate as Testimonial is missing for "+StudentId);
        }
    }
    void displayCurrentStatus(String StudentId){
        System.out.println("Student Id :"+StudentId);
        StudentStatus status=statusList.get(StudentId);
        System.out.println("The Current Status of this student");
        System.out.println("Department Confirmaion issued: "+status.deptconfirm);
        System.out.println("Office Order issued: "+status.officeorder);
        System.out.println("Testiomnial published: "+status.testimonial);
        System.out.println("Certificate published: "+status.certifiate);

    }
}


abstract class Admin{
    protected ResultCoOrdinator resultCoOrdinator;
    public Admin(ResultCoOrdinator resultCoOrdinator){
        this.resultCoOrdinator = resultCoOrdinator;
    }
}

class DepartmentOffice extends Admin{
    public DepartmentOffice(ResultCoOrdinator resultCoOrdinator){
        super(resultCoOrdinator);
    }

    void submitDepartmentConfirmatrion(String StudentId) {
        System.out.println("Dept Office: SUbmiiting request for "+ StudentId);
        resultCoOrdinator.submitDepartmentConfirmatrion(StudentId);
    }

}
class ControllerOffice extends Admin{
    public ControllerOffice(ResultCoOrdinator resultCoOrdinator){
        super(resultCoOrdinator);
    }

    void requestOfficeOrder(String StudentId) {
        System.out.println("Controller: Attempting to issue office order for "+ StudentId);
        resultCoOrdinator.requestOfficeOrder(StudentId);
    }

    void requestCertificate(String StudentId) {
        System.out.println("Cotnroller: ATtempting to issue certificate for"+ StudentId);
        resultCoOrdinator.requestCertificate(StudentId);
    }
}
class DSW extends Admin{
    public DSW(ResultCoOrdinator resultCoOrdinator){
        super(resultCoOrdinator);
    }
    void requestTestimonial(String StudentId){
        System.out.println("DSW: ATtempting to issue tetimonial for"+ StudentId);
        resultCoOrdinator.resuestTestimonial(StudentId);
    }
}

class Student extends Admin{
    private String StudentId;
    public Student(ResultCoOrdinator resultCoOrdinator, String StudentId){
        super(resultCoOrdinator);
        this.StudentId=StudentId;
        resultCoOrdinator.registerStudent(this);
    }
    public String getStudentId(){
        return StudentId;
    }
    public void receivenotification(String msg){
     System.out.println("Notification to "+ StudentId+" :"+msg);
    
    }
}
class StudentStatus{
    boolean deptconfirm=false;
    boolean officeorder=false;
    boolean testimonial=false;
    boolean certifiate=false;
}

public class BUETResultSystem {
    public static void main(String[] args) {
        
        System.out.println("--- SYSTEM INITIALIZATION ---");
        // 1. Create the central coordinator (Mediator)
        BUETResultCoOrdinator coordinator = new BUETResultCoOrdinator();

        // 2. Create the offices, passing the coordinator to them
        DepartmentOffice deptOffice = new DepartmentOffice(coordinator);
        ControllerOffice controllerOffice = new ControllerOffice(coordinator);
        DSW dswOffice = new DSW(coordinator);

        // 3. Register a student in the system
        Student student = new Student(coordinator, "2305055");
        System.out.println("System: Offices and Student " + student.getStudentId() + " successfully registered.\n");


        // --- DEMONSTRATING THE REQUIRED SEQUENCE ---

        System.out.println("--- 1. Attempt to publish a result before departmental confirmation ---");
        // Controller attempts to issue the office order too early
        controllerOffice.requestOfficeOrder(student.getStudentId());
        System.out.println();

        System.out.println("--- 2. Submission of departmental confirmation ---");
        // Department correctly submits confirmation
        deptOffice.submitDepartmentConfirmatrion(student.getStudentId());
        System.out.println();

        System.out.println("--- 3. An early attempt to issue the certificate or transcript ---");
        // Controller attempts to jump to the final step before the DSW testimonial is issued
        controllerOffice.requestCertificate(student.getStudentId());
        System.out.println();

        System.out.println("--- 4. Issuance of the final-result office order ---");
        // Controller requests the office order again (this time it should succeed)
        controllerOffice.requestOfficeOrder(student.getStudentId());
        System.out.println();

        System.out.println("--- 5. Issuance of the testimonial ---");
        // DSW issues the testimonial (this requires the office order to be true)
        dswOffice.requestTestimonial(student.getStudentId());
        System.out.println();

        System.out.println("--- 6. Issuance of the certificate and transcript ---");
        // Controller finally issues the certificate (requires testimonial to be true)
        controllerOffice.requestCertificate(student.getStudentId());
        System.out.println();

        System.out.println("--- 7. Student notifications and the final status ---");
        // You can implement a displayStatus(studentId) method in your coordinator 
        // to print the final boolean states of the StudentStatus object here.
        coordinator.displayCurrentStatus(student.getStudentId());
    }
}