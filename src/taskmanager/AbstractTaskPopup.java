package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import taskmanager.Task.priorityRange;

public abstract class AbstractTaskPopup extends JDialog {
    protected JTextField descriptionField;
    protected JComboBox<Project> projectComboBox;
    protected JComboBox<priorityRange> priorityComboBox;
    protected JDateChooser dateChooser;
    protected JButton confirmButton;
    protected JButton cancelButton;
    protected Task task;
    protected static Integer idCounter = 0;

    public AbstractTaskPopup(JFrame parent, String title) {
        super(parent, title, true);
        initComponents();
        layoutComponents();
        addListeners();
        pack();
        setLocationRelativeTo(parent);
    }

    protected void initComponents() {
        descriptionField = new JTextField(20);
        
        DefaultComboBoxModel<Project> projectModel = new DefaultComboBoxModel<>();
        projectModel.addElement(null);
        for (Project project : App.allProjects) {
            projectModel.addElement(project);
        }
        
        projectComboBox = new JComboBox<>(projectModel);
        projectComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Project) {
                    setText(((Project) value).getNombreProyecto());
                } else if (value == null) {
                    setText("--Sin Proyecto--");
                }
                return this;
            }
        });
        
        
        priorityComboBox = new JComboBox<>(priorityRange.values());
        priorityComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Verificar si el valor es null y mostrar un texto personalizado
                if (value == null) {
                    setText("--Sin Prioridad--");
                } else {
                    setText(value.toString()); // Mostrar el texto por defecto para las prioridades no nulas
                }
                return this;
            }
        });

        // Añadir "null" como el primer elemento en el JComboBox para representar "Sin Prioridad"
        priorityComboBox.insertItemAt(null, 0); // Añadir null como primera opción
        priorityComboBox.setSelectedIndex(0); // Seleccionar la primera opción por defecto

        
        dateChooser = new JDateChooser();
        confirmButton = new JButton("Confirmar");
        cancelButton = new JButton("Cancelar");
    }

    protected void layoutComponents() { //define el layout de los componentes
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridy++;
        formPanel.add(new JLabel("Proyecto:"), gbc);
        gbc.gridy++;
        formPanel.add(new JLabel("Prioridad:"), gbc);
        gbc.gridy++;
        formPanel.add(new JLabel("Fecha Límite:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        formPanel.add(descriptionField, gbc);
        gbc.gridy++;
        formPanel.add(projectComboBox, gbc);
        gbc.gridy++;
        formPanel.add(priorityComboBox, gbc);
        gbc.gridy++;
        formPanel.add(dateChooser, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    protected void addListeners() { //Añade listeners a confirmar y cancelar
        confirmButton.addActionListener(e -> handleTask()); //al confirmar se llama al metodo handleTask
        cancelButton.addActionListener(e -> dispose());
    }

    protected abstract void handleTask();

    protected boolean validateInput() { //valida que se haya ingresado descripcion (unico campo obligatorio)
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) { //validar que exista descripcion
            JOptionPane.showMessageDialog(this, "Descripción es requerida", "Input no válido", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Date date = dateChooser.getDate(); 
        if (date != null){ //validar que la fecha no sea anterior a hoy
            LocalDate chosenDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (chosenDate.isBefore(LocalDate.now())){
                JOptionPane.showMessageDialog(this, "La fecha no puede ser previa a la de hoy", "Input no válido", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    protected Task getTaskFromInput() {
        String description = descriptionField.getText().trim();
        Project selectedProject = (Project) projectComboBox.getSelectedItem();
        priorityRange priority = (priorityRange) priorityComboBox.getSelectedItem();
        LocalDate dueDate = null; //por defecto null
        if (dateChooser.getDate() != null) { //si la seleccion no es null entonces se parsea dueDate
            dueDate = dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        Task task = Task.createInstance(description, dueDate, priority); //construir task
        idCounter -= 1;
        task.setTaskId(idCounter);
        if (selectedProject != null) { //ya que el constructor no requiere proyecto se lo aplicamos por setter, en caso de que lo tenga
            task.setProjectName(selectedProject.getNombreProyecto());
            task.setParentProjectId(selectedProject.getIdProyecto());
        }
        return task;
    }

    public Task getTask() {
        return task;
    }
    
    public static class NewTaskPopup extends AbstractTaskPopup {
        public NewTaskPopup(JFrame parent) {
            super(parent, "Nueva Tarea");
        }

        @Override
        protected void handleTask() {
            if (validateInput()) {
                task = getTaskFromInput();
                App.setflagAllTasksMutationON();
                dispose();
            }
        }
    }

    public static class EditTaskPopup extends AbstractTaskPopup {
        public EditTaskPopup(JFrame parent, Task taskToEdit) {
            super(parent, "Editar Tarea");
            this.task = taskToEdit;
            populateFields();
        }

        private void populateFields() {
            descriptionField.setText(task.getDescripcion());
            priorityComboBox.setSelectedItem(task.getPrioridad());
            if (task.getFechaLimite() != null) {
                dateChooser.setDate(java.sql.Date.valueOf(task.getFechaLimite()));
            }
            for (int i = 0; i < projectComboBox.getItemCount(); i++) {
                Project project = projectComboBox.getItemAt(i);
                if (project != null && project.getIdProyecto() == task.getParentProjectId()) {
                    projectComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

       @Override
        protected void handleTask() {
            if (validateInput()) {
                task.setDescripcion(descriptionField.getText());
                task.setPrioridad((priorityRange) priorityComboBox.getSelectedItem());
                task.setFechaLimite(dateChooser.getDate() != null ? new java.sql.Date(dateChooser.getDate().getTime()).toLocalDate() : null); //necesito entender esta linea

                Project selectedProject = (Project) projectComboBox.getSelectedItem();
                if (selectedProject != null) {
                    task.setParentProjectId(selectedProject.getIdProyecto());
                    task.setProjectName(selectedProject.getNombreProyecto());
                }
                else { //Si el combo box de project es null asignar estos valores a la task (desasociar proyecto)
                    task.setParentProjectId(null);
                    task.setProjectName(null);
                }

                dispose();  // Close the popup
            }
        }


    }
}
