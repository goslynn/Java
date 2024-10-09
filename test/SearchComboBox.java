import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class SearchComboBox extends JFrame {

    // Mapa de datos (clave: ID, valor: país)
    private static final Map<Integer, String> DATA_MAP = new HashMap<>();

    static {
        DATA_MAP.put(1, "Argentina");
        DATA_MAP.put(2, "Australia");
        DATA_MAP.put(3, "Austria");
        DATA_MAP.put(4, "Brazil");
        DATA_MAP.put(5, "Belgium");
        DATA_MAP.put(6, "Canada");
        DATA_MAP.put(7, "China");
        DATA_MAP.put(8, "Denmark");
        DATA_MAP.put(9, "Finland");
        DATA_MAP.put(10, "France");
        DATA_MAP.put(11, "Germany");
        DATA_MAP.put(12, "Greece");
        DATA_MAP.put(13, "Hungary");
        DATA_MAP.put(14, "India");
        DATA_MAP.put(15, "Indonesia");
        DATA_MAP.put(16, "Italy");
        DATA_MAP.put(17, "Japan");
        DATA_MAP.put(18, "Mexico");
        DATA_MAP.put(19, "Netherlands");
        DATA_MAP.put(20, "Norway");
        DATA_MAP.put(21, "Portugal");
        DATA_MAP.put(22, "Russia");
        DATA_MAP.put(23, "Spain");
        DATA_MAP.put(24, "Sweden");
        DATA_MAP.put(25, "Switzerland");
        DATA_MAP.put(26, "United Kingdom");
        DATA_MAP.put(27, "United States");
        DATA_MAP.put(28, "Vietnam");
    }

    private JComboBox<String> comboBox;
    private DefaultComboBoxModel<String> model;
    private JTextField textField;

    public SearchComboBox() {
        // Configuración de la ventana
        setTitle("Search ComboBox with Map");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana

        // Crear DefaultComboBoxModel y JComboBox
        model = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>(model);
        comboBox.setEditable(true);
        textField = (JTextField) comboBox.getEditor().getEditorComponent();

        // Añadir KeyListener para filtrar sugerencias al escribir
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String input = textField.getText();
                updateSuggestions(input); // Actualizar sugerencias en base al input
            }
        });
        
        

        // Configuración del layout y posición del JComboBox
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(comboBox, gbc);

        // Hacer la ventana visible
        setVisible(true);
    }

    // Método que filtra las sugerencias basadas en la entrada del usuario
    private void updateSuggestions(String input) {
        model.removeAllElements(); // Limpiar las sugerencias anteriores

        // Filtrar los valores del mapa que coincidan con la entrada del usuario
        List<String> matches = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : DATA_MAP.entrySet()) {
            if (entry.getValue().toLowerCase().startsWith(input.toLowerCase())) {
                matches.add(entry.getValue());
            }
        }

        // Añadir coincidencias al modelo si existen
        if (!matches.isEmpty()) {
            for (String match : matches) {
                model.addElement(match); // Agregar coincidencias al combo box
            }
            comboBox.showPopup(); // Mostrar el dropdown si hay coincidencias
        } else {
            comboBox.hidePopup(); // Ocultar el dropdown si no hay coincidencias
        }
        
        // Mantener el texto del usuario en el textField sin modificarlo
        textField.setText(input);
        textField.setCaretPosition(input.length()); // Colocar el cursor al final
    }

    // Método para obtener el ID del país seleccionado
    public Integer getSelectedCountryId() {
        String selectedValue = (String) comboBox.getSelectedItem();
        if (selectedValue != null) {
            for (Map.Entry<Integer, String> entry : DATA_MAP.entrySet()) {
                if (entry.getValue().equals(selectedValue)) {
                    return entry.getKey(); // Retornar el ID del país seleccionado
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        // Ejecutar la aplicación
        SwingUtilities.invokeLater(() -> {
            SearchComboBox searchComboBox = new SearchComboBox();
            // Prueba para obtener el ID del país seleccionado
            JButton button = new JButton("Obtener ID");
            button.addActionListener(e -> {
                Integer selectedId = searchComboBox.getSelectedCountryId();
                System.out.println("ID seleccionado: " + selectedId);
            });
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 0;  // Cambia esto dependiendo de dónde quieras ubicar el botón
            gbc.insets = new Insets(10, 10, 10, 10);  // Espaciado
            searchComboBox.add(button, gbc);

        });
    }
}
