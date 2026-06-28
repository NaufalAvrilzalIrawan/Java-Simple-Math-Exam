/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasiujian;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

/**
 *
 * @author user
 */
public class Dashboard extends javax.swing.JFrame {

    private int userId;
    private String username;
    private String role;
    
    /**
     * Default constructor (Kept for NetBeans GUI Builder compatibility)
     */
    public Dashboard() {
        initComponents();
        this.setLocationRelativeTo(null);
    }
    
    public Dashboard(int userId, String username, String role) {
        initComponents();
        this.userId = userId;
        this.username = username;
        this.role = role;
        System.out.println("DEBUG Dashboard: " + userId + " " + username + " " + role);
        lblJdl1.setText("Welcome Back " + username + "!");
        this.setLocationRelativeTo(null);
        
        // Dynamically load student progress from the database
        loadStudentProgress();
    }
    
    /**
     * Dynamically generates the UI to show all students and their progress.
     * Replaces the static placeholders created in the GUI builder.
     */
    private void loadStudentProgress() {
        // 1. Clear the static elements (jLabel1, jLabel2, jLabel3, jProgressBar1) 
        // from jPanel2 so we can build a dynamic, scrollable list instead.
        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout());
        
        // 2. Create a container panel with a Grid Layout (Rows will auto-expand, 2 Columns)
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(0, 2, 20, 15)); // 0 rows (dynamic), 2 columns, 20px hgap, 15px vgap
        listPanel.setBackground(new Color(0, 255, 204));
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding
        
        // 3. Add Headers to the Grid
        JLabel lblHeaderName = new JLabel("NAMA SISWA");
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel lblHeaderProgress = new JLabel("PROGRESS (4 KATEGORI)");
        lblHeaderProgress.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        listPanel.add(lblHeaderName);
        listPanel.add(lblHeaderProgress);
        
        // 4. Fetch student progress from the database
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection1.getConnection();
            
            // Query: Get all users with role 'student' and count distinct categories they've completed
            String sql = "SELECT u.username, COUNT(DISTINCT er.category_id) as completed_categories " +
                         "FROM users u " +
                         "LEFT JOIN exam_results er ON u.id = er.user_id " +
                         "WHERE u.role = 'student' " +
                         "GROUP BY u.id, u.username " +
                         "ORDER BY u.username";
                         
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            // 5. Generate a Label and ProgressBar for each student dynamically
            while (rs.next()) {
                String studentName = rs.getString("username");
                int completed = rs.getInt("completed_categories");
                
                // Calculate percentage (Max 4 categories = 100%)
                int percentage = (int) ((completed / 4.0) * 100);
                
                // Create Name Label
                JLabel lblName = new JLabel(studentName);
                lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                // Create Progress Bar
                JProgressBar pBar = new JProgressBar(0, 100);
                pBar.setValue(percentage);
                pBar.setStringPainted(true);
                pBar.setString(completed + "/4 Kategori (" + percentage + "%)");
                pBar.setForeground(new Color(0, 204, 0)); // Green progress color
                
                // Add to our grid
                listPanel.add(lblName);
                listPanel.add(pBar);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load progress:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if(rs != null) rs.close();
                if(pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        // 6. Wrap the list panel inside a JScrollPane so it handles overflow elegantly
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null); // Clean look without inner borders
        scrollPane.getViewport().setBackground(new Color(0, 255, 204));
        
        // 7. Add the scroll pane back into jPanel2 and refresh the UI
        jPanel2.add(scrollPane, BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();
        lblJdl = new javax.swing.JLabel();
        lblJdl1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuDas = new javax.swing.JMenu();
        mnuNil = new javax.swing.JMenu();
        mnuLot = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(0, 255, 204));
        jPanel2.setForeground(new java.awt.Color(0, 204, 204));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("NAMA");

        jLabel2.setBackground(new java.awt.Color(0, 0, 0));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("PROGRESS");

        jLabel3.setText("jLabel3");

        jProgressBar1.setForeground(new java.awt.Color(0, 255, 204));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(221, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lblJdl.setBackground(new java.awt.Color(0, 0, 0));
        lblJdl.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblJdl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblJdl.setText("DASHBOARD");

        lblJdl1.setBackground(new java.awt.Color(0, 0, 0));
        lblJdl1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblJdl1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblJdl1.setText("Welcome Back");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJdl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblJdl1, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblJdl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblJdl1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mnuDas.setText("Dashboard");
        mnuDas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mnuDasMouseClicked(evt);
            }
        });
        jMenuBar1.add(mnuDas);

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

    private void mnuDasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuDasMouseClicked
        Dashboard dashboard = new Dashboard(userId, username, role);
        dashboard.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_mnuDasMouseClicked

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
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel lblJdl;
    private javax.swing.JLabel lblJdl1;
    private javax.swing.JMenu mnuDas;
    private javax.swing.JMenu mnuLot;
    private javax.swing.JMenu mnuNil;
    // End of variables declaration//GEN-END:variables
}
