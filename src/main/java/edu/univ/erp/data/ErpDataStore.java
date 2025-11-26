package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.univ.erp.domain.CourseSectionRow;
import java.util.ArrayList;
import java.util.List;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.EnrollmentRow;
import java.sql.Statement;
import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.domain.GradebookRow;
import java.util.Map;
import java.util.HashMap;
import edu.univ.erp.domain.Course; 
import edu.univ.erp.domain.InstructorInfo;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.StudentProfile;
import edu.univ.erp.domain.FinalGradeSummary;
import edu.univ.erp.domain.SectionDetails;

//handles all db queries for  ERP DB.
public class ErpDataStore {
    private final MySqlConnectionManager connectionManager;
    public ErpDataStore(){
        this.connectionManager=MySqlConnectionManager.getInstance();
    }
    
    public String getSetting(String key){//setting key
        String sql="SELECT `value` FROM `settings` WHERE `key`=?";
        // Get connection from  ERP pool
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setString(1, key);

            try (ResultSet rs=stmt.executeQuery()){
                if (rs.next()){
                    return rs.getString("value"); //string value of  setting, or null if not found
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null; // Setting not found or error
    }
    //complete course catalog for given term
    public List<CourseSectionRow> getCourseCatalog(String semester, int year){
        List<CourseSectionRow> catalog=new ArrayList<>();
        String sql="SELECT s.section_id, c.code, c.title, c.credits, s.capacity, s.day_time, s.room, " +
                " (SELECT username FROM auth_db.users_auth WHERE user_id=i.user_id) AS instructor_name " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id=c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id=i.user_id " +
                "WHERE s.semester=? AND s.year=?";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setString(1, semester);
            stmt.setInt(2, year);

            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    CourseSectionRow row=new CourseSectionRow(
                            rs.getInt("section_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getInt("capacity"),
                            rs.getString("instructor_name"), // Fetched from auth_db
                            rs.getString("day_time"),
                            rs.getString("room")
                    );
                    catalog.add(row);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return catalog; //list of CourseSectionRow objects
    }

    //checks if student is already enrolled in section
    public boolean isStudentAlreadyEnrolled(int studentId, int sectionId){
        String sql="SELECT 1 FROM enrollments WHERE student_id=? AND section_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs=stmt.executeQuery()){
                return rs.next(); // true if record was found
            }
        } catch (SQLException e){
            e.printStackTrace();
            return true; // error: they are enrolled if DB fails
        }
    }

    //details for single section, including  enrollment count
    public Section getSectionDetails(int sectionId){
        //get capacity and count of current enrollments
        String sql="SELECT s.capacity, COUNT(e.enrollment_id) AS current_enrollment " +
                "FROM sections s " +
                "LEFT JOIN enrollments e ON s.section_id=e.section_id " +
                "WHERE s.section_id=? " +
                "GROUP BY s.section_id, s.capacity";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1, sectionId);

            try (ResultSet rs=stmt.executeQuery()){
                if (rs.next()){
                    return new Section(
                            sectionId,
                            rs.getInt("capacity"),
                            rs.getInt("current_enrollment")
                    );
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null; // section not found
    }

     //creates new enrollment record for student in section.
    public boolean createEnrollment(int studentId, int sectionId){
        String sql="INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'Registered')";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);

            int rowsAffected=stmt.executeUpdate();
            return rowsAffected > 0; //true if  insert was successful

        } catch (SQLException e){
            e.printStackTrace();
            return false;//false if error
        }
    }
    public List<EnrollmentRow> getStudentRegistrations(int studentId){
        List<EnrollmentRow> registrations=new ArrayList<>();

        String sql="SELECT e.enrollment_id, c.code, c.title, c.credits, s.day_time, s.room, " +
                " s.capacity, " +
                " (SELECT username FROM auth_db.users_auth WHERE user_id=s.instructor_id) AS instructor_name " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id=s.section_id " +
                "JOIN courses c ON s.course_id=c.course_id " +
                "WHERE e.student_id=? AND e.status='Registered'";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, studentId);

            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    registrations.add(new EnrollmentRow(
                            rs.getInt("enrollment_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("instructor_name"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("credits"),
                            rs.getInt("capacity") // <-- PASS CAPACITY
                    ));
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return registrations;
    }


