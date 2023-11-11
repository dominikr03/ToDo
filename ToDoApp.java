import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ToDoApp extends JFrame {

    private DefaultListModel<JCheckBox> model;
    private JPanel taskPanel;
    private Color lightModeBackground = Color.WHITE;
    private Color lightModeForeground = Color.BLACK;
    private Color darkModeBackground = Color.DARK_GRAY;
    private Color darkModeForeground = Color.WHITE;
    private boolean darkMode = false;
    private JMenuItem darkModeItem; // Verändert zu JMenuItem

    public ToDoApp() {
        model = new DefaultListModel<>();
        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBackground(lightModeBackground);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Datei");
        JMenuItem saveItem = new JMenuItem("Speichern");
        JMenuItem loadItem = new JMenuItem("Laden");
        darkModeItem = new JMenuItem("Dark Mode 🌙"); // Standardmäßig wird der Mond Emoji angezeigt

        saveItem.addActionListener(e -> saveTasks());
        loadItem.addActionListener(e -> loadTasks());
        darkModeItem.addActionListener(e -> toggleDarkMode());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        menuBar.add(darkModeItem);
        setJMenuBar(menuBar);

        JTextField newTaskField = new JTextField(20);
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> {
            addTask(newTaskField.getText());
            newTaskField.setText("");
        });

        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.add(newTaskField);
        panel.add(addButton);
        getContentPane().add(panel, BorderLayout.SOUTH);
        getContentPane().setBackground(lightModeBackground);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        if (darkMode) {
            darkModeItem.setText("Light Mode 🌞"); // Sonnen Emoji für Light Mode
        } else {
            darkModeItem.setText("Dark Mode 🌙"); // Mond Emoji für Dark Mode
        }
        updateColors();
    }

    private void updateColors() {
        Color bgColor = darkMode ? darkModeBackground : lightModeBackground;
        Color fgColor = darkMode ? darkModeForeground : lightModeForeground;
        taskPanel.setBackground(bgColor);
        taskPanel.setForeground(fgColor);
        for (int i = 0; i < model.size(); i++) {
            JCheckBox checkBox = model.get(i);
            checkBox.setForeground(fgColor);
            checkBox.setBackground(bgColor);
        }
        getContentPane().setBackground(bgColor);
        getContentPane().repaint();
    }

    private void addTask(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.addItemListener(new ItemChangeListener());
        checkBox.setForeground(darkMode ? darkModeForeground : lightModeForeground);
        checkBox.setBackground(darkMode ? darkModeBackground : lightModeBackground);
        model.addElement(checkBox);
        taskPanel.add(checkBox);
        refreshTaskPanel();
    }

    private void refreshTaskPanel() {
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private class ItemChangeListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox checkBox = (JCheckBox) e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkBox.setFont(new Font("Arial", Font.ITALIC, 12));
                checkBox.setForeground(Color.GRAY);
            } else {
                int result = JOptionPane.showConfirmDialog(
                        ToDoApp.this,
                        "Der Eintrag wird vollständig gelöscht. Wirklich löschen?",
                        "Eintrag löschen",
                        JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    taskPanel.remove(checkBox);
                    model.removeElement(checkBox);
                    refreshTaskPanel();
                } else {
                    checkBox.setSelected(true);
                }
            }
        }
    }

    private void saveTasks() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < model.size(); i++) {
                    JCheckBox checkBox = model.get(i);
                    writer.write((checkBox.isSelected() ? "done:" : "todo:") + checkBox.getText());
                    writer.newLine();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving tasks to file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadTasks() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            taskPanel.removeAll();
            model.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("done:")) {
                        addTask(line.substring(5));
                        model.lastElement().setSelected(true);
                    } else if (line.startsWith("todo:")) {
                        addTask(line.substring(5));
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading tasks from file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            refreshTaskPanel();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoApp().setVisible(true));
    }
}
