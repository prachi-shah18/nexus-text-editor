import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Stack;

/**
 * ============================================================
 *                  NEXUS TEXT EDITOR
 * ============================================================
 * Technologies Used:
 * 1. OOP Concepts
 * 2. Java Swing GUI
 * 3. Event Handling
 * 4. File Handling
 * 5. Undo / Redo Logic
 * 6. Dark Mode
 * 7. Status Bar
 * 8. Line & Character Counter
 * 9. Search Feature
 * 10. Auto Save
 *
 * Professional Desktop Application
 *
 * Software Architecture Notes:
 * - Fully Commented Code
 * - Clean Architecture
 * - Easy to Understand
 * - Beginner + Intermediate Friendly
 * ============================================================
 */

public class NexusTextEditor extends JFrame {

    // =============================
    // Main Text Area
    // =============================
    private JTextArea textArea;

    // =============================
    // Status Bar Labels
    // =============================
    private JLabel statusLabel;
    private JLabel fileLabel;

    // =============================
    // File Information
    // =============================
    private File currentFile;
    private boolean isModified = false;

    // =============================
    // Theme Tracking
    // =============================
    private boolean darkMode = false;

    // =============================
    // Undo / Redo Stacks
    // Strong custom logic
    // =============================
    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();

    // =============================
    // Auto Save Timer
    // =============================
    private Timer autoSaveTimer;

    // ============================================================
    // Constructor
    // ============================================================
    public NexusTextEditor() {

        // Frame Title
        setTitle("Nexus Text Editor");

        // Window Size
        setSize(1100, 700);

        // Window Position
        setLocationRelativeTo(null);

        // Close Operation
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Layout
        setLayout(new BorderLayout());

        // Initialize Components
        initializeTextArea();
        initializeMenuBar();
        initializeToolBar();
        initializeStatusBar();
        initializeAutoSave();
        initializeWindowListener();

        // Make Window Visible
        setVisible(true);
    }

    // ============================================================
    // TEXT AREA INITIALIZATION
    // ============================================================
    private void initializeTextArea() {

        textArea = new JTextArea();

        // Smooth Modern UI Theme
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Font Styling
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        // Padding
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Line Wrap
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Smooth scrolling
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Remove ugly borders
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);

        // Store Initial State for Undo
        undoStack.push("");