    //deletes an enrollment record from  db.
    public boolean deleteEnrollment(int enrollmentId){
        String sql="DELETE FROM enrollments WHERE enrollment_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, enrollmentId);
            int rowsAffected=stmt.executeUpdate();
            return rowsAffected > 0; //true if  deletion was successful

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    //fetches all grades for specific student.
    public List<GradeRow> getStudentGrades(int studentId){
        List<GradeRow> grades=new ArrayList<>();
        String sql="SELECT c.code, c.title, g.component, g.score, g.final_grade " +
                "FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id=e.enrollment_id " +
                "JOIN sections s ON e.section_id=s.section_id " +
                "JOIN courses c ON s.course_id=c.course_id " +
                "WHERE e.student_id=?";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, studentId);
            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    // use getObject() for score to handle potential nulls
                    Double score;
                    if (rs.getObject("score") != null){
                        score=rs.getDouble("score");
                    } else {
                        score=null;
                    }
                    grades.add(new GradeRow(
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("component"),
                            score,
                            rs.getString("final_grade")
                    ));
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return grades;
    }

    // all sections assigned to specific instructor
    public List<CourseSectionRow> getInstructorSections(int instructorId){
        List<CourseSectionRow> sections=new ArrayList<>();
        String sql="SELECT s.section_id, c.code, c.title, c.credits, s.capacity, s.day_time, s.room, " +
                " (SELECT username FROM auth_db.users_auth WHERE user_id=s.instructor_id) AS instructor_name " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id=c.course_id " +
                "WHERE s.instructor_id=?"; //  key difference is this WHERE clause

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, instructorId);

            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    sections.add(new CourseSectionRow(
                            rs.getInt("section_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getInt("capacity"),
                            rs.getString("instructor_name"),
                            rs.getString("day_time"),
                            rs.getString("room")
                    ));
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return sections;
    }

