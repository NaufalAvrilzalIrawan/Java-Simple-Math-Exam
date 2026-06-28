/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasiujian;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class Nilai extends javax.swing.JFrame {

    private int userId;
    private String username;
    private String role;
    
    /**
     * Default constructor (Kept for NetBeans GUI Builder compatibility)
     */
    public Nilai() {
        initComponents();
        this.setLocationRelativeTo(null);
    }
    
    public Nilai(int userId, String username, String role) {
        initComponents();
        this.userId = userId;
        this.username = username;
        this.role = role;
        
        System.out.println("DEBUG Nilai: " + userId + " " + username + " " + role);
        this.setLocationRelativeTo(null);
        
        // Update Title based on role
        if(this.role.equalsIgnoreCase("teacher")) {
            lblJdl1.setText("SEMUA SISWA");
        } else {
            lblJdl1.setText("SISWA: " + this.username.toUpperCase());
        }
        
        // Load data into the table
        loadDataToTable();
    }
    
    /**
     * Loads exam results into the JTable based on the user's role.
     */
    private void loadDataToTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Clear existing table rows
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection1.getConnection();
            
            String sql = "SELECT er.id AS result_id, u.username, c.category_name, er.score, er.start_time, er.duration_seconds "
                       + "FROM exam_results er "
                       + "JOIN users u ON er.user_id = u.id "
                       + "JOIN categories c ON er.category_id = c.id ";
            
            if (this.role.equalsIgnoreCase("student")) {
                sql += "WHERE er.user_id = ? ";
            }
            
            sql += "ORDER BY er.start_time DESC";
            
            pst = conn.prepareStatement(sql);
            
            if (this.role.equalsIgnoreCase("student")) {
                pst.setInt(1, this.userId);
            }
            
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String idResult = rs.getString("result_id");
                String studentName = rs.getString("username");
                String dbCategory = rs.getString("category_name");
                String score = rs.getString("score");
                String examTime = rs.getString("start_time");
                
                // Convert English category names from database to Indonesian
                String categoryIndo = dbCategory;
                switch (dbCategory.toLowerCase()) {
                    case "addition":
                    case "penjumlahan":
                        categoryIndo = "Penjumlahan";
                        break;
                    case "subtraction":
                    case "pengurangan":
                        categoryIndo = "Pengurangan";
                        break;
                    case "multiplication":
                    case "perkalian":
                        categoryIndo = "Perkalian";
                        break;
                    case "division":
                    case "pembagian":
                        categoryIndo = "Pembagian";
                        break;
                }
                
                // Format duration from seconds to MM:SS
                int duration = rs.getInt("duration_seconds");
                String formattedDuration = String.format("%02d:%02d", duration / 60, duration % 60);
                
                model.addRow(new Object[]{idResult, studentName, categoryIndo, score, examTime, formattedDuration});
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load data:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if(rs != null) rs.close();
                if(pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates and displays a floating dialog (JDialog) showing specific question details.
     * Includes a custom renderer to color code correct (green) and incorrect (red) rows.
     */
    private void showDetailDialog(String examResultId, String studentName, String categoryName) {
        JDialog detailDialog = new JDialog(this, "Detail Nilai: " + studentName + " - " + categoryName, true);
        detailDialog.setSize(600, 350);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout());
        
        String[] columnNames = {"Soal", "Jawaban Siswa", "Kunci Jawaban", "Status"};
        DefaultTableModel detailModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make detail table read-only
            }
        };
        
        JTable detailTable = new JTable(detailModel);
        
        // Custom cell renderer for coloring rows based on the "Status" column value
        detailTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Get the status value from the 3rd column (index 3 -> "Status")
                String status = table.getValueAt(row, 3).toString();
                
                if (status.equalsIgnoreCase("Benar")) {
                    c.setForeground(new Color(0, 153, 51)); // Dark Green text
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.RED); // Red text
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                
                // Keep background readable and handle row selection styling cleanly
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(Color.WHITE);
                }
                
                return c;
            }
        });
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection1.getConnection();
            String sql = "SELECT question_text, student_answer, correct_answer, is_correct "
                       + "FROM exam_details WHERE exam_result_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, Integer.parseInt(examResultId));
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String soal = rs.getString("question_text");
                String jwbSiswa = rs.getString("student_answer");
                String kunci = rs.getString("correct_answer");
                String status = rs.getBoolean("is_correct") ? "Benar" : "Salah";
                
                detailModel.addRow(new Object[]{soal, jwbSiswa, kunci, status});
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(detailDialog, "Failed to load details:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if(rs != null) rs.close();
                if(pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        detailDialog.add(new JScrollPane(detailTable), BorderLayout.CENTER);
        detailDialog.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnDtl = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblJdl = new javax.swing.JLabel();
        lblJdl1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuUji = new javax.swing.JMenu();
        mnuNil = new javax.swing.JMenu();
        mnuLot = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(0, 255, 204));
        jPanel2.setForeground(new java.awt.Color(0, 204, 204));

        jTable1.setBackground(new java.awt.Color(255, 255, 255));
        jTable1.setForeground(new java.awt.Color(0, 0, 0));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nama", "Kategori", "Nilai", "Waktu Ujian", "Durasi"
            }
        ));
        jTable1.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTable1.setSelectionForeground(new java.awt.Color(0, 204, 204));
        jScrollPane1.setViewportView(jTable1);

        btnDtl.setBackground(new java.awt.Color(255, 255, 255));
        btnDtl.setForeground(new java.awt.Color(0, 0, 0));
        btnDtl.setText("Detail Nilai");
        btnDtl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDtlActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnDtl)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnDtl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lblJdl.setBackground(new java.awt.Color(0, 0, 0));
        lblJdl.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblJdl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblJdl.setText("NILAI");

        lblJdl1.setBackground(new java.awt.Color(0, 0, 0));
        lblJdl1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblJdl1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblJdl1.setText("SISWA");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJdl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblJdl1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblJdl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblJdl1)
                .addContainerGap())
        );

        mnuUji.setText("Ujian");
        mnuUji.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mnuUjiMouseClicked(evt);
            }
        });
        jMenuBar1.add(mnuUji);

        mnuNil.setText("Nilai");
        mnuNil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mnuNilMouseClicked(evt);
            }
        });
        jMenuBar1.add(mnuNil);

        mnuLot.setText("Logout");
        mnuLot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mnuLotMouseClicked(evt);
            }
        });
        jMenuBar1.add(mnuLot);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDtlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDtlActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        
        // Validate if a row is selected (-1 means no row is selected)
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data siswa dari tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract necessary data from the selected row
        String examResultId = jTable1.getValueAt(selectedRow, 0).toString();
        String studentName = jTable1.getValueAt(selectedRow, 1).toString();
        String categoryName = jTable1.getValueAt(selectedRow, 2).toString();
        
        // Show the floating detail dialog
        showDetailDialog(examResultId, studentName, categoryName);
    }//GEN-LAST:event_btnDtlActionPerformed

    private void mnuUjiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuUjiMouseClicked
        Kategori kategoriForm = new Kategori(userId, username, role);
        kategoriForm.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_mnuUjiMouseClicked

    private void mnuNilMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuNilMouseClicked
        Nilai nilai = new Nilai(userId, username, role);
        nilai.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_mnuNilMouseClicked

    private void mnuLotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuLotMouseClicked
        Login login = new Login();
        login.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_mnuLotMouseClicked

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
            java.util.logging.Logger.getLogger(Nilai.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Nilai.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Nilai.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Nilai.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Nilai().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDtl;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblJdl;
    private javax.swing.JLabel lblJdl1;
    private javax.swing.JMenu mnuLot;
    private javax.swing.JMenu mnuNil;
    private javax.swing.JMenu mnuUji;
    // End of variables declaration//GEN-END:variables
}