        // ========================================================
        // Document Listener
        // Detects Changes in Text Area
        // ========================================================
        textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                handleTextChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleTextChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleTextChange();
            }
        });
    }

    // ============================================================
    // HANDLE TEXT CHANGES
    // ============================================================
    private void handleTextChange() {

        isModified = true;

        // Save state for undo feature
        undoStack.push(textArea.getText());

        // Clear redo stack after new typing
        redoStack.clear();

        updateStatusBar();
    }

    // ============================================================
    // MENU BAR
    // ============================================================
    private void initializeMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        // ========================================================
        // FILE MENU
        // ========================================================
        JMenu fileMenu = new JMenu("File");

        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAsFile = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");

        // Keyboard Shortcuts
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

        // Add Actions
        newFile.addActionListener(e -> createNewFile());
        openFile.addActionListener(e -> openFile());
        saveFile.addActionListener(e -> saveFile());
        saveAsFile.addActionListener(e -> saveFileAs());
        exit.addActionListener(e -> exitApplication());

        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // ========================================================
        // EDIT MENU
        // ========================================================
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");
        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        JMenuItem search = new JMenuItem("Search");

        cut.setText("Cut");
        copy.setText("Copy");
        paste.setText("Paste");

        undo.addActionListener(e -> performUndo());
        redo.addActionListener(e -> performRedo());
        search.addActionListener(e -> searchText());

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.addSeparator();
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.addSeparator();
        editMenu.add(search);

        // ========================================================
        // VIEW MENU
        // ========================================================
        JMenu viewMenu = new JMenu("View");

        JMenuItem darkTheme = new JMenuItem("Toggle Dark Mode");

        darkTheme.addActionListener(e -> toggleDarkMode());

        viewMenu.add(darkTheme);

        // ========================================================
        // TOOLS MENU
        // ========================================================
        JMenu toolsMenu = new JMenu("Tools");

        JMenuItem dateTime = new JMenuItem("Insert Date & Time");
        JMenuItem clearAll = new JMenuItem("Clear All");

        dateTime.addActionListener(e -> insertDateAndTime());
        clearAll.addActionListener(e -> clearText());

        toolsMenu.add(dateTime);
        toolsMenu.add(clearAll);

        // ========================================================
        // HELP MENU
        // ========================================================
        JMenu helpMenu = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");

        about.addActionListener(e -> showAboutDialog());

        helpMenu.add(about);

        // Add Menus to Menu Bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    // ============================================================
    // MODERN TOOL BAR
    // ============================================================
    private void initializeToolBar() {

        JToolBar toolBar = new JToolBar();

        // Make toolbar modern looking
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(245, 245, 245));
        toolBar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Custom Styled Buttons
        JButton newBtn = createStyledButton("New");
        JButton openBtn = createStyledButton("Open");
        JButton saveBtn = createStyledButton("Save");
        JButton searchBtn = createStyledButton("Search");
        JButton darkBtn = createStyledButton("Theme");

        newBtn.addActionListener(e -> createNewFile());
        openBtn.addActionListener(e -> openFile());
        saveBtn.addActionListener(e -> saveFile());
        searchBtn.addActionListener(e -> searchText());
        darkBtn.addActionListener(e -> toggleDarkMode());

        toolBar.add(newBtn);
        toolBar.add(Box.createHorizontalStrut(10));

        toolBar.add(openBtn);
        toolBar.add(Box.createHorizontalStrut(10));

        toolBar.add(saveBtn);
        toolBar.add(Box.createHorizontalStrut(10));

        toolBar.add(searchBtn);
        toolBar.add(Box.createHorizontalGlue());

        toolBar.add(darkBtn);

        add(toolBar, BorderLayout.NORTH);
    }

    // ============================================================
    // CREATE MODERN STYLED BUTTON
    // ============================================================
    private JButton createStyledButton(String text) {

        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Rounded border effect
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        button.setBackground(new Color(255, 255, 255));

        // Hover Effect
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(220, 235, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    // ============================================================
    // STATUS BAR
    // ============================================================
    private void initializeStatusBar() {

        JPanel statusPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("Lines: 1 | Characters: 0");
        fileLabel = new JLabel("No File Opened");

        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(fileLabel, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    // ============================================================
    // UPDATE STATUS BAR
    // ============================================================
    private void updateStatusBar() {

        String text = textArea.getText();

        int lines = textArea.getLineCount();
        int characters = text.length();

        statusLabel.setText("Lines: " + lines + " | Characters: " + characters);

        if (currentFile != null) {
            fileLabel.setText(currentFile.getName());
        }
    }

    // ============================================================
    // CREATE NEW FILE
    // ============================================================
    private void createNewFile() {

        if (confirmSaveBeforeAction()) {
            textArea.setText("");
            currentFile = null;
            setTitle("Nexus Text Editor");
            fileLabel.setText("New File");
            isModified = false;
        }
    }

    // ============================================================
    // OPEN FILE
    // ============================================================
    private void openFile() {

        if (!confirmSaveBeforeAction()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();

        // Only show text files
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            currentFile = fileChooser.getSelectedFile();

            try {

                String content = Files.readString(currentFile.toPath());
                textArea.setText(content);

                setTitle(currentFile.getName() + " - Advanced Notepad");

                fileLabel.setText(currentFile.getAbsolutePath());

                isModified = false;

                JOptionPane.showMessageDialog(this,
                        "File Opened Successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {

                JOptionPane.showMessageDialog(this,
                        "Error Opening File!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ============================================================
    // SAVE FILE
    // ============================================================
    private void saveFile() {

        // If file does not exist
        if (currentFile == null) {
            saveFileAs();
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {

            writer.write(textArea.getText());

            isModified = false;

            JOptionPane.showMessageDialog(this,
                    "File Saved Successfully!",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this,
                    "Error Saving File!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // SAVE FILE AS
    // ============================================================
    private void saveFileAs() {

        JFileChooser fileChooser = new JFileChooser();

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            currentFile = fileChooser.getSelectedFile();

            // Add .txt extension automatically
            if (!currentFile.getName().endsWith(".txt")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".txt");
            }

            saveFile();
        }
    }

    // ============================================================
    // SEARCH TEXT FEATURE
    // ============================================================
    private void searchText() {

        String searchWord = JOptionPane.showInputDialog(this,
                "Enter text to search:");

        if (searchWord == null || searchWord.isEmpty()) {
            return;
        }

        String content = textArea.getText();

        int index = content.indexOf(searchWord);

        if (index >= 0) {

            textArea.requestFocus();
            textArea.select(index, index + searchWord.length());

            JOptionPane.showMessageDialog(this,
                    "Text Found!",
                    "Search",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {

            JOptionPane.showMessageDialog(this,
                    "Text Not Found!",
                    "Search",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // ============================================================
    // INSERT DATE & TIME
    // ============================================================
    private void insertDateAndTime() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        String dateTime = LocalDateTime.now().format(formatter);

        textArea.append("\n" + dateTime);
    }

    // ============================================================
    // CLEAR TEXT AREA
    // ============================================================
    private void clearText() {

        int choice = JOptionPane.showConfirmDialog(this,
                "Do you really want to clear everything?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            textArea.setText("");
        }
    }

    // ============================================================
    // DARK MODE FEATURE
    // ============================================================
    private void toggleDarkMode() {

        darkMode = !darkMode;

        if (darkMode) {

            textArea.setBackground(new Color(30, 30, 30));
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);

        } else {

            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            textArea.setCaretColor(Color.BLACK);
        }
    }

    // ============================================================
    // UNDO FEATURE
    // ============================================================
    private void performUndo() {

        if (undoStack.size() > 1) {

            // Save current state into redo stack
            redoStack.push(undoStack.pop());

            // Get previous state
            String previousState = undoStack.peek();

            textArea.setText(previousState);
        }
    }

    // ============================================================
    // REDO FEATURE
    // ============================================================
    private void performRedo() {

        if (!redoStack.isEmpty()) {

            String redoState = redoStack.pop();

            undoStack.push(redoState);

            textArea.setText(redoState);
        }
    }

    // ============================================================
    // AUTO SAVE FEATURE
    // ============================================================
    private void initializeAutoSave() {

        // Auto Save Every 30 Seconds
        autoSaveTimer = new Timer(30000, e -> {

            if (currentFile != null && isModified) {

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {

                    writer.write(textArea.getText());

                    isModified = false;

                    System.out.println("Auto Saved Successfully...");

                } catch (IOException ex) {

                    System.out.println("Auto Save Failed...");
                }
            }
        });

        autoSaveTimer.start();
    }

    // ============================================================
    // ABOUT DIALOG
    // ============================================================
    private void showAboutDialog() {

        JOptionPane.showMessageDialog(this,
                "Nexus Text Editor\n\n"
                        + "Core Technologies Used:\n"
                        + "✔ OOP\n"
                        + "✔ Professional Swing UI\n"
                        + "✔ Advanced Event Handling\n"
                        + "✔ Secure File Management\n"
                        + "✔ Intelligent State Management\n\n"
                        + "Professional Desktop Application",

                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ============================================================
    // CONFIRM SAVE BEFORE ACTION
    // ============================================================
    private boolean confirmSaveBeforeAction() {

        if (!isModified) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to save changes?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            saveFile();
            return true;
        }

        return choice == JOptionPane.NO_OPTION;
    }

    // ============================================================
    // WINDOW CLOSING EVENT
    // ============================================================
    private void initializeWindowListener() {

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    // ============================================================
    // EXIT APPLICATION
    // ============================================================
    private void exitApplication() {

        if (confirmSaveBeforeAction()) {
            dispose();
            System.exit(0);
        }
    }

    // ============================================================
    // MAIN METHOD
    // ============================================================
    public static void main(String[] args) {

        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new NexusTextEditor());
    }
}
