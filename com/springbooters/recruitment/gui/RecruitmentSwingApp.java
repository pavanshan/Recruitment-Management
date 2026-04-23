package com.springbooters.recruitment.gui;

import com.springbooters.recruitment.db.DatabaseConnection;
import com.springbooters.recruitment.db.RecruitmentRepository;
import com.springbooters.recruitment.service.AutomatedScreener;
import com.springbooters.recruitment.service.ExperienceMatchRule;
import com.springbooters.recruitment.service.NotificationFactory;
import com.springbooters.recruitment.service.RecruitmentFacade;
import com.springbooters.recruitment.service.ResumeAvailableRule;
import com.springbooters.recruitment.service.SkillMatchRule;
import com.springbooters.recruitment.model.Application;
import com.springbooters.recruitment.model.ApplicationStage;
import com.springbooters.recruitment.model.ApplicationStatus;
import com.springbooters.recruitment.model.AuditEntry;
import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.ChannelType;
import com.springbooters.recruitment.model.InterviewSchedule;
import com.springbooters.recruitment.model.InterviewType;
import com.springbooters.recruitment.model.JobPosting;
import com.springbooters.recruitment.model.JobPostingBuilder;
import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.Offer;
import com.springbooters.recruitment.model.OfferBuilder;
import com.springbooters.recruitment.model.OfferStatus;
import com.springbooters.recruitment.model.JobStatus;
import com.springbooters.recruitment.model.ScreeningResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.awt.*;
import java.awt.Desktop;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Desktop GUI for the recruitment subsystem.
 *
 * The screen is intentionally dashboard-like: HR can see metrics, job postings,
 * application pipeline and notifications from one place.
 */
public class RecruitmentSwingApp extends JFrame {

    private static final Color HEADER_BLUE = new Color(190, 204, 229);
    private static final Color SIDEBAR_BLUE = new Color(188, 204, 232);
    private static final Color SIDEBAR_SELECTED = new Color(119, 188, 226);
    private static final Color MAIN_BG = new Color(5, 7, 8);
    private static final Color PANEL_BG = new Color(22, 24, 26);
    private static final Color TEXT = new Color(230, 235, 240);
    private static final Color MUTED = new Color(160, 170, 180);
    private static final Color ACCENT = new Color(128, 201, 233);
    private static final Color GREEN = new Color(88, 156, 70);
    private static final Color PINK = new Color(223, 114, 128);

    private final DefaultTableModel jobTableModel = new DefaultTableModel(
            new Object[]{"Job ID", "Job Title", "Department", "Posted Date", "Applications", "Status", "Platform"}, 0);
    private final JTable jobTable = new JTable(jobTableModel);
    
    private final DefaultTableModel applicationsTableModel = new DefaultTableModel(
            new Object[]{"App ID", "Candidate Name", "Job Title", "Date Applied", "Stage"}, 0);
    private final JTable applicationsTable = new JTable(applicationsTableModel);

    private final DefaultTableModel screeningTableModel = new DefaultTableModel(
            new Object[]{"App ID", "Candidate Name", "Score", "Ranking", "Shortlist Status"}, 0);
    private final JTable screeningTable = new JTable(screeningTableModel);

    private final DefaultTableModel interviewsTableModel = new DefaultTableModel(
            new Object[]{"App ID", "Candidate Name", "Job Title", "Interviewer", "Date", "Status"}, 0);
    private final JTable interviewsTable = new JTable(interviewsTableModel);

    private final DefaultTableModel offersTableModel = new DefaultTableModel(
            new Object[]{"App ID", "Candidate Name", "Job Title", "Salary", "Join Date", "Status"}, 0);
    private final JTable offersTable = new JTable(offersTableModel);

    private final DefaultTableModel notificationsTableModel = new DefaultTableModel(
            new Object[]{"Timestamp", "Type", "Contact", "Alert"}, 0);
    private final JTable notificationsTable = new JTable(notificationsTableModel);
    
    private final DefaultTableModel auditTableModel = new DefaultTableModel(
            new Object[]{"Timestamp", "Actor", "Action", "Details"}, 0);
    private final JTable auditTable = new JTable(auditTableModel);

    private final JTextArea notificationArea = new JTextArea(7, 35);
    private final JLabel activeJobsValue = metricValue("0");
    private final JLabel applicationsValue = metricValue("0");
    private final JLabel interviewsValue = metricValue("0");
    private final JLabel offersValue = metricValue("0");
    private final JLabel statusLabel = new JLabel("Ready");
    private final JCheckBox saveToDatabaseBox = new JCheckBox("Save to Database", true);
    private final PipelinePanel pipelinePanel = new PipelinePanel();
    
