//Manejar nulls en el filtrado (enviar los nulls al final)
//Optimizacion: guardar la busqueda del key adapter y llamar el resultado al presionar ENTER (evitar doble busqueda)

package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.table.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import taskmanager.AbstractTaskPopup.EditTaskPopup;
import taskmanager.AbstractTaskPopup.NewTaskPopup;
import taskmanager.Task.priorityRange;

public class TaskManagerGUI extends JFrame {
    private ArrayList<Task> displayTasks = new ArrayList<>();
    private JPanel mainPanel;   
    private JTable taskTable;
    private TaskTableModel taskTableModel;
    private JComboBox<Project> projectComboBox;
    private JButton btnTodo;
    private JRadioButton chkPendientes;  
    private JRadioButton chkCompletadas;
    private int selectedRow = -1;
    
    
    
    public TaskManagerGUI() {
        this.displayTasks = new ArrayList<>(App.allTasks); //obtiene por defecto un nuevo array (instancia) con referecnaia a app.alltasks
        initializeFrame();
        
        JPanel leftPanel = createLeftPanel();
        JPanel topPanel = createTopPanel();
        JPanel bottomRightPanel = createBottomRightPanel();
        createMainPanel();

        add(leftPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomRightPanel, BorderLayout.SOUTH);
        
        addContextMenuToTable();
        
    } //Inicializa todos los paneles de la GUI

    //FLAGS
    
    
    
    
    private void initializeFrame() {
        setTitle("Task Manager");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Llamar al método shutdown antes de cerrar
                App.shutdown();
                // Luego cerrar el JFrame
                dispose();
                System.exit(0); // Terminar la aplicación
            }
        });
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 20));

        addViewSection(leftPanel);
        addFilterSection(leftPanel);

        return leftPanel;
    }

    
    //Aplicar filtros pendiente o completada a ambas vistas.
    private void addViewSection(JPanel panel) {
        // Create and align the label
        JLabel lblVista = new JLabel("VISTA");
        lblVista.setForeground(Color.WHITE);
        lblVista.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        panel.add(lblVista);
        panel.add(Box.createVerticalStrut(10));

        // Create and align the button
        btnTodo = new JButton("Todo"); // Change to JButton
        btnTodo.setToolTipText("Mostrar todas las vistas");
        btnTodo.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        panel.add(btnTodo);
        panel.add(Box.createVerticalStrut(10));

        // Create and align the project label
        JLabel lblProyecto = new JLabel("Proyecto");
        lblProyecto.setForeground(Color.WHITE);
        lblProyecto.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        panel.add(lblProyecto);
        panel.add(Box.createVerticalStrut(10));

        // Create and align the combo box
        projectComboBox = new JComboBox<>();
        projectComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // Cambia tamaño de la lista del combo box
                Font font = new Font("Arial", Font.PLAIN, 12);
                setFont(font);

                if (value == null) { //Si el valor del item es null (index 0) el texto sera:
                    setText("--Selecciona Proyecto--");
                } else if (value instanceof Project) { //Si el item es instancia de project, el texto es el nombre del proyecto
                    setText(((Project) value).getNombreProyecto());
                }
                return this;
            }
        });

        projectComboBox.setPreferredSize(new Dimension(150, 25));
        projectComboBox.setMaximumSize(new Dimension(150, 25));
        projectComboBox.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left

        // Cambiar la fuente del JComboBox cuando no tiene foco
        Font comboBoxFont = new Font("Arial", Font.PLAIN, 11); // Cambia "Arial" y el tamaño según tus necesidades
        projectComboBox.setFont(comboBoxFont);

        projectComboBox.addActionListener(e -> { //Action listener al seleccionar un proyecto
            Project selectedProject = (Project) projectComboBox.getSelectedItem();
            if (selectedProject != null) {
                // Logic when a project is selected
                selectedProject.updateProjectTasks(); //Actualiza las tasks del project (puede no ser muy optimo para el rendimiento)
                updateTableData(new ArrayList<>(selectedProject.getTasks())); //Actualiza con un array nuevo con referencias a las tasks del project
            } else {
                // Logic when no project is selected
                updateTableData(new ArrayList<>(App.allTasks)); // Mostrar todas las tareas
            }
        });

        panel.add(projectComboBox);
        panel.add(Box.createVerticalStrut(20));

        // Action listener for the button
        btnTodo.addActionListener(e -> {
            projectComboBox.setSelectedIndex(-1); // Deseleccionar el combo box
            updateTableData(new ArrayList<>(App.allTasks)); // Mostrar todas las tareas
        });

        btnTodo.setSelected(true); // Inicialmente seleccionado (no aplicable para JButton)
        updateProjectList();
    }
    
    public void updateProjectList() {
        projectComboBox.removeAllItems();
        projectComboBox.addItem(null);
        
        for (Project project : App.allProjects) {
            projectComboBox.addItem(project);
        }
    }


    private void addFilterSection(JPanel panel) {
        JLabel lblFiltros = new JLabel("FILTROS");
        lblFiltros.setForeground(Color.WHITE);
        panel.add(lblFiltros);
        panel.add(Box.createVerticalStrut(10));

        
        chkPendientes = createRadioButton("Pendientes", "filtrar por tareas pendientes");
        chkCompletadas = createRadioButton("Completadas", "filtrar por tareas completadas");

        
        //Action listeners a la seleccion del radio button, mostrando la vista correspondiente
        chkPendientes.addActionListener(e -> {
            if (chkPendientes.isSelected()) {
                chkCompletadas.setSelected(false);
                System.out.println("Filtro de tareas pendientes activado.");
                App.updatePendingTasks();
                updateTableData(App.pendingTasks);
            } else {
                System.out.println("Filtro de tareas pendientes desactivado.");
                updateTableData(App.allTasks);
            }
        });
        panel.add(chkPendientes);
        panel.add(Box.createVerticalStrut(10));

        chkCompletadas.addActionListener(e -> {
            if (chkCompletadas.isSelected()) {
                chkPendientes.setSelected(false);
                System.out.println("Filtro de tareas completadas activado.");
                App.updateCompletedTasks();
                updateTableData(App.completedTasks);
            } else {
                System.out.println("Filtro de tareas completadas desactivado.");
                updateTableData(App.allTasks);
            }
        });
        panel.add(chkCompletadas);
        panel.add(Box.createVerticalStrut(10));

        
    }
    
    

    private JRadioButton createRadioButton(String text, String tooltip) {
        JRadioButton button = new JRadioButton(text);
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setToolTipText(tooltip);
        return button;
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.LIGHT_GRAY);

        SearchBar searchBar = new SearchBar();
        topPanel.add(searchBar);
        JTextField searchField = searchBar.getSearchField();
        JComboBox suggestionBox = searchBar.getSuggestionBox();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateTableData(searchBar.getSearchResult());
                }
            }
        });
