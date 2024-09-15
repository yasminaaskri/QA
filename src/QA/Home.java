package QA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Home extends JFrame {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> userComboBox;
    private JTextField dueDateField; // Changed from JDateChooser to JTextField
    private JButton createButton;
    private Conn conn;
    private SimpleDateFormat dateFormat;

    public Home() {
        conn = new Conn(); // Initialize database connection
        dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Define the date format

        setTitle("Task Management");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        // Title
        JLabel titleLabel = new JLabel("Task Title:");
        titleField = new JTextField();

        // Description
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        // Due Date
        JLabel dueDateLabel = new JLabel("Due Date (yyyy-MM-dd):");
        dueDateField = new JTextField(); // Using JTextField for manual date entry

        // Assigned To
        JLabel userLabel = new JLabel("Assign To:");
        userComboBox = new JComboBox<>();
        loadUsers();

        // Create Button
        createButton = new JButton("Create Task");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTask();
            }
        });

        // Add components to the frame
        add(titleLabel);
        add(titleField);
        add(descriptionLabel);
        add(scrollPane);
        add(dueDateLabel);
        add(dueDateField); // Added JTextField for date entry
        add(userLabel);
        add(userComboBox);
        add(createButton);

        setVisible(true);
    }

    private void loadUsers() {
        try {
            String query = "SELECT username FROM account";
            ResultSet rs = conn.s.executeQuery(query);

            while (rs.next()) {
                userComboBox.addItem(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTask() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        java.sql.Date dueDate = null;
        String assignedUser = (String) userComboBox.getSelectedItem();

        try {
            // Parse the date from the text field
            String dateString = dueDateField.getText();
            Date date = dateFormat.parse(dateString);
            dueDate = new java.sql.Date(date.getTime());

            // Insert the task into the database
            String query = "INSERT INTO tasks (title, description, status, due_date, assigned_to) VALUES (?, ?, 'Pending', ?, ?)";
            PreparedStatement pstmt = conn.c.prepareStatement(query);
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setDate(3, dueDate);

            // Get the user ID for assignment
            String userIdQuery = "SELECT username FROM account WHERE username = ?";
            PreparedStatement userStmt = conn.c.prepareStatement(userIdQuery);
            userStmt.setString(1, assignedUser);
            ResultSet rs = userStmt.executeQuery();

            if (rs.next()) {
                pstmt.setString(4, assignedUser);
            }

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task created successfully!");
        } catch (ParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating task.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Home();
            }
        });
    }
}
