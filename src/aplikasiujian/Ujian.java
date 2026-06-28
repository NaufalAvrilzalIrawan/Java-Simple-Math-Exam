/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasiujian;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 *
 * @author user
 */
public class Ujian extends javax.swing.JFrame {

    // Global variables for exam logic
    private int categoryId;
    private String categoryName;
    private int userId;
    private String username;
    private String role;
    
    private JLabel[] questionLabels;
    private JTextField[] answerFields;
    private int[] correctAnswers = new int[10];
    private String[] questionTexts = new String[10];
    
    private Timer examTimer;
    private int timeRemaining = 600; // 10 minutes in seconds
    private LocalDateTime startTime;

    /**
     * Default constructor (Kept for NetBeans GUI Builder compatibility)
     */
    public Ujian() {
        initComponents();
        
        this.setLocationRelativeTo(null);
    }

    /**
     * Custom constructor to dynamically load the exam category and user
     */
    public Ujian(int categoryId, String categoryName, int userId, String username, String role) {
        initComponents();
        
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.userId = userId;
        this.username = username;
        this.role = role;
        System.out.println(userId + " " + username + " " + role);
        
        initCustomLogic();
        this.setLocationRelativeTo(null);
    }

    /**
     * Initialize custom logic like arrays, timer, and generating questions
     */
    private void initCustomLogic() {
        questionLabels = new JLabel[]{lblSoal1, lblSoal2, lblSoal3, lblSoal4, lblSoal5, lblSoal6, lblSoal7, lblSoal8, lblSoal9, lblSoal10};
        answerFields = new JTextField[]{txtJwb1, txtJwb2, txtJwb3, txtJwb4, txtJwb5, txtJwb6, txtJwb7, txtJwb8, txtJwb9, txtJwb10};
        
        lblJdl.setText(categoryName.toUpperCase());
        lblUser.setText("Siswa: " + username);
        
        startTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        lblStart.setText("Waktu Mulai: " + startTime.format(formatter));
        
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitExam();
            }
        });

        generateQuestions();
        startTimer();
    }

    /**
     * Generates 10 random math questions based on the selected category
     */
    private void generateQuestions() {
        Random rand = new Random();
        
        for (int i = 0; i < 10; i++) {
            int num1 = 0, num2 = 0, answer = 0;
            String operator = "";
            
            // 1: Addition, 2: Subtraction, 3: Multiplication, 4: Division
            switch (categoryId) {
                case 1:
                    num1 = rand.nextInt(100) + 1;
                    num2 = rand.nextInt(100) + 1;
                    answer = num1 + num2;
                    operator = "+";
                    break;
                case 2:
                    num1 = rand.nextInt(100) + 50;
                    num2 = rand.nextInt(num1) + 1;
                    answer = num1 - num2;
                    operator = "-";
                    break;
                case 3:
                    num1 = rand.nextInt(20) + 1;
                    num2 = rand.nextInt(20) + 1;
                    answer = num1 * num2;
                    operator = "x";
                    break;
                case 4:
                    num2 = rand.nextInt(20) + 1;
                    answer = rand.nextInt(20) + 1;
                    num1 = num2 * answer;
                    operator = "/";
                    break;
            }
            
            correctAnswers[i] = answer;
            questionTexts[i] = num1 + " " + operator + " " + num2;
            questionLabels[i].setText(questionTexts[i]);
            answerFields[i].setText("");
        }
    }

    /**
     * Starts the 10-minute countdown timer
     */
    private void startTimer() {
        examTimer = new Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                timeRemaining--;
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                
                lblTimer.setText(String.format("Timer: %02d:%02d", minutes, seconds));
                
                if (timeRemaining <= 0) {
                    examTimer.stop();
                    JOptionPane.showMessageDialog(null, "Waktu habis! Jawaban dikumpulkan.");
                    submitExam();
                }
            }
        });
        examTimer.start();
    }

    /**
     * Validates answers, calculates score, and saves to the database
     */
    private void submitExam() {
        examTimer.stop();
        btnSubmit.setEnabled(false);
        
        int correctCount = 0;
        int[] studentAnswers = new int[10];
        boolean[] isCorrectArray = new boolean[10];
        
        for (int i = 0; i < 10; i++) {
            int studentAns = 0;
            try {
                if (!answerFields[i].getText().trim().isEmpty()) {
                    studentAns = Integer.parseInt(answerFields[i].getText().trim());
                }
            } catch (NumberFormatException ex) {
                // Ignore invalid inputs (treat as 0/wrong)
            }
            
            studentAnswers[i] = studentAns;
            
            if (studentAns == correctAnswers[i]) {
                correctCount++;
                isCorrectArray[i] = true;
            } else {
                isCorrectArray[i] = false;
            }
        }
        
        int finalScore = correctCount * 10; // Total 100
        LocalDateTime endTime = LocalDateTime.now();
        int durationSeconds = 600 - timeRemaining; 
        
        saveToDatabase(finalScore, endTime, durationSeconds, studentAnswers, isCorrectArray);
    }

    /**
     * Saves the final score to exam_results and specific questions to exam_details
     */
    private void saveToDatabase(int finalScore, LocalDateTime endTime, int durationSeconds, int[] studentAnswers, boolean[] isCorrectArray) {
        Connection conn = null;
        PreparedStatement pstResult = null;
        PreparedStatement pstDetail = null;
        ResultSet rsKeys = null;
        System.out.println("User ID: " + userId + " Category ID: " + categoryId);
        
        try {
            conn = DatabaseConnection1.getConnection();
            conn.setAutoCommit(false);
            
            String sqlResult = "INSERT INTO exam_results (user_id, category_id, score, total_questions, exam_date, start_time, end_time, duration_seconds) "
                             + "VALUES (?, ?, ?, 10, CURRENT_DATE, ?, ?, ?)";
            
            pstResult = conn.prepareStatement(sqlResult, Statement.RETURN_GENERATED_KEYS);
            pstResult.setInt(1, userId);
            pstResult.setInt(2, categoryId);
            pstResult.setInt(3, finalScore);
            pstResult.setObject(4, startTime);
            pstResult.setObject(5, endTime);
            pstResult.setInt(6, durationSeconds);
            
            pstResult.executeUpdate();
            
            rsKeys = pstResult.getGeneratedKeys();
            int examResultId = 0;
            if (rsKeys.next()) {
                examResultId = rsKeys.getInt(1);
            }

            String sqlDetail = "INSERT INTO exam_details (exam_result_id, question_text, student_answer, correct_answer, is_correct) "
                             + "VALUES (?, ?, ?, ?, ?)";
            pstDetail = conn.prepareStatement(sqlDetail);
            
            for (int i = 0; i < 10; i++) {
                pstDetail.setInt(1, examResultId);
                pstDetail.setString(2, questionTexts[i]);
                pstDetail.setInt(3, studentAnswers[i]);
                pstDetail.setInt(4, correctAnswers[i]);
                pstDetail.setBoolean(5, isCorrectArray[i]);
                pstDetail.addBatch();
            }
            pstDetail.executeBatch();
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, "Exam submitted!\nYour Score: " + finalScore);
            this.dispose();
            
            Kategori kategoriForm = new Kategori(userId, username, role);
            kategoriForm.setVisible(true);
            System.out.println("Redirecting to Category Menu...");
            
        } catch (SQLException ex) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rsKeys != null) rsKeys.close();
                if (pstResult != null) pstResult.close();
                if (pstDetail != null) pstDetail.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblTimer = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblJml = new javax.swing.JLabel();
        lblStart = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        lblSoal1 = new javax.swing.JLabel();
        txtJwb1 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        lblSoal2 = new javax.swing.JLabel();
        txtJwb2 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        lblSoal3 = new javax.swing.JLabel();
        txtJwb3 = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        lblSoal5 = new javax.swing.JLabel();
        txtJwb5 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        lblSoal6 = new javax.swing.JLabel();
        txtJwb6 = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        lblSoal4 = new javax.swing.JLabel();
        txtJwb4 = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        lblSoal7 = new javax.swing.JLabel();
        txtJwb7 = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        lblSoal8 = new javax.swing.JLabel();
        txtJwb8 = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        lblSoal9 = new javax.swing.JLabel();
        txtJwb9 = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        lblSoal10 = new javax.swing.JLabel();
        txtJwb10 = new javax.swing.JTextField();
        lblJdl = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuUji = new javax.swing.JMenu();
        mnuNil = new javax.swing.JMenu();
        mnuPass = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));

        lblTimer.setBackground(new java.awt.Color(0, 0, 0));
        lblTimer.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTimer.setText("Timer:");

        lblUser.setBackground(new java.awt.Color(0, 0, 0));
        lblUser.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblUser.setText("Siswa");

        lblJml.setBackground(new java.awt.Color(0, 0, 0));
        lblJml.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblJml.setText("Soal: 10");

        lblStart.setBackground(new java.awt.Color(0, 0, 0));
        lblStart.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStart.setText("Waktu Mulai:");

        btnSubmit.setBackground(new java.awt.Color(0, 255, 204));
        btnSubmit.setForeground(new java.awt.Color(0, 0, 0));
        btnSubmit.setText("Kumpulkan");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTimer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblJml, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                    .addComponent(lblStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSubmit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblUser)
                .addGap(42, 42, 42)
                .addComponent(lblStart)
                .addGap(18, 18, 18)
                .addComponent(lblTimer)
                .addGap(18, 18, 18)
                .addComponent(lblJml)
                .addGap(18, 18, 18)
                .addComponent(btnSubmit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(0, 255, 204));
        jPanel2.setForeground(new java.awt.Color(0, 204, 204));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal1.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal1.setText("30 + 190");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal1, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal1))
                .addGap(17, 17, 17))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal2.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal2.setText("30 + 190");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal3.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal3.setText("30 + 190");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal3, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal3))
                .addGap(17, 17, 17))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal5.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal5.setText("30 + 190");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal5, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb5, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal5))
                .addGap(17, 17, 17))
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal6.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal6.setText("30 + 190");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal6, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb6, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal6))
                .addGap(17, 17, 17))
        );

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal4.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal4.setText("30 + 190");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal4, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb4, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal4))
                .addGap(17, 17, 17))
        );

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal7.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal7.setText("30 + 190");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal7, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb7, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal7))
                .addGap(17, 17, 17))
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal8.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal8.setText("30 + 190");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal8, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb8, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal8))
                .addGap(17, 17, 17))
        );

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal9.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal9.setText("30 + 190");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal9, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb9, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal9))
                .addGap(17, 17, 17))
        );

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));

        lblSoal10.setBackground(new java.awt.Color(0, 0, 0));
        lblSoal10.setText("30 + 190");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSoal10, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtJwb10, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJwb10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoal10))
                .addGap(17, 17, 17))
        );

        lblJdl.setBackground(new java.awt.Color(0, 0, 0));
        lblJdl.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblJdl.setText("PENJUMLAHAN");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(228, 228, 228)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 1, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblJdl)
                .addGap(131, 131, 131))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblJdl)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        mnuUji.setText("Ujian");
        jMenuBar1.add(mnuUji);

        mnuNil.setText("Nilai");
        jMenuBar1.add(mnuNil);

        mnuPass.setText("Reset Password");
        jMenuBar1.add(mnuPass);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Ujian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ujian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ujian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ujian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ujian().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSubmit;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel lblJdl;
    private javax.swing.JLabel lblJml;
    private javax.swing.JLabel lblSoal1;
    private javax.swing.JLabel lblSoal10;
    private javax.swing.JLabel lblSoal2;
    private javax.swing.JLabel lblSoal3;
    private javax.swing.JLabel lblSoal4;
    private javax.swing.JLabel lblSoal5;
    private javax.swing.JLabel lblSoal6;
    private javax.swing.JLabel lblSoal7;
    private javax.swing.JLabel lblSoal8;
    private javax.swing.JLabel lblSoal9;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTimer;
    private javax.swing.JLabel lblUser;
    private javax.swing.JMenu mnuNil;
    private javax.swing.JMenu mnuPass;
    private javax.swing.JMenu mnuUji;
    private javax.swing.JTextField txtJwb1;
    private javax.swing.JTextField txtJwb10;
    private javax.swing.JTextField txtJwb2;
    private javax.swing.JTextField txtJwb3;
    private javax.swing.JTextField txtJwb4;
    private javax.swing.JTextField txtJwb5;
    private javax.swing.JTextField txtJwb6;
    private javax.swing.JTextField txtJwb7;
    private javax.swing.JTextField txtJwb8;
    private javax.swing.JTextField txtJwb9;
    // End of variables declaration//GEN-END:variables
}
