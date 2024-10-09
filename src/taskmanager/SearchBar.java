//El realizar una busqueda haciendo click en una de las sugerencias no funciona
package taskmanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class SearchBar extends JPanel {
    private DefaultComboBoxModel<String> suggestionBoxModel;
    private JComboBox<String> suggestionBox;
    private JTextField searchField;
    private ArrayList<String> taskList = new ArrayList<>();  // Lista de tareas
    private boolean navigating = false;
    private ArrayList<Task> searchResult = new ArrayList<>();

    public SearchBar() {
        updateTaskList();  // Inicializar la lista de tareas
        initializeSearchBar();  // Inicializar los componentes de la barra de búsqueda
        applyStyles();
        this.setPreferredSize(new Dimension(300, 20));
    }

    // Inicializa la lista de tareas usando App.allTasks
    private void updateTaskList() {
        taskList.clear();
        for (Task task : App.allTasks) {
            String description = task.getDescripcion();
            // Solo agregar si la descripción no está ya en la lista
            if (!taskList.contains(description)) {
                taskList.add(description);
            }
        }
    }

    // Inicializa los componentes de la barra de búsqueda
    private void initializeSearchBar() {
        suggestionBoxModel = new DefaultComboBoxModel<>();  // Restaurar el DefaultComboBoxModel
        suggestionBox = new JComboBox<>(suggestionBoxModel);
        suggestionBox.setUI(new NoArrowComboBoxUI());
        suggestionBox.setEditable(true);  // Hacer editable para permitir escritura sin autocompletar
        searchField = (JTextField) suggestionBox.getEditor().getEditorComponent();

        // Agregar KeyListener al campo de búsqueda
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Actualizar sugerencias cada vez que se presiona una tecla
                String query = searchField.getText();
                if (e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
                    updateSuggestions(query);
                    System.out.println("updating suggestions");
                }

                // Detectar cuando se presiona Enter
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Realizar la búsqueda con el valor actual del campo de texto
                    searchResult.clear();
                    searchResult = performSearch(query);
                    System.out.println("Busqueda realizada");
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Si se navega con las flechas, no se realiza búsqueda, solo se muestra la sugerencia
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    navigating = true;  // Estamos navegando con las flechas
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Quitar el foco del campo de texto
                    searchField.transferFocus();  // O puedes usar searchField.setFocusable(false);
                    System.out.println("Foco removido del campo de búsqueda");
                }
            }
        });

// -- No detecta el MouseEvent --
        // MouseAdapter para manejar clic en los elementos del JComboBox (dropdown)
//        suggestionBox.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                // Verificar si el clic fue con el botón izquierdo
//                if (e.getButton() == MouseEvent.BUTTON1 && !navigating) {
//                    // Si se hace clic en el JComboBox, realizar la búsqueda
//                    String selectedItem = (String) suggestionBox.getSelectedItem();
//                    searchField.setText(selectedItem);  // Establecer el valor seleccionado en el textfield
//                    searchResult.clear();
//                    searchResult = performSearch(selectedItem);
//                    System.out.println("Busqueda Realizada papu");
//                }
//            }
//        });

        setLayout(new BorderLayout());  // Usar BorderLayout para posicionar mejor el combo box
        add(suggestionBox, BorderLayout.CENTER);  // Añadir el combo box al centro del panel
    }

    private void applyStyles() {
        // Opcional: puedes personalizar colores o fuentes aquí
        suggestionBox.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    // Método que filtra las sugerencias basadas en la entrada del usuario
    private void updateSuggestions(String query) {
        if (App.flagAllTasksMutation) { //Cada vez que se escribe se checkea la flag, si esta activa significa que se ha mutado el listado asi que este se actualiza y se desactiva la flag
            updateTaskList();
            App.setflagAllTasksMutationOFF();
        }

        suggestionBoxModel.removeAllElements();  // Limpiar sugerencias anteriores

        if (query.isEmpty()) {
            suggestionBox.hidePopup();  // Ocultar el dropdown si no hay texto
            return;
        }

        // Filtrar las descripciones de las tareas que coincidan con la búsqueda
        ArrayList<String> matches = getDescriptionMatchesList(query);

        if (!matches.isEmpty()) {
            for (String match : matches) {
                suggestionBoxModel.addElement(match);  // Agregar coincidencias
            }
            suggestionBox.showPopup();  // Mostrar el dropdown si hay coincidencias
        } else {
            suggestionBox.hidePopup();  // Ocultar el dropdown si no hay coincidencias
        }

        // Evitar Autocompletado
        searchField.setText(query);
        searchField.setCaretPosition(query.length());
    }

    private ArrayList<String> getDescriptionMatchesList(String query) { // Filtra y encuentra coincidencias
        final String normalizedQuery = StringUtils.normalizeString(query);
        return taskList.stream()
                .filter(task -> StringUtils.normalizeString(task).contains(normalizedQuery))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Task> performSearch(String query) { // Filtra y encuentra coincidencias
        final String normalizedQuery = StringUtils.normalizeString(query);
        return App.allTasks.stream()
                .filter(task -> StringUtils.normalizeString(task.getDescripcion()).contains(normalizedQuery))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public JComboBox<String> getSuggestionBox() {
        return suggestionBox;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public ArrayList<Task> getSearchResult() {
        return searchResult;
    }

    public class NoArrowComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            return new JButton() {
                @Override
                public int getWidth() {
                    return 0;  // Hacemos que el botón de flecha tenga un ancho de 0 para que no se vea
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, 0);  // Evitamos que el botón consuma espacio
                }
            };
        }
    }
}
