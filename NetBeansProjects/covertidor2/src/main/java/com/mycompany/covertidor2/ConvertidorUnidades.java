package com.mycompany.covertidor2;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
            frame.setSize(400, 200);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10); // Margen entre componentes

            // Selección de Categoría (Temperatura o Presión)
            String[] categorias = {"Temperatura", "Presión"};
            categoriaBox = new JComboBox<>(categorias);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            frame.add(categoriaBox, gbc);

            // Input y Unidades
            input1 = new JTextField(10);
            input2 = new JTextField(10);
            unidad1Box = new JComboBox<>();
            unidad2Box = new JComboBox<>();

            // Añadimos los componentes con su respectiva ubicación
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            frame.add(input1, gbc);
            gbc.gridx = 1;
            frame.add(unidad1Box, gbc);

            // Botón de intercambio
            JButton swapButton = new JButton("↔");
            gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 1;
            frame.add(swapButton, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
            frame.add(input2, gbc);
            gbc.gridx = 1;
            frame.add(unidad2Box, gbc);

            // Acción del botón de intercambio
            swapButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Intercambiar unidades seleccionadas
                    int unidad1Index = unidad1Box.getSelectedIndex();
                    unidad1Box.setSelectedIndex(unidad2Box.getSelectedIndex());
                    unidad2Box.setSelectedIndex(unidad1Index);

                    // Intercambiar valores de entrada
                    String temp = input1.getText();
                    input1.setText(input2.getText());
                    input2.setText(temp);

                    // Actualizar la conversión
                    actualizarConversión(input1, input2, unidad1Box, unidad2Box);
                }
            });

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
            ItemListener unidadCambioListener = new UnidadCambioListener(input1, input2, unidad1Box, unidad2Box);
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

    private static void asegurarUnidadesDiferentes() {
        Optional.ofNullable(unidad1Box.getSelectedItem())
            .filter(unidad -> unidad.equals(unidad2Box.getSelectedItem()))
            .ifPresent(unidad -> unidad2Box.setSelectedIndex((unidad2Box.getSelectedIndex() + 1) % unidad2Box.getItemCount()));
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

        asegurarUnidadesDiferentes();
    }
}