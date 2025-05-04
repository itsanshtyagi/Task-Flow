
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TaskFlowApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}

class Task implements Serializable {
    String title;
    String description;
    String priority;
    String status;
    String dueDate;
    String assignedTo;

    public Task(String title, String description, String priority, String status, String dueDate, String assignedTo) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
    }

    public String toString() {
        return String.format("<html><b>%s</b> [%s]<br/>Priority: %s | Due: %s<br/><i>Assigned to:</i> %s</html>", title, status, priority, dueDate, assignedTo);
    }
}

class LoginScreen extends JFrame {
    public LoginScreen() {
        setTitle("TaskFlow - Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(30, 30, 30));

        JLabel titleLabel = new JLabel("TaskFlow Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JTextField username = new JTextField();
        username.setBorder(BorderFactory.createTitledBorder("Username"));
        JPasswordField password = new JPasswordField();
        password.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 200, 100));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        panel.add(titleLabel);
        panel.add(username);
        panel.add(password);
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword());
            if (user.equals("admin") && pass.equals("admin123")) {
                dispose();
                new DashboardScreen(user);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        });

        setVisible(true);
    }
}

class DashboardScreen extends JFrame {
    private DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private JList<Task> taskList = new JList<>(taskListModel);
    private ArrayList<String> members = new ArrayList<>();
    private final String TASKS_FILE = "tasks.ser";
    private final String MEMBERS_FILE = "members.ser";

    public DashboardScreen(String username) {
        setTitle("TaskFlow - Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadData();

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(35, 35, 45));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JButton addTaskButton = createStyledButton("+ Add Task");
        JButton updateTaskButton = createStyledButton(" Update Task");
        JButton deleteTaskButton = createStyledButton(" Delete Task");
        JButton addMemberButton = createStyledButton(" Add Member");

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(35, 35, 45));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(35, 35, 45));
        buttonPanel.add(addTaskButton);
        buttonPanel.add(updateTaskButton);
        buttonPanel.add(deleteTaskButton);
        buttonPanel.add(addMemberButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);

        taskList.setCellRenderer(new TaskCellRenderer());
        taskList.setBackground(new Color(245, 245, 245));
        taskList.setSelectionBackground(new Color(100, 149, 237));
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Tasks"));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        addTaskButton.addActionListener(e -> addTask());
        updateTaskButton.addActionListener(e -> updateTask());
        deleteTaskButton.addActionListener(e -> deleteTask());
        addMemberButton.addActionListener(e -> addMember());

        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(50, 150, 255));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorder(new LineBorder(new Color(30, 30, 30), 1));
        return button;
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void addTask() {
        String title = JOptionPane.showInputDialog(this, "Enter Task Title:");
        if (title == null || title.trim().isEmpty()) return;

        String description = JOptionPane.showInputDialog(this, "Enter Description:");
        if (description == null || description.trim().isEmpty()) return;

        String[] priorities = {"Low", "Medium", "High"};
        String priority = (String) JOptionPane.showInputDialog(this, "Select Priority:", "Priority", JOptionPane.QUESTION_MESSAGE, null, priorities, priorities[0]);
        if (priority == null) return;

        String[] statuses = {"To Do", "In Progress", "Done"};
        String status = (String) JOptionPane.showInputDialog(this, "Select Status:", "Status", JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
        if (status == null) return;

        String dueDate = JOptionPane.showInputDialog(this, "Enter Due Date (YYYY-MM-DD):");
        if (dueDate == null || dueDate.trim().isEmpty() || !isValidDate(dueDate)) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No members available. Add a member first.");
            return;
        }
        String assignedTo = (String) JOptionPane.showInputDialog(this, "Assign to Member:", "Assign", JOptionPane.QUESTION_MESSAGE, null, members.toArray(), members.get(0));
        if (assignedTo == null) return;

        Task newTask = new Task(title, description, priority, status, dueDate, assignedTo);
        taskListModel.addElement(newTask);
        saveData();
    }

    private void updateTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            Task selectedTask = taskListModel.get(index);

            String updatedTitle = JOptionPane.showInputDialog(this, "Update Title:", selectedTask.title);
            if (updatedTitle == null || updatedTitle.trim().isEmpty()) return;

            String updatedDescription = JOptionPane.showInputDialog(this, "Update Description:", selectedTask.description);
            if (updatedDescription == null || updatedDescription.trim().isEmpty()) return;

            String[] priorities = {"Low", "Medium", "High"};
            String updatedPriority = (String) JOptionPane.showInputDialog(this, "Update Priority:", "Priority", JOptionPane.QUESTION_MESSAGE, null, priorities, selectedTask.priority);
            if (updatedPriority == null) return;

            String[] statuses = {"To Do", "In Progress", "Done"};
            String updatedStatus = (String) JOptionPane.showInputDialog(this, "Update Status:", "Status", JOptionPane.QUESTION_MESSAGE, null, statuses, selectedTask.status);
            if (updatedStatus == null) return;

            String updatedDueDate = JOptionPane.showInputDialog(this, "Update Due Date (YYYY-MM-DD):", selectedTask.dueDate);
            if (updatedDueDate == null || updatedDueDate.trim().isEmpty() || !isValidDate(updatedDueDate)) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
                return;
            }

            String updatedAssignedTo = (String) JOptionPane.showInputDialog(this, "Update Assigned To:", "Assign", JOptionPane.QUESTION_MESSAGE, null, members.toArray(), selectedTask.assignedTo);
            if (updatedAssignedTo == null) return;

            selectedTask.title = updatedTitle;
            selectedTask.description = updatedDescription;
            selectedTask.priority = updatedPriority;
            selectedTask.status = updatedStatus;
            selectedTask.dueDate = updatedDueDate;
            selectedTask.assignedTo = updatedAssignedTo;

            taskList.repaint();
            saveData();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to update.");
        }
    }

    private void deleteTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?");
            if (confirm == JOptionPane.YES_OPTION) {
                taskListModel.remove(index);
                saveData();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.");
        }
    }

    private void addMember() {
        String member = JOptionPane.showInputDialog(this, "Enter member name:");
        if (member != null && !member.trim().isEmpty()) {
            members.add(member);
            JOptionPane.showMessageDialog(this, "Member added: " + member);
            saveData();
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TASKS_FILE))) {
            ArrayList<Task> tasks = new ArrayList<>();
            for (int i = 0; i < taskListModel.size(); i++) tasks.add(taskListModel.get(i));
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MEMBERS_FILE))) {
            oos.writeObject(members);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TASKS_FILE))) {
            ArrayList<Task> tasks = (ArrayList<Task>) ois.readObject();
            for (Task t : tasks) taskListModel.addElement(t);
        } catch (Exception ignored) {}

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MEMBERS_FILE))) {
            members = (ArrayList<String>) ois.readObject();
        } catch (Exception ignored) {}
    }

    class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setBorder(new EmptyBorder(10, 10, 10, 10));
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(new Color(100, 149, 237));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.DARK_GRAY);
            }
            return label;
        }
    }
}
