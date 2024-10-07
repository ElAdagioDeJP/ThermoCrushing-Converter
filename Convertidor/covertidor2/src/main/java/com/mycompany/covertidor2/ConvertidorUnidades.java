
package com.mycompany.covertidor2;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ConvertidorUnidades {

    private static Categoria categoriaActual;
    private static JComboBox<String> categoriaBox, unidad1Box, unidad2Box;
    private static JTextField input1, input2;
    private static JLabel formulaLabel;
    private static boolean isUpdating = false;  // Flag para evitar ciclo infinito de eventos
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.####E0");  // Notación científica
    private static final DecimalFormat twoDecimalFormat = new DecimalFormat("0.##");  // Formato con dos decimales

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Convertidor de Unidades");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridBagLayout()); // Usamos GridBagLayout para una interfaz mejor organizada
            frame.setSize(500, 300);
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
            input1.setPreferredSize(new Dimension(13, 20));
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
            gbc.gridx = 1; gbc.gridy = 1; gbc.gridheight = 1;gbc.gridwidth = 1;
            frame.add(equalsLabel, gbc);

            gbc.gridx = 2;
            frame.add(panel2, gbc);

            // Añadir JLabel para mostrar la fórmula
            formulaLabel = new JLabel("Fórmula: ");
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
            formulaLabel.setPreferredSize(new Dimension(279, 50));
            frame.add(formulaLabel, gbc);

            // Actualización sin if ni for
            DocumentListener convertir = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { actualizarConversión(input1, input2, unidad1Box, unidad2Box, false); }
                public void removeUpdate(DocumentEvent e) { actualizarConversión(input1, input2, unidad1Box, unidad2Box, false); }
                public void insertUpdate(DocumentEvent e) { actualizarConversión(input1, input2, unidad1Box, unidad2Box, false); }
            };
            
            DocumentListener invertirConversión = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { actualizarConversión(input2, input1, unidad2Box, unidad1Box, true); }
                public void removeUpdate(DocumentEvent e) { actualizarConversión(input2, input1, unidad2Box, unidad1Box, true); }
                public void insertUpdate(DocumentEvent e) { actualizarConversión(input2, input1, unidad2Box, unidad1Box, true); }
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
                        actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
                        actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
                        actualizarFormula();
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

            // Agregar FocusListener para limpiar letras al perder el foco
            FocusListener limpiarLetrasListener = new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    // No hacer nada al ganar el foco
                }

                @Override
                public void focusLost(FocusEvent e) {
                    JTextField source = (JTextField) e.getSource();
                    Optional.of(source.getText())
                            .filter(texto -> texto.chars().anyMatch(Character::isLetter))
                            .ifPresent(texto -> SwingUtilities.invokeLater(() -> source.setText("")));
                }
            };

            input1.addFocusListener(limpiarLetrasListener);
            input2.addFocusListener(limpiarLetrasListener);

            frame.setVisible(true);
        });
    }

    public static void actualizarConversión(JTextField inputOrigen, JTextField inputDestino, JComboBox<String> unidadOrigenBox, JComboBox<String> unidadDestinoBox, boolean desdeSegundoInput) {
        Runnable actualizar = () -> {
            try {
                String textoOrigen = inputOrigen.getText();
                String unidadOrigen = (String) unidadOrigenBox.getSelectedItem();
                String unidadDestino = (String) unidadDestinoBox.getSelectedItem();
                Optional.of(textoOrigen)
                        .filter(texto -> !texto.isEmpty())
                        .map(Double::parseDouble)
                        .ifPresentOrElse(valorOrigen -> {
                            Function<Double, Double> conversion = Optional.ofNullable(categoriaActual.obtenerConversion(unidadOrigen, unidadDestino))
                                    .orElseThrow(() -> new IllegalArgumentException("Conversión no soportada: " + unidadOrigen + " a " + unidadDestino));
                            double valorConvertido = conversion.apply(valorOrigen);

                            Map<Boolean, String> formatos = new HashMap<>();
                            formatos.put(true, decimalFormat.format(valorConvertido));
                            formatos.put(false, twoDecimalFormat.format(valorConvertido));

                            String valorFormateado = Optional.of(textoOrigen.length() >= 21)
                                                             .map(formatos::get)
                                                             .orElse(twoDecimalFormat.format(valorConvertido));

                            inputDestino.setText(valorFormateado);
                            actualizarFormula(valorOrigen, valorConvertido, unidadOrigen, unidadDestino, desdeSegundoInput);
                        }, () -> {
                            inputDestino.setText("");
                            actualizarFormula(0, 0, unidadOrigen, unidadDestino, desdeSegundoInput);
                        });
            } catch (NullPointerException | IllegalArgumentException e) {
                // Manejo de excepciones
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
        actualizarFormula();
    }

    private static void asegurarUnidadesDiferentes(Object source) {
        Map<Object, Runnable> acciones = new HashMap<>();
        acciones.put(unidad1Box, () -> {
            String unidad1 = (String) unidad1Box.getSelectedItem();
            String unidad2 = (String) unidad2Box.getSelectedItem();
            Optional.ofNullable(unidad1)
                    .filter(u -> u.equals(unidad2) && source == unidad1Box)
                    .ifPresent(u -> unidad1Box.setSelectedIndex((unidad1Box.getSelectedIndex() + 1) % unidad1Box.getItemCount()));
        });
        acciones.put(unidad2Box, () -> {
            String unidad1 = (String) unidad1Box.getSelectedItem();
            String unidad2 = (String) unidad2Box.getSelectedItem();
            Optional.ofNullable(unidad2)
                    .filter(u -> u.equals(unidad1) && source == unidad2Box)
                    .ifPresent(u -> unidad2Box.setSelectedIndex((unidad2Box.getSelectedIndex() + 1) % unidad2Box.getItemCount()));
        });

        Optional.ofNullable(acciones.get(source)).ifPresent(Runnable::run);
    }

    private static void actualizarFormula() {
        String unidadOrigen = (String) unidad1Box.getSelectedItem();
        String unidadDestino = (String) unidad2Box.getSelectedItem();
        Optional.ofNullable(unidadOrigen)
                .flatMap(u1 -> Optional.ofNullable(unidadDestino)
                        .map(u2 -> u1 + "-" + u2))
                .ifPresent(key -> {
                    Map<String, String> formulas = new HashMap<>();
                    formulas.put("Celsius-Fahrenheit", "(%s °C × 9/5) + 32 = %s °F");
                    formulas.put("Fahrenheit-Celsius", "(%s °F − 32) × 5/9 = %s °C");
                    formulas.put("Kelvin-Celsius", "%s K − 273.15 = %s °C");
                    formulas.put("Celsius-Kelvin", "%s °C + 273.15 = %s K");
                    formulas.put("Kelvin-Fahrenheit", "(%s K − 273.15) × 9/5 + 32 = %s °F");
                    formulas.put("Fahrenheit-Kelvin", "(%s °F − 32) × 5/9 + 273.15 = %s K");

                    formulas.put("Atmósfera-Bar", "multiplica el valor de presión por 1,013");
                    formulas.put("Atmósfera-PSI", "Multiplicar el valor de presión por 14,696");
                    formulas.put("Atmósfera-Pascales", "multiplica el valor de presión por 101300");
                    formulas.put("Atmósfera-Torr", "Multiplicar el valor de presión por 760");
                    formulas.put("Bar-Atmósfera", "divide el valor de presión entre 1,013");
                    formulas.put("Bar-PSI", "multiplica el valor de presión por 14,504");
                    formulas.put("Bar-Pascales", "Multiplicar el valor de presión por 100000");
                    formulas.put("Bar-Torr", "multiplica el valor de presión por 750,1");
                    formulas.put("Pascales-Atmósfera", " divide el valor de presión entre 101300");
                    formulas.put("Pascales-Bar", "Divide el valor de presión entre 100000");
                    formulas.put("Pascales-PSI", " divide el valor de presión entre 6895");
                    formulas.put("Pascales-Torr", " divide el valor de presión entre 133,3");
                    formulas.put("PSI-Atmósfera", "Divide el valor de presión entre 14,696");
                    formulas.put("PSI-Bar", "divide el valor de presión entre 14,504");
                    formulas.put("PSI-Pascales", "multiplica el valor de presión por 6895");
                    formulas.put("PSI-Torr", "Multiplicar el valor de presión por 51,715");
                    formulas.put("Torr-Atmósfera", "Divide el valor de presión entre 760");
                    formulas.put("Torr-Bar", "divide el valor de presión entre 750,1");
                    formulas.put("Torr-Pascales", "Divide el valor de presión entre 51,715");
                    formulas.put("Torr-PSI", "multiplica el valor de presión por 133,3");

                    Optional.ofNullable(formulas.get(key))
                            .map(formulaTemplate -> String.format(formulaTemplate, input1.getText(), input2.getText()))
                            .ifPresent(formula -> formulaLabel.setText("Fórmula: " + formula));
                });
    }

    private static void actualizarFormula(double valorOrigen, double valorConvertido, String unidadOrigen, String unidadDestino, boolean desdeSegundoInput) {
        Map<String, String> formulas = new HashMap<>();
        formulas.put("Celsius-Fahrenheit", desdeSegundoInput ? "(%s °F − 32) × 5/9 = %s °C" : "(%s °C × 9/5) + 32 = %s °F");
        formulas.put("Fahrenheit-Celsius", desdeSegundoInput ? "(%s °C × 9/5) + 32 = %s °F" : "(%s °F − 32) × 5/9 = %s °C");
        formulas.put("Kelvin-Celsius", desdeSegundoInput ? "%s °C + 273.15 = %s K" : "%s K − 273.15 = %s °C");
        formulas.put("Celsius-Kelvin", desdeSegundoInput ? "%s K − 273.15 = %s °C" : "%s °C + 273.15 = %s K");
        formulas.put("Kelvin-Fahrenheit", desdeSegundoInput ? "(%s °F − 32) × 5/9 + 273.15 = %s K" : "(%s K − 273.15) × 9/5 + 32 = %s °F");
        formulas.put("Fahrenheit-Kelvin", desdeSegundoInput ? "(%s K − 273.15) × 9/5 + 32 = %s °F" : "(%s °F − 32) × 5/9 + 273.15 = %s K");
    
        String key = unidadOrigen + "-" + unidadDestino;
        String formulaTemplate = Optional.ofNullable(formulas.get(key))
                                         .orElseThrow(() -> new IllegalArgumentException("Conversión no soportada: " + unidadOrigen + " a " + unidadDestino));
        
        // Formatear los valores
        String valorOrigenFormateado = (String.valueOf(valorOrigen).length() >= 9) ? decimalFormat.format(valorOrigen) : twoDecimalFormat.format(valorOrigen);
        String valorConvertidoFormateado = (String.valueOf(valorConvertido).length() >= 9) ? decimalFormat.format(valorConvertido) : twoDecimalFormat.format(valorConvertido);
        
        // Ajustar la fórmula dependiendo del campo de entrada
        String formula = desdeSegundoInput 
            ? String.format(formulaTemplate, valorConvertidoFormateado, valorOrigenFormateado)
            : String.format(formulaTemplate, valorOrigenFormateado, valorConvertidoFormateado);
        
        formulaLabel.setText("Fórmula: " + formula);
    }
}