    //all students and their grades for some section.
    public List<GradebookRow> getGradebookForSection(int sectionId){
        List<GradebookRow> gradebook=new ArrayList<>();
        Map<Integer, GradebookRow> enrollmentMap=new HashMap<>();

        // get students
        String studentSql="SELECT e.enrollment_id, e.student_id, s.roll_no, " +
                " (SELECT username FROM auth_db.users_auth WHERE user_id=s.user_id) AS student_name " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id=s.user_id " +
                "WHERE e.section_id=? AND e.status='Registered'";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(studentSql)){

            stmt.setInt(1, sectionId);
            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    GradebookRow row=new GradebookRow(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("student_name")
                    );
                    gradebook.add(row);
                    enrollmentMap.put(row.getEnrollmentId(), row);
                }
            }
            //get grades
            String gradeSql="SELECT enrollment_id, component, score, final_grade FROM grades " +
                    "WHERE enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE section_id=?)";
            try (PreparedStatement gradeStmt=conn.prepareStatement(gradeSql)){
                gradeStmt.setInt(1, sectionId);
                try (ResultSet rs=gradeStmt.executeQuery()){
                    while (rs.next()){
                        int enrollmentId=rs.getInt("enrollment_id");
                        GradebookRow row=enrollmentMap.get(enrollmentId);
                        if (row == null) continue;
                        String component=rs.getString("component");
                        Double score;
                        if (rs.getObject("score") != null){
                            score=rs.getDouble("score");
                        } else {
                            score=null;
                        }
                        String finalGrade=rs.getString("final_grade");

                        switch (component){//hardcoded components
                            case "Quiz-1": row.setQuiz1(score); break;
                            case "Quiz-2": row.setQuiz2(score); break;
                            case "Project": row.setProject(score); break;
                            case "Midsem": row.setMidsem(score); break;
                            case "Endsem": row.setEndsem(score); break;
                            case "Final":
                                row.setFinalGrade(finalGrade);
                                row.setFinalScore(score); // store numeric score
                                break;
                        }
                    }
                }
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return gradebook;
    }

    // inserts/updates grade
    public void saveGrade(int enrollmentId, String component, Double score){
        // This query checks if row for this component already exists
        String checkSql="SELECT grade_id FROM grades WHERE enrollment_id=? AND component=?";
        String updateSql="UPDATE grades SET score=? WHERE grade_id=?";
        String insertSql="INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";

        try (Connection conn=connectionManager.getErpConnection()){

            Integer gradeId=null; // To store  ID of  existing grade, if any

            try (PreparedStatement checkStmt=conn.prepareStatement(checkSql)){
                checkStmt.setInt(1, enrollmentId);
                checkStmt.setString(2, component);
                try (ResultSet rs=checkStmt.executeQuery()){
                    if (rs.next()){
                        gradeId=rs.getInt("grade_id");
                    }
                }
            }

            if (gradeId != null){
                try (PreparedStatement updateStmt=conn.prepareStatement(updateSql)){
                    if (score == null){
                        updateStmt.setNull(1, java.sql.Types.DECIMAL);
                    } else {
                        updateStmt.setDouble(1, score);
                    }
                    updateStmt.setInt(2, gradeId);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt=conn.prepareStatement(insertSql)){
                    insertStmt.setInt(1, enrollmentId);
                    insertStmt.setString(2, component);
                    if (score == null){
                        insertStmt.setNull(3, java.sql.Types.DECIMAL);
                    } else {
                        insertStmt.setDouble(3, score);
                    }
                    insertStmt.executeUpdate();
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    //Checks if user is assigned instructor for section.
    public boolean isInstructorForSection(int instructorId, int sectionId){
        String sql="SELECT 1 FROM sections WHERE instructor_id=? AND section_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, instructorId);
            stmt.setInt(2, sectionId);

            try (ResultSet rs=stmt.executeQuery()){
                return rs.next(); // true if record was found
            }
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Saves *both*  final numeric score and  letter grade.
     */
    public void saveFinalGrade(int enrollmentId, String finalGrade, double finalScore){ // Added finalScore
        // This query now saves to score and final_grade
        String sql="INSERT INTO grades (enrollment_id, component, final_grade, score) " +
                "VALUES (?, 'Final', ?, ?) " + // Added ? for score
                "ON DUPLICATE KEY UPDATE final_grade=VALUES(final_grade), score=VALUES(score)"; // Also update score

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, enrollmentId);
            stmt.setString(2, finalGrade);
            stmt.setDouble(3, finalScore); // Pass  final score
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public boolean hasGrades(int enrollmentId){
        String sql="SELECT 1 FROM grades WHERE enrollment_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, enrollmentId);
            try (ResultSet rs=stmt.executeQuery()){
                return rs.next(); // true if any grade record was found
            }
        } catch (SQLException e){
            e.printStackTrace();
            return true; //  error: assume grades exist if DB fails
        }
    }

    //inserts new course in courses table
    public boolean createCourse(String code, String title, int credits){
        String sql="INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            return stmt.executeUpdate() > 0; //true if successful

        } catch (SQLException e){
            e.printStackTrace(); // error could be duplicate code
            return false;
        }
    }

    public void HelloWorld(int var){
        if (var == 1){
            System.out.println("Hello World");
        } else {
            System.out.println("No thank you man");
        }
    } //lol

    // list of instructors for dropdown menus
    public List<InstructorInfo> getAllInstructors(){
        List<InstructorInfo> instructors=new ArrayList<>();
        String sql = "SELECT i.user_id, u.username " +
                "FROM instructors i " +
                "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
                "ORDER BY u.username";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    instructors.add(new InstructorInfo(
                            rs.getInt("user_id"),
                            rs.getString("username")
                    ));
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return instructors;
    }

    //inserts new section in  sections table
    public boolean createSection(int courseId, Integer instructorId, String dayTime,
                                 String room, int capacity, String semester, int year){

        String sql="INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, courseId);
            if (instructorId == null){
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, instructorId);
            }
            stmt.setString(3, dayTime);
            stmt.setString(4, room);
            stmt.setInt(5, capacity);
            stmt.setString(6, semester);
            stmt.setInt(7, year);

            return stmt.executeUpdate() > 0; //true if successful

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    //list of all courses for dropdown menus
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT c.course_id, c.code, c.title, c.credits, " +
                "GROUP_CONCAT(DISTINCT u.username ORDER BY u.username SEPARATOR ', ') as instructors " +
                "FROM courses c " +
                "LEFT JOIN sections s ON c.course_id = s.course_id " +
                "LEFT JOIN auth_db.users_auth u ON s.instructor_id = u.user_id " +
                "GROUP BY c.course_id, c.code, c.title, c.credits " +
                "ORDER BY c.code";

        try (Connection conn = connectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                            rs.getInt("course_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getString("instructors") // Pass the new string
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
    //creates new student profile in erp_db.
    public boolean createStudentProfile(int userId, String rollNo, String program, int year){
        String sql="INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, userId);
            stmt.setString(2, rollNo);
            stmt.setString(3, program);
            stmt.setInt(4, year);
            return stmt.executeUpdate() > 0;//true if success

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    //create new instructor profile in  erp_db.
    public boolean createInstructorProfile(int userId, String department, String title){
        String sql="INSERT INTO instructors (user_id, department, title) VALUES (?, ?, ?)";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, userId);
            stmt.setString(2, department);
            stmt.setString(3, title);
            return stmt.executeUpdate() > 0; //true if success

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    //update value in 'settings' table.
    public boolean updateSetting(String key, String value){
        String sql="UPDATE settings SET `value`=? WHERE `key`=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setString(1, value);
            stmt.setString(2, key);
            return stmt.executeUpdate() > 0; //true if successful

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    //fetches student profile from students table.
    public StudentProfile getStudentProfile(int userId){
        String sql="SELECT roll_no, program, year FROM students WHERE user_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, userId);
            try (ResultSet rs=stmt.executeQuery()){
                if (rs.next()){
                    return new StudentProfile(
                            userId,
                            rs.getString("roll_no"),
                            rs.getString("program"),
                            rs.getInt("year")
                    );
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Deletes all grades for a specific enrollment.
     * Used before dropping a student to satisfy foreign key constraints.
     */
    public void deleteGrades(int enrollmentId) {
        String sql = "DELETE FROM grades WHERE enrollment_id = ?";
        try (Connection conn = connectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // list of final grades for profile
    public List<FinalGradeSummary> getFinalGrades(int studentId){
        List<FinalGradeSummary> grades=new ArrayList<>();
        String sql="SELECT c.code, c.title, g.final_grade " + //only grade
                "FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id=e.enrollment_id " +
                "JOIN sections s ON e.section_id=s.section_id " +
                "JOIN courses c ON s.course_id=c.course_id " +
                "WHERE e.student_id=? AND g.component='Final'";

        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1, studentId);
            try (ResultSet rs=stmt.executeQuery()){
                while (rs.next()){
                    grades.add(new FinalGradeSummary(
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("final_grade")
                    ));
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return grades;
    }
    //details for single section for editing
    public SectionDetails getFullSectionDetails(int sectionId){
        String sql="SELECT * FROM sections WHERE section_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, sectionId);
            try (ResultSet rs=stmt.executeQuery()){
                if (rs.next()){
                    return new SectionDetails(
                            rs.getInt("section_id"),
                            rs.getInt("course_id"),
                            (Integer) rs.getObject("instructor_id"), // Get as object for null
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity"),
                            rs.getString("semester"),
                            rs.getInt("year")
                    );
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }



    // updates existing section in  db4
    public boolean updateSection(int sectionId, Integer instructorId, String dayTime,
                                 String room, int capacity, String semester, int year){

        String sql="UPDATE sections SET instructor_id=?, day_time=?, room=?, " +
                "capacity=?, semester=?, year=? " +
                "WHERE section_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            if (instructorId == null){
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, instructorId);
            }
            stmt.setString(2, dayTime);
            stmt.setString(3, room);
            stmt.setInt(4, capacity);
            stmt.setString(5, semester);
            stmt.setInt(6, year);
            stmt.setInt(7, sectionId); // Set  WHERE clause

            return stmt.executeUpdate() > 0;//true is success

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    //check if course has any sections linked to it
    public boolean hasSections(int courseId){
        String sql="SELECT 1 FROM sections WHERE course_id=? LIMIT 1";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, courseId);
            try (ResultSet rs=stmt.executeQuery()){
                return rs.next(); // true if section was found
            }
        } catch (SQLException e){
            e.printStackTrace();
            return true; //  error: assume it has sections
        }
    }

    //delete course from courses table.
    public boolean deleteCourse(int courseId){
        String sql="DELETE FROM courses WHERE course_id=?";
        try (Connection conn=connectionManager.getErpConnection();
             PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0; //true if successful

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    //Checks if a section has any active enrollments.
    public boolean hasEnrollments(int sectionId) {
        String sql = "SELECT 1 FROM enrollments WHERE section_id = ?";
        try (Connection conn = connectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if at least one student is found
            }
        } catch (SQLException e) { e.printStackTrace(); return true; }
    }

     //Deletes a section from the database.
    public boolean deleteSection(int sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = connectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    //Fetches ALL sections for the management table.
    public List<CourseSectionRow> getAllSections() {
        List<CourseSectionRow> sections = new ArrayList<>();
        String sql = "SELECT s.section_id, c.code, c.title, c.credits, s.capacity, s.day_time, s.room, " +
                " (SELECT username FROM auth_db.users_auth WHERE user_id = s.instructor_id) AS instructor_name " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY c.code, s.section_id";

        try (Connection conn = connectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sections.add(new CourseSectionRow(
                        rs.getInt("section_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits"),
                        rs.getInt("capacity"),
                        rs.getString("instructor_name"),
                        rs.getString("day_time"),
                        rs.getString("room")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sections;
    }

}