//        suggestionBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                updateTableData(searchBar.getSearchResult());
//            }
//        });
        return topPanel;
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        taskTableModel = new TaskTableModel(displayTasks); //Construye la tabla bajo el modelo custom con las displayTasks
        taskTable = new JTable(taskTableModel);
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new CustomTableCellRenderer()); //Custom Cell Renderer para la prioridad
        taskTable.setAutoCreateRowSorter(true); //filtrador ascendiente o descendiente
        
        JScrollPane scrollPane = new JScrollPane(taskTable);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void addContextMenuToTable() {
        JPopupMenu contextMenu = createContextMenu();
        taskTable.setComponentPopupMenu(contextMenu); //Crea menu de contexto, Añade mouse listener al click secundario por defecto

        //listener al click (izq o der) que selecciona una fila 
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int currentRow = taskTable.rowAtPoint(point);
                taskTable.setRowSelectionInterval(currentRow, currentRow);
            }
        });
    }
    
    private JPopupMenu createContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem editItem = new JMenuItem("Edit");

        deleteItem.addActionListener(e -> deleteSelectedTask());
        editItem.addActionListener(e -> editSelectedTask());

        contextMenu.add(deleteItem);
        contextMenu.add(editItem);

        return contextMenu;
    }
    
    private void deleteSelectedTask() {
        selectedRow = taskTable.getSelectedRow();  //devuelve el indice correspondiete a la fila seleccionada segun la vista
        if (selectedRow >= 0) {
            Integer taskToDeleteId = taskTableModel.getTaskIdAt(selectedRow); //obtiene el id de la tarea seleccionada
            Task taskToDelete = taskTableModel.getTaskById(taskToDeleteId); //obtiene la instancia de la tarea en base a su id

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this task?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) { //si confirma el dialog de arriba elimina
                // Remove from all lists
                App.allTasks.remove(taskToDelete);
                App.pendingTasks.remove(taskToDelete);
                App.completedTasks.remove(taskToDelete);

                // Remove from project
                for (Project project : App.allProjects) {
                    project.getTasks().remove(taskToDelete);
                }
                
                //Remove from DB 
                DB.deleteRecord(taskToDeleteId);
                
                App.setflagAllTasksMutationON();

                // Update the table
                updateTableData(getCurrentViewTasks());
            } //else no pasa nada, se cierra simplemente
        }
    }
    
    private void editSelectedTask() { //se ejecuta en el event listener del context menu luego de mouse adapter, por lo que siempre hay selectedRow
        selectedRow = taskTable.getSelectedRow(); // Índice de la vista
        if (selectedRow >= 0) {
            int taskId = taskTableModel.getTaskIdAt(selectedRow);  // Obtener el ID de la tarea en la fila seleccionada
            Task selectedTask = taskTableModel.getTaskById(taskId); //Obtener tarea por su id

            if (selectedTask != null) {
                // Mostrar el cuadro de diálogo de edición de tarea
                EditTaskPopup editPopup = new EditTaskPopup(this, selectedTask);
                editPopup.setVisible(true);  // Mostrar el popup para edición

                // Después de cerrar el popup, actualizar los datos de la tabla con la vista actual
                updateTableData(getCurrentViewTasks());  
            }
        }
    }


     private ArrayList<Task> getCurrentViewTasks() { //Cambiar a una version mas manejable y optima
        if (projectComboBox.getSelectedItem() != null) {
            return ((Project) projectComboBox.getSelectedItem()).getTasks();
        } else if (btnTodo.isSelected()) {
            return new ArrayList<>(App.allTasks);
        } else if (chkPendientes.isSelected()) {
            return new ArrayList<>(App.pendingTasks);
        } else if (chkCompletadas.isSelected()) {
            return new ArrayList<>(App.completedTasks);
        }
        return new ArrayList<>(App.allTasks);  // Default to all tasks
    }
     
    private void updateTableData(ArrayList<Task> updatedTasks) {
        displayTasks.clear(); //limpia la instancia del atributo
        displayTasks.addAll(updatedTasks); // Actualiza displayTasks con las tareas actuales
        taskTableModel.fireTableDataChanged(); // Notifica al modelo de tabla para actualizar la vista
    }
    
    private JPanel createBottomRightPanel() {
        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.setBackground(Color.LIGHT_GRAY);

        JButton btnNewProject = createIconButton("resources/newProject.png", "Crear un nuevo proyecto");
        JButton btnNewTask = createIconButton("resources/newTask.png", "Añadir una nueva tarea");
        
        btnNewTask.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNewTaskPopup();  // Open the popup to add a new task
            }
        });
        
        btnNewProject.addActionListener(e -> {
            NewProjectPopup popup = new NewProjectPopup(TaskManagerGUI.this);
            popup.setVisible(true);
            Project newProject = popup.getNewProject();
            if (newProject != null) {
                updateProjectList();
            }
        });

        bottomRightPanel.add(btnNewProject);
        bottomRightPanel.add(btnNewTask);
        
        return bottomRightPanel;
    }
    
    private void showNewTaskPopup() {
        NewTaskPopup newTaskPopup = new NewTaskPopup(this);
        newTaskPopup.setVisible(true);
        // Refrezcar la vista actual de la tabla, (puede no mostrar la nueva tarea)
        updateTableData(getCurrentViewTasks());
    }

    private JButton createIconButton(String iconPath, String tooltip) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL imageURL = cl.getResource(iconPath);
        ImageIcon icon = new ImageIcon(imageURL);

        JButton button = new JButton(icon);
        button.setPreferredSize(new Dimension(34, 34));
        button.setToolTipText(tooltip);

        return button;
    }
    
    //TABLE FORMATTING 
    public class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (column == 3 && value instanceof priorityRange) {
                priorityRange priority = (priorityRange) value;
                switch (priority) {
                    case LOW:
                        cellComponent.setBackground(new Color(200, 255, 200)); // Light green
                        cellComponent.setForeground(new Color(0, 100, 0)); // Dark green
                        break;
                    case MEDIUM:
                        cellComponent.setBackground(new Color(255, 255, 200)); // Light yellow
                        cellComponent.setForeground(new Color(184, 134, 11)); // Dark goldenrod
                        break;
                    case HIGH:
                        cellComponent.setBackground(new Color(255, 200, 200)); // Light red
                        cellComponent.setForeground(new Color(139, 0, 0)); // Dark red
                        break;
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                setText(priority.name());
            } else {
                cellComponent.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                cellComponent.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }
            
            return cellComponent;
        }
    }
    
    
    //CUSTOM TABLE CLASS
    public class TaskTableModel extends AbstractTableModel {
        private ArrayList<Task> displayTasks;
        private final String[] columnNames = {"Estado", "Nombre Proyecto", "Descripción", "Prioridad", "Fecha Límite"};
        
        public TaskTableModel(ArrayList<Task> displayTasks) {
            this.displayTasks = displayTasks;
        }
        
        public int getTaskIdAt(int rowIndex) {
            return displayTasks.get(rowIndex).getTaskId(); // Devuelve el ID de la tarea en la fila especificada
        }

        // Método que devuelve la tarea en base a su ID
        public Task getTaskById(int taskId) {
            for (Task task : displayTasks) { //busca en displayTasks (instancia unica recordar)
                if (task.getTaskId() == taskId) {
                    return task;
                }
            }
            return null; // Devuelve null si no se encuentra la tarea
        }
        
        
        @Override 
        public int getRowCount() {
            return displayTasks.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Task task = displayTasks.get(rowIndex);
            switch (columnIndex) {
                case 0: return task.isCompletada();
                case 1: return task.getProjectName();
                case 2: return task.getDescripcion();
                case 3: return task.getPrioridad();
                case 4: return task.getFechaLimite();
                default: return null;
            }
        }
        
        @Override
        public String getColumnName(int column) { 
            return columnNames[column];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Boolean.class;
                case 3: return priorityRange.class;
                case 4: return LocalDate.class;
                default: return String.class;
            }
        }
        
        @Override 
        public boolean isCellEditable(int rowIndex, int columnIndex) { //solo la celda de la checkbox es editable
            return columnIndex == 0;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) { //Para setear completada
            if (columnIndex == 0) {
                Task thisTask = displayTasks.get(rowIndex);
                thisTask.setCompletada((Boolean) aValue);
                if ((Boolean) aValue) {
                    System.out.println("Task Completada! : " + thisTask.toString());
                }
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
    
}

