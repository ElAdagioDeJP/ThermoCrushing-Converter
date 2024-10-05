package com.mycompany.covertidor2;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ConvertidorUnidades {

    private static Categoria categoriaActual;
    private static JComboBox<String> categoriaBox, unidad1Box, unidad2Box;
    private static JTextField input1, input2;
    private static boolean isUpdating = false;  // Flag para evitar ciclo infinito de eventos
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.#####E0");  // Notación científica
    private static final DecimalFormat regularFormat = new DecimalFormat("0.#####");  // Formato normal

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Convertidor de Unidades");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridBagLayout()); // Usamos GridBagLayout para una interfaz mejor organizada
            frame.setSize(400, 300);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10); // Margen entre componentes

            // Selección de Categoría (Temperatura o Presión)
            String[] categorias = {"Temperatura", "Presión"};
            categoriaBox = new JComboBox<>(categorias);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            frame.add(categoriaBox, gbc);

            // Panel para el primer conjunto de input y unidad
            JPanel panel1 = new JPanel(new GridBagLayout());
            GridBagConstraints gbcPanel1 = new GridBagConstraints();
            gbcPanel1.insets = new Insets(0, 0, 0, 0); // Sin espacio entre componentes
            gbcPanel1.gridx = 0; gbcPanel1.gridy = 0;
            input1 = new JTextField(10);
            panel1.add(input1, gbcPanel1);
            gbcPanel1.gridy = 1;
            unidad1Box = new JComboBox<>();
            panel1.add(unidad1Box, gbcPanel1);

            // Panel para el segundo conjunto de input y unidad
            JPanel panel2 = new JPanel(new GridBagLayout());
            GridBagConstraints gbcPanel2 = new GridBagConstraints();
            gbcPanel2.insets = new Insets(0, 0, 0, 0); // Sin espacio entre componentes
            gbcPanel2.gridx = 0; gbcPanel2.gridy = 0;
            input2 = new JTextField(10);
            panel2.add(input2, gbcPanel2);
            gbcPanel2.gridy = 1;
            unidad2Box = new JComboBox<>();
            panel2.add(unidad2Box, gbcPanel2);

            // Añadimos los paneles al frame en una disposición horizontal
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            frame.add(panel1, gbc);

            // Añadimos el símbolo "=" entre los JTextField
            JLabel equalsLabel = new JLabel("=");
            equalsLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Aumentar el tamaño de la fuente
            gbc.gridx = 1; gbc.gridy = 1; gbc.gridheight = 1;
            frame.add(equalsLabel, gbc);

            gbc.gridx = 2;
            frame.add(panel2, gbc);

            // Actualización sin if ni for
            DocumentListener convertir = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { actualizarConversión(input1, input2, unidad1Box, unidad2Box); }
                public void removeUpdate(DocumentEvent e) { actualizarConversión(input1, input2, unidad1Box, unidad2Box); }
                public void insertUpdate(DocumentEvent e) { actualizarConversión(input1, input2, unidad1Box, unidad2Box); }
            };
            
            DocumentListener invertirConversión = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { actualizarConversión(input2, input1, unidad2Box, unidad1Box); }
                public void removeUpdate(DocumentEvent e) { actualizarConversión(input2, input1, unidad2Box, unidad1Box); }
                public void insertUpdate(DocumentEvent e) { actualizarConversión(input2, input1, unidad2Box, unidad1Box); }
            };

            input1.getDocument().addDocumentListener(convertir);
            input2.getDocument().addDocumentListener(invertirConversión);

            // Listener para cambios en las unidades seleccionadas
            ItemListener unidadCambioListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    Map<Integer, Runnable> acciones = new HashMap<>();
                    acciones.put(ItemEvent.SELECTED, () -> {
                        asegurarUnidadesDiferentes(e.getSource());
                        actualizarConversión(input1, input2, unidad1Box, unidad2Box);
                    });
                
                    Optional.ofNullable(acciones.get(e.getStateChange())).ifPresent(Runnable::run);
                }
            };
            unidad1Box.addItemListener(unidadCambioListener);
            unidad2Box.addItemListener(unidadCambioListener);

            // Cambiar categoría y actualizar las unidades
            Map<String, Runnable> categoriasMap = new HashMap<>();
            categoriasMap.put("Temperatura", () -> actualizarCategoria(new Temperatura()));
            categoriasMap.put("Presión", () -> actualizarCategoria(new Presion()));

            categoriaBox.addActionListener(new CategoriaCambioListener(categoriasMap, categoriaBox));

            // Inicializa con la categoría seleccionada
            actualizarCategoria(new Temperatura());

            frame.setVisible(true);
        });
    }

    public static void actualizarConversión(JTextField inputOrigen, JTextField inputDestino, JComboBox<String> unidadOrigenBox, JComboBox<String> unidadDestinoBox) {
        Runnable actualizar = () -> {
            try {
                String textoOrigen = inputOrigen.getText();
                Optional.of(textoOrigen)
                        .filter(texto -> !texto.isEmpty())
                        .map(Double::parseDouble)
                        .ifPresentOrElse(valorOrigen -> {
                            String unidadOrigen = (String) unidadOrigenBox.getSelectedItem();
                            String unidadDestino = (String) unidadDestinoBox.getSelectedItem();
                            Function<Double, Double> conversion = Optional.ofNullable(categoriaActual.obtenerConversion(unidadOrigen, unidadDestino))
                                    .orElseThrow(() -> new IllegalArgumentException("Conversión no soportada: " + unidadOrigen + " a " + unidadDestino));
                            double valorConvertido = conversion.apply(valorOrigen);
                            String valorFormateado = Optional.of(valorConvertido)
                                    .filter(valor -> valor >= 1e6 || valor < 1e-6)
                                    .map(decimalFormat::format)
                                    .orElseGet(() -> regularFormat.format(valorConvertido));
                            inputDestino.setText(valorFormateado);
                        }, () -> inputDestino.setText(""));
            } catch (NullPointerException | IllegalArgumentException e) {
                
            } finally {
                isUpdating = false;  // Libera el bloqueo
            }
        };

        Optional.of(isUpdating)
                .filter(updating -> !updating)
                .ifPresent(updating -> {
                    isUpdating = true;  // Bloquea actualizaciones para evitar ciclos infinitos
                    actualizar.run();
                });
    }

    // Actualiza la categoría actual y las unidades en los JComboBox usando un enfoque sin `if` ni `for`
    private static void actualizarCategoria(Categoria nuevaCategoria) {
        categoriaActual = nuevaCategoria;
        String[] unidades = categoriaActual.getUnidades();
        unidad1Box.removeAllItems();
        unidad2Box.removeAllItems();
        Arrays.stream(unidades).forEach(unidad -> {
            unidad1Box.addItem(unidad);
            unidad2Box.addItem(unidad);
        });

        // Asegurar que las unidades sean diferentes
        asegurarUnidadesDiferentes(unidad1Box);
    }

    private static void asegurarUnidadesDiferentes(Object source) {
        Map<Object, Runnable> acciones = new HashMap<>();
        acciones.put(unidad1Box, () -> {
            String unidad1 = (String) unidad1Box.getSelectedItem();
            String unidad2 = (String) unidad2Box.getSelectedItem();
            unidad1Box.setSelectedIndex(
                unidad1 != null && unidad1.equals(unidad2) && source == unidad1Box
                    ? (unidad1Box.getSelectedIndex() + 1) % unidad1Box.getItemCount()
                    : unidad1Box.getSelectedIndex()
            );
        });
        acciones.put(unidad2Box, () -> {
            String unidad1 = (String) unidad1Box.getSelectedItem();
            String unidad2 = (String) unidad2Box.getSelectedItem();
            unidad2Box.setSelectedIndex(
                unidad2 != null && unidad2.equals(unidad1) && source == unidad2Box
                    ? (unidad2Box.getSelectedIndex() + 1) % unidad2Box.getItemCount()
                    : unidad2Box.getSelectedIndex()
            );
        });

        Optional.ofNullable(acciones.get(source)).ifPresent(Runnable::run);
    }
}
//probando