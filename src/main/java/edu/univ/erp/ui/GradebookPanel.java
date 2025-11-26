package edu.univ.erp.ui;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.CourseSectionRow;
import edu.univ.erp.domain.GradebookRow;
import edu.univ.erp.service.InstructorService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

//The ui panel for an instructor to enter and save grades for section
public class GradebookPanel extends JPanel {
    private final UserSession session;
    private final CourseSectionRow section;
    private final ErpDataStore erpDataStore;
    private final InstructorService instructorService;

    private JTable gradebookTable;
    private GradebookTableModel tableModel;
    private List<GradebookRow> gradebookList;

    public GradebookPanel(UserSession session, CourseSectionRow section){
        this.session=session;
        this.section=section;
        this.erpDataStore=new ErpDataStore();
        this.instructorService=new InstructorService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // title
        JLabel titleLabel=new JLabel("Gradebook for: " + section.getCourseCode() + " - " + section.getCourseTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        // table
        JScrollPane scrollPane=createTablePanel();
        add(scrollPane, BorderLayout.CENTER);
        // action panel
        JPanel actionPanel=createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
        loadGradebookData();
    }

    private JScrollPane createTablePanel(){
        tableModel=new GradebookTableModel();
        gradebookTable=new JTable(tableModel);

        // column size
        TableColumn rollNoCol=gradebookTable.getColumnModel().getColumn(0);
        rollNoCol.setPreferredWidth(100);
        TableColumn nameCol=gradebookTable.getColumnModel().getColumn(1);
        nameCol.setPreferredWidth(250);

        return new JScrollPane(gradebookTable);
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // --- NEW BUTTON ---
        JButton statsButton = new JButton("Show Statistics");
        statsButton.setBackground(new Color(13, 110, 253)); // Blue
        statsButton.setForeground(Color.WHITE);
        statsButton.addActionListener(e -> onShowStats());
        panel.add(statsButton);
        // ------------------

        JButton calcFinalButton = new JButton("Calculate Final Grades");
        calcFinalButton.addActionListener(e -> onCalculateFinal());

        JButton saveButton = new JButton("Save All Grades");
        saveButton.setBackground(new Color(25, 135, 84)); // Green
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> onSaveGrades());

        panel.add(calcFinalButton);
        panel.add(saveButton);
        return panel;
    }
    private void onShowStats() {
        // 1. Get the report string from the service
        String stats = instructorService.getSectionStatistics(gradebookList);

        // 2. Put it in a text area so we can use a monospaced font (for alignment)
        JTextArea textArea = new JTextArea(stats);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setOpaque(false); // Make it look like a label

        // 3. Show the dialog
        JOptionPane.showMessageDialog(this, textArea, "Class Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadGradebookData(){
        // get fresh data
        gradebookList=erpDataStore.getGradebookForSection(section.getSectionId());
        // use this new list
        tableModel.setGradebookData(gradebookList);
    }

    private void onSaveGrades(){
        if (gradebookTable.isEditing()){
            gradebookTable.getCellEditor().stopCellEditing();
        }
        String message=instructorService.saveAllGrades(session.getUserId(),section.getSectionId(), gradebookList);
        JOptionPane.showMessageDialog(this, message, "Save Status", JOptionPane.INFORMATION_MESSAGE);
        loadGradebookData(); // refresh data from db
    }

    private void onCalculateFinal(){
        if (gradebookTable.isEditing()) gradebookTable.getCellEditor().stopCellEditing();
        String message=instructorService.calculateFinalGrades(session.getUserId(), section.getSectionId(), gradebookList);
        JOptionPane.showMessageDialog(this, message);
        loadGradebookData();
    }

    class GradebookTableModel extends javax.swing.table.AbstractTableModel {
        private final String[] columnNames={"Roll No", "Name", "Quiz-1 (10%)", "Quiz-2 (10%)", "Project (20%)", "Midsem (25%)", "Endsem (35%)", "Final"};
        private List<GradebookRow> data;
        public void setGradebookData(List<GradebookRow> data){
            this.data=data;//get data
            fireTableDataChanged(); //refresh
        }

        //overriding
        @Override
        public int getRowCount(){
            if (data == null){
                return 0;
            }
            return data.size();
        }
        @Override
        public int getColumnCount(){ return columnNames.length; }

        @Override
        public String getColumnName(int col){ return columnNames[col]; }

        @Override
        public boolean isCellEditable(int row, int col){
            return col >= 2 && col <= 6; // col 2 to 6 are editable
        }

        @Override
        public Class<?> getColumnClass(int col){
            if (col < 2 || col == 7) return String.class;
            return Double.class;
        }

        @Override
        public Object getValueAt(int row, int col){
            if (data ==null|| row >=data.size()) return null;
            GradebookRow r=data.get(row);
            switch(col){
                case 0:return r.getRollNo();
                case 1:return r.getStudentName();
                case 2:return r.getQuiz1();
                case 3:return r.getQuiz2();
                case 4:return r.getProject();
                case 5:return r.getMidsem();
                case 6:return r.getEndsem();
                case 7:return r.getFinalGrade();
                default: return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (data == null || row >= data.size()) return;
            GradebookRow r = data.get(row);
            Double score = null;

            // (Keep your parsing logic here: instanceof Double, String, Number)
            if (aValue instanceof Double) score=(Double) aValue;
            else if (aValue instanceof String){
                try { score=java.lang.Double.parseDouble((String) aValue); } catch(Exception e){}
            }
            else if (aValue instanceof Number) score=((Number) aValue).doubleValue();

            switch (col){
                case 2: r.setQuiz1(score); break;
                case 3: r.setQuiz2(score); break;
                case 4: r.setProject(score); break;
                case 5: r.setMidsem(score); break;
                case 6: r.setEndsem(score); break;
            }
        }
    }
    private void onDropStudent() {
        int selectedRow = gradebookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student row to drop.",
                    "No Student Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the student data from the list
        GradebookRow selectedStudent = gradebookList.get(selectedRow);

        // Confirm deletion
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + selectedStudent.getStudentName() + " (" + selectedStudent.getRollNo() + ")?\n" +
                        "This will remove them and ALL their grades from this section.",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // Call the service
            String message = instructorService.dropStudent(
                    session.getUserId(),
                    section.getSectionId(),
                    selectedStudent.getEnrollmentId()
            );

            JOptionPane.showMessageDialog(this, message);

            // Refresh the table to show they are gone
            if (message.startsWith("Success")) {
                loadGradebookData();
            }
        }
    }
}