    private final JLabel dashboardHeading = new JLabel("Job Postings");
    private final java.util.List<JLabel> navLabels = new java.util.ArrayList<>();
    private final java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);
    private final AnalyticsPanel analyticsPanel;
    
    private TableRowSorter<DefaultTableModel> jobSorter;
    private TableRowSorter<DefaultTableModel> appsSorter;
    private TableRowSorter<DefaultTableModel> candidatesSorter; // if added
    
    private final RecruitmentFacade recruitment;

    private int activeJobs = 0;
    private int applications = 0;
    private int interviews = 0;
    private int offers = 0;

    public RecruitmentSwingApp() {
        super("HRMS - Recruitment Management System");
        
        ResumeAvailableRule resumeRule = new ResumeAvailableRule();
        resumeRule.linkWith(new ExperienceMatchRule())
                .linkWith(new SkillMatchRule());
        recruitment = new RecruitmentFacade(new NotificationFactory(), new AutomatedScreener(resumeRule));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1040, 650));

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        
        analyticsPanel = new AnalyticsPanel(recruitment);
        add(createDashboard(), BorderLayout.CENTER);

        initializeDashboard();
        fullSyncFromDb(); // Load initial data from DB
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecruitmentSwingApp().setVisible(true));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BLUE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("HRMS - Recruitment Management System v1.5 - Secure Edition");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        JLabel user = new JLabel("SpringBooters");
        user.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.add(user, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(0, 1, 0, 0));
        sidebar.setPreferredSize(new Dimension(170, 0));
        sidebar.setBackground(SIDEBAR_BLUE);

        sidebar.add(navLabel("Navigation", false, false));
        sidebar.add(navLabel("Dashboard", false, true));
        sidebar.add(navLabel("Job Postings", true, true));
        sidebar.add(navLabel("Applications", false, true));
        sidebar.add(navLabel("Screening", false, true));
        sidebar.add(navLabel("Interviews", false, true));
        sidebar.add(navLabel("Offer Letters", false, true));
        sidebar.add(navLabel("Notifications", false, true));
        sidebar.add(navLabel("Reports", false, true));
        sidebar.add(navLabel("Audit Log", false, true));
        sidebar.add(navLabel("Career Portal", false, true));
        return sidebar;
    }

    private JLabel navLabel(String text, boolean selected, boolean clickable) {
        JLabel label = new JLabel("  " + text);
        label.setOpaque(true);
        label.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 13));
        label.setBackground(selected ? SIDEBAR_SELECTED : SIDEBAR_BLUE);
        label.setForeground(Color.BLACK);
        
        if (clickable) {
            navLabels.add(label);
            label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (JLabel l : navLabels) {
                        l.setBackground(SIDEBAR_BLUE);
                        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    }
                    label.setBackground(SIDEBAR_SELECTED);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    dashboardHeading.setText(text);
                    if (cardLayout != null) {
                        String cardName = text.equals("Job Postings") ? "Dashboard" : text;
                        cardLayout.show(mainContentPanel, cardName);
                        if (cardName.equals("Career Portal")) {
                            refreshPortal();
                        }
                    }
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (label.getBackground() != SIDEBAR_SELECTED) {
                        label.setBackground(new Color(160, 180, 215));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (label.getBackground() != SIDEBAR_SELECTED) {
                        label.setBackground(SIDEBAR_BLUE);
                    }
                }
            });
        }
        
        return label;
    }

    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(14, 14));
        dashboard.setBackground(MAIN_BG);
        dashboard.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        dashboard.add(createDashboardTop(), BorderLayout.NORTH);
        mainContentPanel.setOpaque(false);
        // Dashboard and Job Postings share the same card component instance to avoid duplicate layout problems
        JPanel dashPanel = createMainContent();
        mainContentPanel.add(dashPanel, "Dashboard");
        
        mainContentPanel.add(createDataPanel(applicationsTable, null, createApplicationActionButtons(), true), "Applications");
        mainContentPanel.add(createDataPanel(screeningTable, null, null, false), "Screening");

        JButton scheduleBtn = primaryButton("Schedule Interview");
        scheduleBtn.addActionListener(e -> {
            int row = interviewsTable.getSelectedRow();
            String selectedId = null;
            if (row != -1) {
                int modelRow = interviewsTable.convertRowIndexToModel(row);
                selectedId = (String) interviewsTable.getModel().getValueAt(modelRow, 0);
            }
            new ScheduleInterviewDialog(this, selectedId).setVisible(true);
        });

        JButton recordResultBtn = primaryButton("Record Result");
        recordResultBtn.addActionListener(e -> {
            int row = interviewsTable.getSelectedRow();
            String selectedId = null;
            if (row != -1) {
                int modelRow = interviewsTable.convertRowIndexToModel(row);
                selectedId = (String) interviewsTable.getModel().getValueAt(modelRow, 0);
            }
            new RecordInterviewResultDialog(this, selectedId).setVisible(true);
        });

        JButton interviewNowBtnHeader = primaryButton("Interview Now");
        interviewNowBtnHeader.setBackground(new Color(80, 150, 80));
        interviewNowBtnHeader.addActionListener(e -> {
            int row = interviewsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a candidate from the Interviews table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = interviewsTable.convertRowIndexToModel(row);
            String appId = (String) interviewsTable.getModel().getValueAt(modelRow, 0);
            
            try {
                recruitment.completeInterview(appId);
                runDbSave(() -> {
                    try { new RecruitmentRepository(new DatabaseConnection()).saveApplicationStatus(recruitment.getStatus(appId)); }
                    catch (Exception ex) { throw new RuntimeException(ex); }
                });
                refreshAllViews();
                notificationArea.append("Quick Interview Completed for: " + appId + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel interviewActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        interviewActions.setOpaque(false);
        interviewActions.add(scheduleBtn);
        interviewActions.add(recordResultBtn);
        interviewActions.add(interviewNowBtnHeader);
        

        mainContentPanel.add(createDataPanel(interviewsTable, null, interviewActions, false), "Interviews");
        mainContentPanel.add(createDataPanel(offersTable, null, createOfferActionButtons(), false), "Offer Letters");

        // Reports View
        mainContentPanel.add(createDataPanel(notificationsTable, null, null, false), "Notifications");
        mainContentPanel.add(createDataPanel(auditTable, null, null, false), "Audit Log");

        JPanel reportsPanel = new JPanel(new BorderLayout());

        // Career Portal View
        mainContentPanel.add(new CareerPortalPanel(), "Career Portal");

        reportsPanel.add(analyticsPanel, BorderLayout.CENTER);
        analyticsPanel.addExportButton(reportsPanel);
        mainContentPanel.add(reportsPanel, "Reports");
        
        setupTableSorters();
        dashboard.add(mainContentPanel, BorderLayout.CENTER);
        dashboard.add(createFooter(), BorderLayout.SOUTH);
        return dashboard;
    }
    
    private void setupTableSorters() {
        jobSorter = new TableRowSorter<>(jobTableModel);
        jobTable.setRowSorter(jobSorter);
        
        appsSorter = new TableRowSorter<>(applicationsTableModel);
        applicationsTable.setRowSorter(appsSorter);
    }

    private JPanel createDashboardTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        dashboardHeading.setForeground(TEXT);
        dashboardHeading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        top.add(dashboardHeading, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        saveToDatabaseBox.setOpaque(false);
        saveToDatabaseBox.setForeground(TEXT);
        
        JButton postJobBtn = primaryButton("+ Job");
        postJobBtn.addActionListener(e -> new PostJobDialog(this).setVisible(true));
        
        JButton addCandBtn = primaryButton("+ Candidate");
        addCandBtn.addActionListener(e -> new AddCandidateDialog(this).setVisible(true));
        
        JButton addAppBtn = primaryButton("+ Apply");
        addAppBtn.addActionListener(e -> new ApplyDialog(this).setVisible(true));

        JButton offerBtn = primaryButton("+ Offer");
        offerBtn.addActionListener(e -> new RecordOfferDialog(this).setVisible(true));

        JButton refreshBtn = primaryButton("Refresh");
        refreshBtn.setBackground(new Color(100, 120, 140));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> fullSyncFromDb());

        JButton approveBtn = primaryButton("✓ Approve");
        approveBtn.addActionListener(e -> {
            int row = jobTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a job to approve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = jobTable.convertRowIndexToModel(row);
            String jobId = jobTableModel.getValueAt(modelRow, 0).toString();
            String status = jobTableModel.getValueAt(modelRow, 5).toString();
            
            if (!JobStatus.DRAFT.name().equals(status)) {
                JOptionPane.showMessageDialog(this, "Only DRAFT requisitions can be approved. Current: " + status, "Selection Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                recruitment.approveJobRequisition(jobId);
                JobPosting job = recruitment.getJobPostings().stream().filter(j -> j.getJobId().equals(jobId)).findFirst().get();
                runDbSave(() -> {
                    try { new RecruitmentRepository(new DatabaseConnection()).saveJobPosting(job); } catch (Exception ex) { throw new RuntimeException(ex); }
                });
                refreshAllViews();
                notificationArea.append("Requisition Approved: " + jobId + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton publishBtn = primaryButton("↑ Publish");
        publishBtn.addActionListener(e -> {
            int row = jobTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a job to publish.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = jobTable.convertRowIndexToModel(row);
            String jobId = jobTableModel.getValueAt(modelRow, 0).toString();
            String status = jobTableModel.getValueAt(modelRow, 5).toString();

            if (!JobStatus.APPROVED.name().equals(status)) {
                JOptionPane.showMessageDialog(this, "Only APPROVED requisitions can be published. Current: " + status, "Selection Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                JobPosting jobSpec = recruitment.getJobPostings().stream()
                        .filter(j -> j.getJobId().equals(jobId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
                recruitment.publishJob(jobSpec);
                runDbSave(() -> {
                    try { new RecruitmentRepository(new DatabaseConnection()).saveJobPosting(jobSpec); } catch (Exception ex) { throw new RuntimeException(ex); }
                });
                refreshAllViews();
                JOptionPane.showMessageDialog(this, 
                    "Job \"" + jobSpec.getTitle() + "\" is now ACTIVE on " + jobSpec.getPlatformName() + ".\nCandidates can now apply.",
                    "Publication Successful", JOptionPane.INFORMATION_MESSAGE);
                notificationArea.append("Job Published: " + jobId + " is now ACTIVE.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(saveToDatabaseBox);
        actions.add(refreshBtn);
        actions.add(approveBtn);
        actions.add(publishBtn);
        actions.add(postJobBtn);
        actions.add(addCandBtn);
        actions.add(addAppBtn);
        actions.add(offerBtn);
        
        top.add(actions, BorderLayout.EAST);
        return top;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setOpaque(false);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(metricCard("Active Jobs", activeJobsValue, ACCENT));
        metrics.add(metricCard("Applications", applicationsValue, ACCENT));
        metrics.add(metricCard("Interviews Today", interviewsValue, new Color(146, 174, 141)));
        metrics.add(metricCard("Offers Sent", offersValue, PINK));
        content.add(metrics, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 12));
        body.setOpaque(false);
        body.add(createDataPanel(jobTable, new String[]{"All Jobs", "Active", "Draft"}), BorderLayout.CENTER);
        body.add(createRightPanel(), BorderLayout.EAST);
        content.add(body, BorderLayout.CENTER);
        return content;
    }

    private JPanel createDataPanel(JTable table, String[] filters, JComponent actions, boolean searchable) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        if (filters != null || searchable) {
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            
            if (filters != null) {
                JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                filterPanel.setOpaque(false);
                for (String f : filters) {
                    filterPanel.add(filterButton(f, f.equals(filters[0])));
                }
                top.add(filterPanel, BorderLayout.WEST);
            }

            if (searchable) {
                JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                searchPanel.setOpaque(false);
                searchPanel.add(new JLabel("🔍") {{ setForeground(TEXT); }});
                JTextField searchField = new JTextField(15);
                searchField.setBackground(new Color(30, 32, 35));
                searchField.setForeground(TEXT);
                searchField.setCaretColor(TEXT);
                searchField.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 70)));
                searchField.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) { filter(); }
                    public void removeUpdate(DocumentEvent e) { filter(); }
                    public void changedUpdate(DocumentEvent e) { filter(); }
                    private void filter() {
                        String text = searchField.getText();
                        TableRowSorter sorter = (TableRowSorter) table.getRowSorter();
                        if (sorter != null) {
                            if (text.trim().length() == 0) sorter.setRowFilter(null);
                            else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                        }
                    }
                });
                searchPanel.add(searchField);
                top.add(searchPanel, BorderLayout.EAST);
            }
            panel.add(top, BorderLayout.NORTH);
        }

        table.setBackground(PANEL_BG);
        table.setForeground(TEXT);
        table.setGridColor(new Color(45, 50, 55));
        table.setSelectionBackground(SIDEBAR_SELECTED);
        table.setSelectionForeground(Color.BLACK);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(PANEL_BG);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 50, 55)));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        if (actions != null) {
            actions.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0)); // Add some padding
            panel.add(actions, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel createDataPanel(JTable table, String[] filters, JComponent actions) {
        return createDataPanel(table, filters, actions, false);
    }

    private JPanel createDataPanel(JTable table, String[] filters) {
        return createDataPanel(table, filters, null, false);
    }


    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 14));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(330, 0));
        right.add(pipelinePanel, BorderLayout.NORTH);
        right.add(createNotificationPanel(), BorderLayout.CENTER);
        return right;
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 50, 55)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel label = new JLabel("Notifications");
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(label, BorderLayout.NORTH);

        notificationArea.setEditable(false);
        notificationArea.setBackground(PANEL_BG);
        notificationArea.setForeground(TEXT);
        notificationArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        notificationArea.setLineWrap(true);
        notificationArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(notificationArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(HEADER_BLUE);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setText("Recruitment Management System | HRMS v1.0 | Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(statusLabel, BorderLayout.CENTER);
        return footer;
    }

    private JPanel metricCard(String labelText, JLabel valueLabel, Color borderColor) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(MAIN_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(label);
        card.add(valueLabel);
        return card;
    }

    private JLabel metricValue(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        return label;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
    }

    private JButton filterButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setBackground(selected ? new Color(215, 226, 234) : new Color(22, 24, 26));
        button.setForeground(selected ? Color.BLACK : TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 55)));
        return button;
    }

    private void initializeDashboard() {
        refreshAllViews();
        notificationArea.setText("Ready for recruitment operations.");
    }

    private void refreshMetrics() {
        activeJobsValue.setText(String.valueOf(activeJobs));
        applicationsValue.setText(String.valueOf(applications));
        interviewsValue.setText(String.valueOf(interviews));
        offersValue.setText(String.valueOf(offers));
    }

    private void refreshAllViews() {
        jobTableModel.setRowCount(0);
        applicationsTableModel.setRowCount(0);
        screeningTableModel.setRowCount(0);
        interviewsTableModel.setRowCount(0);
        offersTableModel.setRowCount(0);
        notificationsTableModel.setRowCount(0);
        auditTableModel.setRowCount(0);

        int countApplied = 0;
        int countScreened = 0;
        int countInterviewed = 0;
        int countOffered = 0;

        for (JobPosting j : recruitment.getJobPostings()) {
            long apps = recruitment.getApplications().stream()
                    .filter(a -> a.getJobId().equals(j.getJobId())).count();
            jobTableModel.addRow(new Object[]{j.getJobId(), j.getTitle(), j.getDepartment(), j.getPostedDate(), apps, j.getStatus(), j.getPlatformName()});
        }
        
        for (Application a : recruitment.getApplications()) {
            ApplicationStatus status = recruitment.getStatus(a.getApplicationId());
            String stageStr = (status != null && status.getCurrentStage() != null) ? status.getCurrentStage() : "APPLIED";
            ApplicationStage stage = ApplicationStage.from(stageStr);
            String cName = resolveCandidateName(a.getCandidateId());
            String jTitle = resolveJobTitle(a.getJobId());
            
            // Pipeline Counts logic (Now based on stage hierarchy)
            if (stage == ApplicationStage.APPLIED) countApplied++;
            
            // Items remain in sections if they have reached them (Historical milestone check)
            boolean hasReachedScreening = status.hasReached(ApplicationStage.SCREENED);
            boolean hasReachedInterview = status.hasReached(ApplicationStage.SHORTLISTED);
            boolean hasReachedOffer = status.hasReached(ApplicationStage.INTERVIEW_COMPLETED);

            if (stage == ApplicationStage.SCREENED) countScreened++;
            if (stage == ApplicationStage.SHORTLISTED || stage == ApplicationStage.INTERVIEW_SCHEDULED) countInterviewed++;
            if (stage == ApplicationStage.INTERVIEW_COMPLETED || stage == ApplicationStage.OFFERED || stage == ApplicationStage.HIRED) countOffered++;

            applicationsTableModel.addRow(new Object[]{a.getApplicationId(), cName, jTitle, a.getDateApplied(), stageStr});

            // 1. Interviews Tab Pipeline (Persists after SHORTLISTED)
            if (hasReachedInterview) {
                InterviewSchedule schedule = recruitment.getInterviewSchedules().stream()
                        .filter(i -> i.getCandidateId().equals(a.getCandidateId()))
                        .findFirst().orElse(null);
                
                String interviewer = (schedule != null) ? schedule.getInterviewerId() : "TBD";
                String iDate = (schedule != null) ? (schedule.getInterviewDate() != null ? schedule.getInterviewDate().toString() : "TBD") : "Needs Scheduling";
                interviewsTableModel.addRow(new Object[]{a.getApplicationId(), cName, jTitle, interviewer, iDate, stageStr});
            }

            // 2. Offers Tab Pipeline (Persists after INTERVIEW_COMPLETED)
            if (hasReachedOffer) {
                Offer offer = recruitment.getOffers().stream()
                        .filter(o -> o.getCandidateId().equals(a.getCandidateId()))
                        .findFirst().orElse(null);
                
                String salary = (offer != null && offer.getSalary() != null) ? offer.getSalary().toString() : "TBD";
                String joinDate = (offer != null && offer.getStartDate() != null) ? offer.getStartDate().toString() : "TBD";
                String oStatus = (offer != null) ? offer.getStatus() : "READY_FOR_OFFER";
                offersTableModel.addRow(new Object[]{a.getApplicationId(), cName, jTitle, salary, joinDate, oStatus});
            }
        }

        // Screening Tab logic (Persists all applications that have been screened)
        for (ScreeningResult r : recruitment.getScreeningResults()) {
            ApplicationStatus status = recruitment.getStatus(r.getApplicationId());
            if (status != null) {
                Candidate c = resolveCandidateByAppId(r.getApplicationId());
                String cName = c != null ? c.getName() : "Unknown";
                String currentStage = status.getCurrentStage();
                // We show the screening result but update the status column to show where they are now
                screeningTableModel.addRow(new Object[]{r.getApplicationId(), cName, r.getScore(), r.getRanking(), currentStage});
            }
        }

        for (NotificationLog n : recruitment.getNotificationLogs()) {
            notificationsTableModel.addRow(new Object[]{n.getSentTimestamp(), n.getNotificationType(), n.getContactInfoUsed(), n.getStatusAlert()});
        }

        for (AuditEntry e : recruitment.getAuditEntries()) {
            auditTableModel.addRow(new Object[]{e.getTimestamp(), e.getActor(), e.getAction(), e.getDetails()});
        }
        
        activeJobs = recruitment.getJobPostings().size();
        applications = recruitment.getApplications().size();
        interviews = countInterviewed; // Use stage-based count
        offers = countOffered;         // Use stage-based count
        refreshMetrics();
        pipelinePanel.updateValues(countApplied, countScreened, countInterviewed, countOffered);
        analyticsPanel.refresh();
    }

    private void downloadOfferDocument(String offerId) {
        try {
            String html = recruitment.generateOfferLetterHtml(offerId);
            String fileName = "Offer_" + offerId + ".html";
            Path path = Paths.get(System.getProperty("user.home"), "Downloads", fileName);
            java.nio.file.Files.writeString(path, html);
            JOptionPane.showMessageDialog(this, "Offer letter saved to your Downloads folder: " + fileName, "Download Success", JOptionPane.INFORMATION_MESSAGE);
            notificationArea.append("Downloaded offer document: " + fileName + "\n");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to download offer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String resolveCandidateName(String cid) {
        if (cid == null) return "Unknown";
        
        // Try direct lookup
        String name = recruitment.getCandidates().stream()
                .filter(c -> c.getCandidateId().equals(cid))
                .findFirst().map(Candidate::getName).orElse(null);
        
        if (name != null) return name;

        // Try lookup via application ID if cid looks like an App ID
        if (cid.startsWith("APP-")) {
            return recruitment.getApplications().stream()
                .filter(a -> a.getApplicationId().equals(cid))
                .findFirst()
                .map(a -> resolveCandidateName(a.getCandidateId()))
                .orElse("Unknown candidate for: " + cid);
        }

        return "Unknown (" + cid + ")";
    }

    private String resolveJobTitle(String jobId) {
        return recruitment.getJobPostings().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst().map(JobPosting::getTitle).orElse("Unknown Job");
    }

    private Candidate resolveCandidateByAppId(String appId) {
        Application app = recruitment.getApplications().stream()
                .filter(a -> a.getApplicationId().equals(appId))
                .findFirst().orElse(null);
        if (app == null) return null;
        return recruitment.getCandidates().stream()
                .filter(c -> c.getCandidateId().equals(app.getCandidateId()))
                .findFirst().orElse(null);
    }

    private static String nextId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void runDbSave(Runnable saveAction) {
        if (saveToDatabaseBox.isSelected()) {
            new Thread(() -> {
                try {
                    saveAction.run();
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("System | WARNING: Database save failed: " + ex.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        }
    }

    private void fullSyncFromDb() {
        new Thread(() -> {
            try {
                RecruitmentRepository repo = new RecruitmentRepository(new DatabaseConnection());
                recruitment.setRepository(repo);
                
                List<JobPosting> jobs = repo.getAllJobPostings();
                recruitment.loadInitialData(
                    jobs,
                    repo.getAllCandidates(),
                    repo.getAllApplications(),
                    repo.getAllApplicationStatuses(),
                    repo.getAllScreeningResults(),
                    repo.getAllInterviewSchedules(),
                    repo.getAllOffers(),
                    repo.getAllNotificationLogs()
                );
                SwingUtilities.invokeLater(() -> {
                    refreshAllViews();
                    statusLabel.setText("System | Dashboard synced with database.");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("System | ERROR: Sync failed: " + ex.getMessage());
                });
            }
        }).start();
    }


    private void refreshPortal() {
        for (Component c : mainContentPanel.getComponents()) {
            if (c instanceof CareerPortalPanel) {
                ((CareerPortalPanel) c).refresh();
                break;
            }
        }
    }

    private <T> void populateTable(JTable table, java.util.Collection<T> data, java.util.function.Function<T, Object[]> mapper) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (T item : data) {
            model.addRow(mapper.apply(item));
        }
    }

    private JPanel createOfferActionButtons() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton generateOfferBtn = primaryButton("Generate Offer");
        generateOfferBtn.addActionListener(e -> {
            int row = offersTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a candidate from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = offersTable.convertRowIndexToModel(row);
            String appId = (String) offersTable.getModel().getValueAt(modelRow, 0);
            
            Application app = recruitment.getApplications().stream()
                    .filter(a -> a.getApplicationId().equals(appId)).findFirst().orElse(null);
            
            if (app != null) {
                new RecordOfferDialog(this, appId).setVisible(true);
            }
        });

        JButton downloadOfferBtn = primaryButton("Download Letter");
        downloadOfferBtn.addActionListener(e -> {
            int row = offersTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an application from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = offersTable.convertRowIndexToModel(row);
            String appId = (String) offersTable.getModel().getValueAt(modelRow, 0);
            
            Offer offer = recruitment.getOffers().stream()
                    .filter(o -> o.getCandidateId().equals(resolveCandidateIdByAppId(appId)))
                    .findFirst().orElse(null);
            
            if (offer != null) {
                downloadOfferDocument(offer.getOfferId());
            } else {
                JOptionPane.showMessageDialog(this, "No offer generated yet for this candidate.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            }
        });

        actions.add(downloadOfferBtn);
        actions.add(generateOfferBtn);
        return actions;
    }

    private String resolveCandidateIdByAppId(String appId) {
        return recruitment.getApplications().stream()
                .filter(a -> a.getApplicationId().equals(appId))
                .findFirst().map(Application::getCandidateId).orElse(null);
    }

    private JPanel createApplicationActionButtons() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton shortlistBtn = primaryButton("✓ Shortlist");
        shortlistBtn.addActionListener(e -> {
            String appId = getSelectedApplicationId();
            if (appId == null) return;
            try {
                recruitment.shortlistApplication(appId);
                runDbSave(() -> {
                    try { new RecruitmentRepository(new DatabaseConnection()).saveApplicationStatus(recruitment.getStatus(appId)); }
                    catch (Exception ex) { throw new RuntimeException(ex); }
                });
                refreshAllViews();
                notificationArea.append("Application Shortlisted: " + appId + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton rejectBtn = primaryButton("✗ Reject");
        rejectBtn.setBackground(new Color(180, 80, 80));
        rejectBtn.addActionListener(e -> {
            String appId = getSelectedApplicationId();
            if (appId == null) return;
            try {
                recruitment.rejectApplication(appId);
                runDbSave(() -> {
                    try { new RecruitmentRepository(new DatabaseConnection()).saveApplicationStatus(recruitment.getStatus(appId)); }
                    catch (Exception ex) { throw new RuntimeException(ex); }
                });
                refreshAllViews();
                notificationArea.append("Application Rejected: " + appId + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton viewResumeBtn = primaryButton("View Resume");
        viewResumeBtn.addActionListener(e -> {
            String appId = getSelectedApplicationId();
            if (appId == null) return;
            try {
                Application app = recruitment.getApplications().stream()
                        .filter(a -> a.getApplicationId().equals(appId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Application not found: " + appId));
                
                Candidate candidate = recruitment.getCandidates().stream()
                        .filter(c -> c.getCandidateId().equals(app.getCandidateId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Candidate not found: " + app.getCandidateId()));
                
                String resumePath = candidate.getResume();
                if (resumePath != null && !resumePath.trim().isEmpty()) {
                    Desktop.getDesktop().open(new File(resumePath));
                } else {
                    JOptionPane.showMessageDialog(this, "No resume on file.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(viewResumeBtn);
        actions.add(shortlistBtn);
        actions.add(rejectBtn);
        return actions;
    }

    private String getSelectedApplicationId() {
        int row = applicationsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an application first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = applicationsTable.convertRowIndexToModel(row);
        return applicationsTable.getModel().getValueAt(modelRow, 0).toString();
    }

    private static class PipelinePanel extends JPanel {

        private int applied = 0;
        private int screened = 0;
        private int interviewed = 0;
        private int offered = 0;
        PipelinePanel() {
            setPreferredSize(new Dimension(330, 190));
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(45, 50, 55)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        }

        void updateValues(int applied, int screened, int interviewed, int offered) {
            this.applied = applied;
            this.screened = screened;
            this.interviewed = interviewed;
            this.offered = offered;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            graphics.setColor(TEXT);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 15));
            graphics.drawString("Application Pipeline", 15, 25);

            int max = Math.max(1, Math.max(applied, Math.max(screened, Math.max(interviewed, offered))));
            drawBar(graphics, "Applied", applied, max, 45, ACCENT);
            drawBar(graphics, "Screened", screened, max, 78, new Color(110, 176, 196));
            drawBar(graphics, "Interviewed", interviewed, max, 111, new Color(144, 180, 143));
            drawBar(graphics, "Offered", offered, max, 144, PINK);
        }

        private void drawBar(Graphics graphics, String label, int value, int max, int y, Color color) {
            graphics.setColor(TEXT);
            graphics.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            graphics.drawString(label, 18, y + 12);
            int width = value == 0 ? 0 : Math.max(6, (int) (170.0 * value / max));
            graphics.setColor(color);
            graphics.fillRect(108, y, width, 12);
            graphics.setColor(MUTED);
            graphics.drawString(String.valueOf(value), 288, y + 12);
        }
    }

    private abstract class BaseDialog extends JDialog {
        BaseDialog(JFrame parent, String title) {
            super(parent, title, true);
            setLayout(new BorderLayout(10, 10));
        }

        protected JTextField field(String value) {
            return new JTextField(value, 24);
        }

        protected String text(JTextField field) {
            return field.getText().trim();
        }

        protected void require(JTextField field, String label) {
            if (text(field).isEmpty()) {
                throw new IllegalArgumentException(label + " is required.");
            }
        }
        
        protected void requirePositiveAmount(JTextField field, String label) {
            try {
                BigDecimal value = new BigDecimal(text(field));
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(label + " must be greater than zero.");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(label + " must be a valid number.");
            }
        }

        protected void requireDate(JTextField field, String label) {
            try {
                LocalDate.parse(text(field));
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(label + " must be in yyyy-MM-dd format.");
            }
        }

        protected void requireTime(JTextField field, String label) {
            try {
                LocalTime.parse(text(field));
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(label + " must be in HH:mm format.");
            }
        }

        protected void requireInteger(JTextField field, String label) {
            try {
                int value = Integer.parseInt(text(field));
                if (value < 0) {
                    throw new IllegalArgumentException(label + " must be a non-negative number.");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(label + " must be a valid whole number.");
            }
        }

        protected int addDialogField(JPanel panel, int row, String labelText, Component component) {
            panel.add(new JLabel(labelText), dialogConstraints(row, 0));
            GridBagConstraints fieldConstraints = dialogConstraints(row, 1);
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.weightx = 1.0;
            panel.add(component, fieldConstraints);
            return row + 1;
        }

        protected GridBagConstraints dialogConstraints(int row, int column) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = column;
            constraints.gridy = row;
            constraints.insets = new Insets(4, 4, 4, 4);
            constraints.anchor = GridBagConstraints.WEST;
            return constraints;
        }
    }

    private class PostJobDialog extends BaseDialog {
        private final JTextField jobTitle = field("");
        private final JComboBox<String> department = new JComboBox<>(new String[]{"Engineering", "Sales", "Human Resources", "Marketing", "Finance", "Other"});
        private final JTextField description = field("");
        private final JTextField requiredSkills = field("");
        private final JTextField salary = field("");
        private final JTextField platform = field("");
        private final JComboBox<String> channelSelector = new JComboBox<>(new String[]{"EXTERNAL", "INTERNAL"});
        private final JTextField experience = field("");

        PostJobDialog(JFrame parent) {
            super(parent, "Request Job Requisition");
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            row = addDialogField(panel, row, "Title", jobTitle);
            row = addDialogField(panel, row, "Department", department);
            row = addDialogField(panel, row, "Description", description);
            row = addDialogField(panel, row, "Required Skills (CSV)", requiredSkills);
            row = addDialogField(panel, row, "Salary", salary);
            row = addDialogField(panel, row, "Channel", channelSelector);
            row = addDialogField(panel, row, "Platform (Optional)", platform);
            row = addDialogField(panel, row, "Experience (yrs)", experience);
            
            JLabel info = new JLabel("Saved as DRAFT. Use Approve then Publish to activate.");
            info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            info.setForeground(MUTED);
            panel.add(info, dialogConstraints(row++, 1));

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Submit Requisition");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void submit() {
            try {
                require(jobTitle, "Job title");
                require(description, "Description");
                requirePositiveAmount(salary, "Salary");
                requireInteger(experience, "Experience");

                String skills = text(requiredSkills).trim();
                JobPosting jobPosting = new JobPostingBuilder()
                        .withJobId(nextId("JOB"))
                        .withTitle(text(jobTitle))
                        .withDepartment(department.getSelectedItem().toString())
                        .withDescription(text(description))
                        .withRequiredSkills(skills.isEmpty() ? null : skills)
                        .withSalary(new BigDecimal(text(salary)))
                        .withStatus(JobStatus.DRAFT)
                        .withPlatform(text(platform))
                        .withChannel(ChannelType.valueOf(channelSelector.getSelectedItem().toString()))
                        .withPostedDate(LocalDate.now())
                        .withExpiryDate(LocalDate.now().plusDays(30))
                        .withExperience(Integer.parseInt(text(experience)))
                        .build();

                recruitment.submitJobRequisition(jobPosting);
                runDbSave(() -> {
                    try {
                        new RecruitmentRepository(new DatabaseConnection()).saveJobPosting(jobPosting);
                    } catch (Exception ex) {
                        throw new RuntimeException("Save Job failed: " + ex.getMessage(), ex);
                    }
                });

                refreshAllViews();
                notificationArea.append("Job Posted: " + jobPosting.getTitle() + "\n");
                statusLabel.setText("System | Job Posting completed.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class AddCandidateDialog extends BaseDialog {
        private final JTextField candidateName = field("");
        private final JTextField contact = field("");
        private final JTextField resume = field("");
        private final JTextField experience = field("");
        private final JTextField skills = field("");
        private final JTextField source = field("");

        AddCandidateDialog(JFrame parent) {
            super(parent, "Add Candidate");
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            row = addDialogField(panel, row, "Candidate Name", candidateName);
            row = addDialogField(panel, row, "Contact / Email", contact);
            
            JPanel resumePanel = new JPanel(new BorderLayout(4, 0));
            resume.setEditable(false);
            resumePanel.add(resume, BorderLayout.CENTER);
            JButton browseButton = new JButton("Browse...");
            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Resumes (PDF, TXT)", "pdf", "txt"));
                if (fileChooser.showOpenDialog(AddCandidateDialog.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    resume.setText(selectedFile.getAbsolutePath());
                    smartParseResume(selectedFile);
                }
            });
            resumePanel.add(browseButton, BorderLayout.EAST);
            row = addDialogField(panel, row, "Resume", resumePanel);

            row = addDialogField(panel, row, "Experience", experience);
            row = addDialogField(panel, row, "Skills", skills);
            row = addDialogField(panel, row, "Source", source);

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Add Talent");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private String readFileContent(File file) {
            try {
                return java.nio.file.Files.readString(file.toPath());
            } catch (Exception e) {
                return "";
            }
        }

        private void smartParseResume(File file) {
            String fileName = file.getName();
            String content = "";
            
            if (fileName.toLowerCase().endsWith(".txt")) {
                content = readFileContent(file);
            }

            // 1. Label-based search (Name: )
            java.util.regex.Matcher nameMatcher = java.util.regex.Pattern.compile("(?i)Name:\\s*([^\\n\\r]+)").matcher(content);
            if (nameMatcher.find()) {
                candidateName.setText(nameMatcher.group(1).trim());
            }

            // 2. Fallback: Check first non-empty line (many resumes have name at the very top)
            if (candidateName.getText().trim().isEmpty() && !content.isEmpty()) {
                String[] lines = content.split("\\r?\\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        candidateName.setText(line.trim());
                        break;
                    }
                }
            }

            // 3. Fallback: Use cleaned filename
            if (candidateName.getText().trim().isEmpty()) {
                String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
                String cleanedName = baseName.replace("_", " ").replace("-", " ");
                String[] parts = cleanedName.split("\\s+");
                if (parts.length >= 2) {
                    candidateName.setText(parts[0] + " " + parts[1]);
                } else if (parts.length == 1) {
                    candidateName.setText(parts[0]);
                }
            }

            // Wider Skills Dictionary Logic
            String sourceText = (content + " " + fileName).toLowerCase();
            java.util.Set<String> foundSkills = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            
            String[] widerSkillset = {
                "Java", "Spring", "SpringBoot", "Hibernate", "Microservices", "REST",
                "Python", "Django", "Flask", "Pandas", "NumPy", "TensorFlow", "PyTorch",
                "Node", "Express", "React", "Angular", "Vue", "JavaScript", "TypeScript",
                "HTML", "CSS", "SQL", "PostgreSQL", "MongoDB", "Redis", "Elasticsearch",
                "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "Git", "DevOps",
                "C++", "C#", ".NET", "PHP", "Laravel", "Swift", "Kotlin", "Android", "iOS",
                "Machine Learning", "Data Science", "AI", "Cloud Computing"
            };

            for (String s : widerSkillset) {
                // Using the robust regex matching logic I implemented for SkillMatchRule
                String regex = "(?i)(?<=^|[^a-zA-Z0-9])" + java.util.regex.Pattern.quote(s) + "(?=$|[^a-zA-Z0-9])";
                if (java.util.regex.Pattern.compile(regex).matcher(sourceText).find()) {
                    foundSkills.add(s);
                }
            }

            if (!foundSkills.isEmpty() && skills.getText().trim().isEmpty()) {
                skills.setText(String.join(", ", foundSkills));
            }
            
            String logMsg = content.isEmpty() ? "Extracted from filename" : "Extracted from file content";
            statusLabel.setText("System | Smart Parse: " + logMsg + " for " + fileName);
        }

        private void submit() {
            try {
                require(candidateName, "Candidate name");
                require(contact, "Contact");
                requireInteger(experience, "Experience");
                require(skills, "Skills");
                require(source, "Source");
                require(resume, "Resume");
                String resumePath = text(resume).toLowerCase();
                if (!resumePath.endsWith(".pdf") && !resumePath.endsWith(".txt")) {
                    throw new IllegalArgumentException("Resume must be a valid .pdf or .txt file.");   
                }

                Candidate candidate = new Candidate();
                candidate.setCandidateId(nextId("CAN"));
                candidate.setName(text(candidateName));
                candidate.setContactInfo(text(contact));
                candidate.setResume(text(resume));
                candidate.setSkills(text(skills));
                candidate.setSource(text(source));
                candidate.setExperience(Integer.parseInt(text(experience)));

                recruitment.addCandidate(candidate);
                runDbSave(() -> {
                    try {
                        new RecruitmentRepository(new DatabaseConnection()).saveCandidate(candidate);
                    } catch (Exception ex) {
                        throw new RuntimeException("Save Candidate failed: " + ex.getMessage(), ex);
                    }
                });

                refreshAllViews();
                notificationArea.append("Candidate Added: " + candidate.getName() + " to the talent pool.\n");
                statusLabel.setText("System | Candidate profile created.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ApplyDialog extends BaseDialog {
        private final JComboBox<String> jobDropdown = new JComboBox<>();
        private final JComboBox<String> candidateDropdown = new JComboBox<>();

        ApplyDialog(JFrame parent) {
            super(parent, "Submit Application");
            for(JobPosting j : recruitment.getJobPostings()) {
                jobDropdown.addItem(j.getJobId() + " - " + j.getTitle());
            }
            java.util.Set<String> uniqueCands = new java.util.HashSet<>();
            for(Candidate c : recruitment.getCandidates()) {
                String entry = c.getCandidateId() + " - " + c.getName();
                if (uniqueCands.add(entry)) {
                    candidateDropdown.addItem(entry);
                }
            }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            row = addDialogField(panel, row, "Select Candidate", candidateDropdown);
            row = addDialogField(panel, row, "Select Job", jobDropdown);

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Submit Application");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void submit() {
            try {
                if (jobDropdown.getSelectedItem() == null) throw new IllegalArgumentException("No job selected.");
                if (candidateDropdown.getSelectedItem() == null) throw new IllegalArgumentException("No candidate selected.");
                
                String jobId = jobDropdown.getSelectedItem().toString().split(" - ")[0];
                String candidateId = candidateDropdown.getSelectedItem().toString().split(" - ")[0];

                Application application = recruitment.submitApplication(nextId("APP"), candidateId, jobId);

                // --- PIPELINE AUTOMATION: SCREENING ---
                ScreeningResult screeningResult = recruitment.processCandidateApplication(application.getApplicationId());
                
                // --- PIPELINE AUTOMATION: SCREENING ONLY ---
                // Interview scheduling is done manually by HR via "Schedule Interview"

                runDbSave(() -> {
                    try { 
                        RecruitmentRepository repo = new RecruitmentRepository(new DatabaseConnection());
                        repo.saveApplication(application);
                        repo.saveScreeningResult(screeningResult);
                        repo.saveApplicationStatus(recruitment.getStatus(application.getApplicationId()));
                    } catch (Exception ex) {
                        throw new RuntimeException("Save Application failed: " + ex.getMessage(), ex);
                    }
                });

                refreshAllViews();
                
                JobPosting job = recruitment.getJobPostings().stream().filter(j -> j.getJobId().equals(jobId)).findFirst().get();
                JOptionPane.showMessageDialog(this, 
                    "Application submitted for " + job.getTitle() + " via " + job.getPlatformName() + ".",
                    "Application Success", JOptionPane.INFORMATION_MESSAGE);

                StringBuilder msg = new StringBuilder();
                msg.append("Application submitted for: ").append(candidateId).append("\n");
                msg.append("Screening: ").append(screeningResult.getShortlistStatus())
                   .append(" (Score: ").append(screeningResult.getScore()).append(")\n");
                msg.append("Remarks: ").append(screeningResult.getRemarks()).append("\n");
                notificationArea.append(msg.toString());
                
                statusLabel.setText("System | Application submitted and screened.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RecordOfferDialog extends BaseDialog {
        private final JComboBox<String> candidateDropdown = new JComboBox<>();
        private final JComboBox<String> resultSelector = new JComboBox<>(new String[]{"ACCEPT - Issue Offer", "REJECT - Decline Candidate"});
        private final JTextField offerDetails = field("");
        private final JTextField offerSalary = field("");
        private final JTextField joiningDate = field(LocalDate.now().plusDays(20).toString());

        RecordOfferDialog(JFrame parent) {
            this(parent, null);
        }

        RecordOfferDialog(JFrame parent, String preSelectedAppId) {
            super(parent, "Record Job Offer");
            
            String targetItem = null;
            for (Application a : recruitment.getApplications()) {
                ApplicationStatus status = recruitment.getStatus(a.getApplicationId());
                if (status != null && ("INTERVIEW_COMPLETED".equals(status.getCurrentStage()) || "OFFERED".equals(status.getCurrentStage()))) {
                    String candidateName = resolveCandidateName(a.getCandidateId());
                    String item = a.getApplicationId() + " - " + candidateName;
                    candidateDropdown.addItem(item);
                    
                    if (a.getApplicationId().equals(preSelectedAppId)) {
                        targetItem = item;
                    }
                }
            }

            if (candidateDropdown.getItemCount() == 0) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent, "No candidates are ready for an offer yet.", "Warning", JOptionPane.WARNING_MESSAGE);
                    dispose();
                });
                return;
            }

            if (targetItem != null) {
                candidateDropdown.setSelectedItem(targetItem);
            }
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            row = addDialogField(panel, row, "Select Candidate", candidateDropdown);
            row = addDialogField(panel, row, "Interview Result", resultSelector);
            row = addDialogField(panel, row, "Offer Details", offerDetails);
            row = addDialogField(panel, row, "Offer Salary", offerSalary);
            row = addDialogField(panel, row, "Joining Date", joiningDate);

            // Dynamic logic: disable info fields if rejecting
            java.util.function.Consumer<Boolean> toggleFields = (isAccept) -> {
                offerDetails.setEnabled(isAccept);
                offerSalary.setEnabled(isAccept);
                joiningDate.setEnabled(isAccept);
                offerDetails.setBackground(isAccept ? Color.WHITE : new Color(240, 240, 240));
                offerSalary.setBackground(isAccept ? Color.WHITE : new Color(240, 240, 240));
                joiningDate.setBackground(isAccept ? Color.WHITE : new Color(240, 240, 240));
            };

            resultSelector.addActionListener(e -> {
                boolean isAccept = resultSelector.getSelectedIndex() == 0;
                toggleFields.accept(isAccept);
            });
            
            // Trigger default state
            toggleFields.accept(true);

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Issue Offer");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void submit() {
            try {
                if(candidateDropdown.getSelectedItem() == null) throw new IllegalArgumentException("No candidate selected.");
                String candStr = candidateDropdown.getSelectedItem().toString();
                String selectedAppId = candStr.split(" - ")[0];
                
                // Resolve the CANDIDATE ID from the Application ID
                Application selectedApp = recruitment.getApplications().stream()
                    .filter(a -> a.getApplicationId().equals(selectedAppId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Application not found: " + selectedAppId));
                String candidateId = selectedApp.getCandidateId();
                
                boolean isAccept = resultSelector.getSelectedIndex() == 0;

                if (isAccept) {
                    require(offerDetails, "Offer Details");
                    requirePositiveAmount(offerSalary, "Offer Salary");
                    requireDate(joiningDate, "Joining Date");

                    Offer offer = new OfferBuilder()
                            .withOfferId(nextId("OFF"))
                            .forCandidate(candidateId)
                            .withDetails(text(offerDetails))
                            .withSalary(new BigDecimal(text(offerSalary)))
                            .startingOn(LocalDate.parse(text(joiningDate)))
                            .expiringOn(LocalDate.now().plusDays(7))
                            .withStatus(OfferStatus.PENDING)
                            .build();
                    
                    recruitment.recordOffer(offer);

                    runDbSave(() -> {
                        try { 
                            RecruitmentRepository repo = new RecruitmentRepository(new DatabaseConnection());
                            repo.saveOffer(offer);
                            repo.saveApplicationStatus(recruitment.getStatusByCandidateId(candidateId)); 
                        } catch (Exception ex) {
                            throw new RuntimeException("Save Offer failed: " + ex.getMessage(), ex);
                        }
                    });
                    
                    notificationArea.append("Decision: OFFER ISSUED to " + candidateId + "\n");
                } else {
                    // REJECTED branch
                    recruitment.declineCandidate(candidateId);
                    
                    runDbSave(() -> {
                        try { 
                            new RecruitmentRepository(new DatabaseConnection())
                                .saveApplicationStatus(recruitment.getStatusByCandidateId(candidateId)); 
                        } catch (Exception ex) {
                            throw new RuntimeException("Update Status failed: " + ex.getMessage(), ex);
                        }
                    });
                    
                    notificationArea.append("Decision: CANDIDATE REJECTED (ID: " + candidateId + ")\n");
                }

                refreshAllViews();
                statusLabel.setText("System | Interview evaluation recorded.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ScheduleInterviewDialog extends BaseDialog {
        private final JComboBox<String> applicationDropdown = new JComboBox<>();
        private final JTextField interviewerId = field("INT-SYS");
        private final JTextField interviewDate = field(LocalDate.now().plusDays(1).toString());
        private final JTextField interviewTime = field("10:00");
        private final JComboBox<String> typeDropdown = new JComboBox<>(new String[]{"TECHNICAL", "HR_ROUND", "VIDEO_CALL", "IN_PERSON"});

        ScheduleInterviewDialog(JFrame parent) {
            this(parent, null);
        }

        ScheduleInterviewDialog(JFrame parent, String preSelectedAppId) {
            super(parent, "Schedule Manual Interview");
            String targetItem = null;
            for (Application a : recruitment.getApplications()) {
                ApplicationStatus status = recruitment.getStatus(a.getApplicationId());
                if (status != null && "SHORTLISTED".equals(status.getCurrentStage())) {
                    String cName = resolveCandidateName(a.getCandidateId());
                    String item = a.getApplicationId() + " - " + cName;
                    applicationDropdown.addItem(item);
                    if (a.getApplicationId().equals(preSelectedAppId)) targetItem = item;
                }
            }

            if (applicationDropdown.getItemCount() == 0) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent, "No candidates are in Shortlisted stage ready for scheduling.", "Warning", JOptionPane.WARNING_MESSAGE);
                    dispose();
                });
                return;
            }

            if (targetItem != null) applicationDropdown.setSelectedItem(targetItem);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            row = addDialogField(panel, row, "Select Application", applicationDropdown);
            row = addDialogField(panel, row, "Interviewer ID", interviewerId);
            row = addDialogField(panel, row, "Date (yyyy-MM-dd)", interviewDate);
            row = addDialogField(panel, row, "Time (HH:mm)", interviewTime);
            row = addDialogField(panel, row, "Type", typeDropdown);

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Schedule");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void submit() {
            try {
                if (applicationDropdown.getSelectedItem() == null) throw new IllegalArgumentException("No application selected.");
                String appId = applicationDropdown.getSelectedItem().toString().split(" - ")[0];
                Application app = recruitment.getApplications().stream()
                        .filter(a -> a.getApplicationId().equals(appId)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

                require(interviewerId, "Interviewer ID");
                requireDate(interviewDate, "Date");
                requireTime(interviewTime, "Time");

                LocalDate parsedDate = LocalDate.parse(text(interviewDate));
                LocalTime parsedTime = LocalTime.parse(text(interviewTime));
                LocalDateTime scheduledAt = LocalDateTime.of(parsedDate, parsedTime);
                LocalDateTime now = LocalDateTime.now();

                if (!scheduledAt.isAfter(now)) {
                    throw new IllegalArgumentException(
                        "ERROR: Past dates are forbidden.\n"
                        + "Current System Time: " + now.toString().replace("T", " ").substring(0, 19) + "\n"
                        + "Your Selected Time:  " + scheduledAt.toString().replace("T", " ").substring(0, 19) + "\n\n"
                        + "Please select a time in the future.");
                }

                InterviewSchedule schedule = new InterviewSchedule();
                schedule.setScheduleId(nextId("INT"));
                schedule.setCandidateId(app.getCandidateId());
                schedule.setInterviewerId(text(interviewerId));
                schedule.setInterviewDate(parsedDate);
                schedule.setInterviewTime(parsedTime);
                schedule.setInterviewType(typeDropdown.getSelectedItem().toString());

                recruitment.scheduleInterview(schedule);

                runDbSave(() -> {
                    try {
                        new RecruitmentRepository(new DatabaseConnection()).saveInterviewSchedule(schedule);
                    } catch (Exception ex) {
                        throw new RuntimeException("Save Interview failed: " + ex.getMessage(), ex);
                    }
                });

                refreshAllViews();
                notificationArea.append("Interview scheduled for application: " + appId + "\n");
                statusLabel.setText("System | Manual interview scheduled.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RecordInterviewResultDialog extends BaseDialog {
        private final JComboBox<String> applicationDropdown = new JComboBox<>();
        private final JTextField scoreField = field("");
        private final JTextField feedbackField = field("");
        private final JComboBox<String> outcomeDropdown = new JComboBox<>(new String[]{"PASS", "FAIL"});

        RecordInterviewResultDialog(JFrame parent) {
            this(parent, null);
        }

        RecordInterviewResultDialog(JFrame parent, String preSelectedAppId) {
            super(parent, "Record Interview Result");
            String targetItem = null;
            for (Application a : recruitment.getApplications()) {
                ApplicationStatus status = recruitment.getStatus(a.getApplicationId());
                if (status != null && "INTERVIEW_SCHEDULED".equals(status.getCurrentStage())) {
                    String cName = resolveCandidateName(a.getCandidateId());
                    String item = a.getApplicationId() + " - " + cName;
                    applicationDropdown.addItem(item);
                    if (a.getApplicationId().equals(preSelectedAppId)) targetItem = item;
                }
            }

            if (applicationDropdown.getItemCount() == 0) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent, "No candidates are currently in INTERVIEW_SCHEDULED stage - use 'Interview Now' instead.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                });
                return;
            }

            if (targetItem != null) applicationDropdown.setSelectedItem(targetItem);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            row = addDialogField(panel, row, "Select Application", applicationDropdown);
            row = addDialogField(panel, row, "Score (0-100)", scoreField);
            row = addDialogField(panel, row, "Feedback", feedbackField);
            row = addDialogField(panel, row, "Outcome", outcomeDropdown);

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Submit Result");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void submit() {
            try {
                if (applicationDropdown.getSelectedItem() == null) throw new IllegalArgumentException("No application selected.");
                String appId = applicationDropdown.getSelectedItem().toString().split(" - ")[0];
                int score = Integer.parseInt(text(scoreField));
                if (score < 0 || score > 100) throw new IllegalArgumentException("Score must be between 0 and 100.");
                String feedback = text(feedbackField);
                if (feedback.isEmpty()) throw new IllegalArgumentException("Feedback is required.");

                String outcome = outcomeDropdown.getSelectedItem().toString();
                String candidateName = applicationDropdown.getSelectedItem().toString().split(" - ")[1];

                recruitment.completeInterview(appId);

                if ("PASS".equals(outcome)) {
                    notificationArea.append("Interview PASSED: " + candidateName + " — Score: " + score + ". Proceed to offer.\n");
                    statusLabel.setText("System | Interview PASSED. Ready for offer.");
                    statusLabel.setForeground(new Color(46, 125, 50));
                } else {
                    recruitment.rejectApplication(appId);
                    notificationArea.append("Interview FAILED: " + candidateName + " — Application rejected.\n");
                    statusLabel.setText("System | Interview FAILED. Application rejected.");
                    statusLabel.setForeground(new Color(198, 40, 40));
                }

                runDbSave(() -> {
                    try {
                        new RecruitmentRepository(new DatabaseConnection()).saveApplicationStatus(recruitment.getStatus(appId));
                    } catch (Exception ex) {
                        throw new RuntimeException("Save status failed: " + ex.getMessage(), ex);
                    }
                });

                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Interview result recorded for " + candidateName + ".");
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Submission Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CareerPortalPanel extends JPanel {
        private final JPanel cardsContainer = new JPanel();
        private final JTextField searchField = new JTextField(20);

        CareerPortalPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);

            JLabel title = new JLabel("Global Talent Portal - Explore Opportunities");
            title.setForeground(ACCENT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
            add(title, BorderLayout.NORTH);

            cardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
            cardsContainer.setOpaque(false);
            
            JScrollPane scroll = new JScrollPane(cardsContainer);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);

            searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            searchField.setForeground(TEXT);
            searchField.setCaretColor(TEXT);
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
                private void search() {
                    String query = searchField.getText();
                    cardsContainer.removeAll();
                    recruitment.getJobPostings().stream()
                        .filter(j -> JobStatus.ACTIVE.name().equals(j.getStatus()) && (query.isEmpty() || j.getTitle().toLowerCase().contains(query.toLowerCase())))
                        .forEach(j -> cardsContainer.add(new JobCard(j)));
                    cardsContainer.revalidate();
                    cardsContainer.repaint();
                }
            });
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            searchPanel.setOpaque(false);
            searchPanel.add(new JLabel("🔍  Search Career Portal: ") {{ setForeground(MUTED); }});
            searchPanel.add(searchField);
            add(searchPanel, BorderLayout.NORTH);

            JPanel trackingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            trackingPanel.setOpaque(false);
            trackingPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JTextField trackField = new JTextField(15);
            trackField.setBackground(new Color(30, 32, 35));
            trackField.setForeground(ACCENT);
            JButton trackBtn = primaryButton("Track Application");
            trackBtn.addActionListener(e -> {
                String id = trackField.getText().trim();
                if (id.isEmpty()) return;
                ApplicationStatus status = recruitment.getStatus(id);
                if (status != null) {
                    JOptionPane.showMessageDialog(this, "Application " + id + " Status: " + status.getCurrentStage() + "\nHistory: " + status.getHistory(), "Application Tracking", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Application ID not found.", "Tracking Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            trackingPanel.add(new JLabel("Track Application ID: ") {{ setForeground(MUTED); }});
            trackingPanel.add(trackField);
            trackingPanel.add(trackBtn);
            add(trackingPanel, BorderLayout.SOUTH);
        }

        void refresh() {
            cardsContainer.removeAll();
            java.util.List<JobPosting> activeJobsList = recruitment.getJobPostings().stream()
                    .filter(j -> JobStatus.ACTIVE.name().equals(j.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

            if (activeJobsList.isEmpty()) {
                JLabel empty = new JLabel("No active openings at the moment. Check back soon!");
                empty.setForeground(MUTED);
                empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                empty.setBorder(BorderFactory.createEmptyBorder(50, 50, 0, 0));
                cardsContainer.add(empty);
            } else {
                for (JobPosting job : activeJobsList) {
                    cardsContainer.add(new JobCard(job));
                }
            }
            cardsContainer.revalidate();
            cardsContainer.repaint();
        }

        private class JobCard extends JPanel {
            JobCard(JobPosting job) {
                setPreferredSize(new Dimension(280, 180));
                setBackground(PANEL_BG);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(45, 50, 55)),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));
                setLayout(new BorderLayout(5, 10));

                JPanel top = new JPanel(new GridLayout(2, 1));
                top.setOpaque(false);
                JLabel titleLbl = new JLabel(job.getTitle());
                titleLbl.setForeground(TEXT);
                titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
                top.add(titleLbl);

                JLabel deptLbl = new JLabel(job.getDepartment() + " | " + job.getPlatformName());
                deptLbl.setForeground(ACCENT);
                deptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                top.add(deptLbl);
                add(top, BorderLayout.NORTH);

                JTextArea desc = new JTextArea(job.getDescription());
                desc.setEditable(false);
                desc.setLineWrap(true);
                desc.setWrapStyleWord(true);
                desc.setBackground(PANEL_BG);
                desc.setForeground(MUTED);
                desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                add(desc, BorderLayout.CENTER);

                JButton applyBtn = primaryButton("Apply Now");
                applyBtn.addActionListener(e -> new PublicApplyDialog((JFrame) SwingUtilities.getWindowAncestor(this), job).setVisible(true));
                add(applyBtn, BorderLayout.SOUTH);
            }
        }
    }

    private class PublicApplyDialog extends BaseDialog {
        private final JobPosting job;
        private final JTextField candidateName = field("");
        private final JTextField contact = field("");
        private final JTextField resume = field("");
        private final JTextField experience = field("");
        private final JTextField skills = field("");

        PublicApplyDialog(JFrame parent, JobPosting job) {
            super(parent, "Apply for " + job.getTitle());
            this.job = job;
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            int row = 0;
            
            JLabel info = new JLabel("Join our " + job.getDepartment() + " team!");
            info.setFont(new Font("Segoe UI", Font.BOLD, 14));
            info.setForeground(ACCENT);
            panel.add(info, dialogConstraints(row++, 0));

            row = addDialogField(panel, row, "Full Name", candidateName);
            row = addDialogField(panel, row, "Contact / Email", contact);
            
            JPanel resumePanel = new JPanel(new BorderLayout(4, 0));
            resume.setEditable(false);
            resumePanel.add(resume, BorderLayout.CENTER);
            JButton browseButton = new JButton("Browse...");
            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Resume Files (.pdf, .docx)", "pdf", "docx"));
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    resume.setText(selectedFile.getAbsolutePath());
                    autoFillFromResume(selectedFile);
                }
            });
            resumePanel.add(browseButton, BorderLayout.EAST);
            row = addDialogField(panel, row, "Resume Path", resumePanel);

            row = addDialogField(panel, row, "Experience (yrs)", experience);
            row = addDialogField(panel, row, "Skills (CSV)", skills);

            add(new JScrollPane(panel), BorderLayout.CENTER);
            JButton run = primaryButton("Submit My Application");
            run.addActionListener(e -> submit());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ev -> dispose());
            actions.add(cancel);
            actions.add(run);
            add(actions, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(parent);
        }

        private void submit() {
            try {
                require(candidateName, "Candidate name");
                require(contact, "Contact");
                requireInteger(experience, "Experience");
                require(skills, "Skills");
                require(resume, "Resume");

                Candidate candidate = new Candidate();
                candidate.setCandidateId(nextId("CAN-PUB"));
                candidate.setName(text(candidateName));
                candidate.setContactInfo(text(contact));
                candidate.setResume(text(resume));
                candidate.setSkills(text(skills));
                candidate.setSource("CAREER-PORTAL");
                candidate.setExperience(Integer.parseInt(text(experience)));

                recruitment.addCandidate(candidate);
                Application application = recruitment.submitApplication(nextId("APP-PUB"), candidate.getCandidateId(), job.getJobId());
                
                // Trigger automated screening for the new portal application
                ScreeningResult screeningResult = recruitment.processCandidateApplication(application.getApplicationId());

                runDbSave(() -> {
                    try {
                        RecruitmentRepository repo = new RecruitmentRepository(new DatabaseConnection());
                        repo.saveCandidate(candidate);
                        repo.saveApplication(application);
                        repo.saveScreeningResult(screeningResult);
                        repo.saveApplicationStatus(recruitment.getStatus(application.getApplicationId()));
                    } catch (Exception ex) {
                        throw new RuntimeException("Portal Submit failed: " + ex.getMessage(), ex);
                    }
                });

                refreshAllViews();
                refreshPortal();
                
                // Candidate Success Message
                JOptionPane.showMessageDialog(this, 
                    "Thank you, " + candidate.getName() + "!\nApplication submitted for " + job.getTitle() + ".\nYou will be contacted if shortlisted.", 
                    "Application Received", JOptionPane.INFORMATION_MESSAGE);
                
                // HR Instant Notification Verdict
                showHrVerdictDialog(candidate, screeningResult);
                
                notificationArea.append("Portal Application: " + candidate.getName() + " applied for " + job.getJobId() + "\n");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void autoFillFromResume(File file) {
            String text = "";
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                text = extractTextFromPdf(file);
            } else if (file.getName().toLowerCase().endsWith(".docx")) {
                text = extractTextFromDocx(file);
            }

            if (text.isEmpty()) return;

            // Simple Auto-fill logic
            if (text.toLowerCase().contains("experience")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\+?\\s*years?").matcher(text.toLowerCase());
                if (m.find()) experience.setText(m.group(1));
            }

            String[] commonSkills = {"Java", "Spring", "SQL", "Python", "C++", "C#", "React", "Angular", "UML", "OOD"};
            java.util.StringJoiner sj = new java.util.StringJoiner(",");
            for (String s : commonSkills) {
                if (text.toLowerCase().contains(s.toLowerCase())) sj.add(s);
            }
            if (sj.length() > 0) skills.setText(sj.toString());
            
            notificationArea.append("Auto-filled details from resume: " + file.getName() + "\n");
        }

        private String extractTextFromPdf(File file) {
            try {
                // PDFBox 2.0.x
                org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(file);
                org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                String text = stripper.getText(document);
                document.close();
                return text;
            } catch (NoClassDefFoundError e) {
                return ""; // Fallback siliently
            } catch (Exception e) {
                return "";
            }
        }

        private String extractTextFromDocx(File file) {
            try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file)) {
                java.util.zip.ZipEntry entry = zipFile.getEntry("word/document.xml");
                if (entry == null) return "";
                try (java.io.InputStream is = zipFile.getInputStream(entry)) {
                    byte[] bytes = is.readAllBytes();
                    String xml = new String(bytes);
                    return xml.replaceAll("<[^>]+>", " ");
                }
            } catch (Exception e) {
                 return "";
            }
        }

        private void showHrVerdictDialog(Candidate c, ScreeningResult r) {
            String verdict = r.isShortlisted() ? "RECOMMENDED" : "LOW MATCH";
            Color verdictColor = r.isShortlisted() ? new Color(46, 125, 50) : new Color(198, 40, 40);
            
            JDialog dialog = new JDialog((Frame)null, "HR Notification - AI Screening Verdict", true);
            dialog.setLayout(new BorderLayout());
            
            JPanel content = new JPanel(new GridLayout(0, 1, 0, 10));
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            content.setBackground(Color.WHITE);
            
            JLabel title = new JLabel("New Application: " + c.getName());
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            content.add(title);
            
            JLabel scoreLabel = new JLabel("AI Screening Score: " + r.getScore() + "/100 (" + verdict + ")");
            scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            scoreLabel.setForeground(verdictColor);
            content.add(scoreLabel);
            
            JTextArea remarks = new JTextArea(r.getRemarks());
            remarks.setWrapStyleWord(true);
            remarks.setLineWrap(true);
            remarks.setEditable(false);
            remarks.setBackground(new Color(245, 245, 245));
            remarks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            remarks.setPreferredSize(new Dimension(350, 100));
            content.add(new JScrollPane(remarks));
            
            JLabel footer = new JLabel("Note: Stage updated to SCREENED. Manual Shortlist required.");
            footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            content.add(footer);
            
            dialog.add(content, BorderLayout.CENTER);
            JButton close = new JButton("Dismiss");
            close.addActionListener(e -> dialog.dispose());
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(close);
            dialog.add(bp, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
}
