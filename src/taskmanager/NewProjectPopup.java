package taskmanager;

import javax.swing.*;
import java.awt.*;


public class NewProjectPopup extends JDialog {
    private JTextField nameField;
    private JButton confirmButton;
    private JButton cancelButton;
    private Project newProject;
    private Integer idCounter = 0;

    public NewProjectPopup(JFrame parent) {
        super(parent, "New Project", true);
        initComponents();
        layoutComponents();
        addListeners();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        nameField = new JTextField(20);
        confirmButton = new JButton("Confirmar");
        cancelButton = new JButton("Cancelar");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        formPanel.add(new JLabel("Nombre del proyecto:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        formPanel.add(nameField, gbc);


        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        confirmButton.addActionListener(e -> createProject());
        cancelButton.addActionListener(e -> dispose());
    }

    private void createProject() {
        String projectName = nameField.getText().trim();
        if (projectName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Se requiere un nombre de proyecto", "Input no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        newProject = Project.createInstance(projectName);
        idCounter -= 1;
        newProject.setIdProyecto(idCounter);
        dispose();
    }

    public Project getNewProject() {
        return newProject;
    }
}