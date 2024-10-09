
package com.mycompany.covertidor2;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class ConvertidorUnidades {

    private static Categoria categoriaActual;
    private static JComboBox<String> categoriaBox, unidad1Box, unidad2Box;
    private static JTextField input1, input2;
    private static JLabel formulaLabel;
    private static boolean isUpdating = false;  // Flag para evitar ciclo infinito de eventos
    private static int previus1selected=0;
    private static int previus2selected=1;
    private static boolean isInitialized = false;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Convertidor de Unidades");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridBagLayout());
            frame.setSize(500, 300);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
    
            String[] categorias = {"Temperatura", "Presión"};
            categoriaBox = new JComboBox<>(categorias);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            frame.add(categoriaBox, gbc);
    
            JPanel panel1 = new JPanel(new GridBagLayout());
            GridBagConstraints gbcPanel1 = new GridBagConstraints();
            gbcPanel1.insets = new Insets(0, 0, 0, 0);
            gbcPanel1.gridx = 0; gbcPanel1.gridy = 0;
            input1 = new JTextField(10);
            panel1.add(input1, gbcPanel1);
            gbcPanel1.gridy = 1;
            unidad1Box = new JComboBox<>();
            input1.setPreferredSize(new Dimension(250, 25));
            panel1.add(unidad1Box, gbcPanel1);
    
            JPanel panel2 = new JPanel(new GridBagLayout());
            GridBagConstraints gbcPanel2 = new GridBagConstraints();
            gbcPanel2.insets = new Insets(0, 0, 0, 0);
            gbcPanel2.gridx = 0; gbcPanel2.gridy = 0;
            input2 = new JTextField(10);
            panel2.add(input2, gbcPanel2);
            gbcPanel2.gridy = 1;
            unidad2Box = new JComboBox<>();
            input2.setPreferredSize(new Dimension(20, 25));
            panel2.add(unidad2Box, gbcPanel2);
    
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            frame.add(panel1, gbc);
    
            JLabel equalsLabel = new JLabel("=");
            equalsLabel.setFont(new Font("Arial", Font.BOLD, 24));
            gbc.gridx = 1; gbc.gridy = 1; gbc.gridheight = 1; gbc.gridwidth = 1;
            frame.add(equalsLabel, gbc);
    
            gbc.gridx = 2;
            frame.add(panel2, gbc);
    
            formulaLabel = new JLabel("Fórmula: ");
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
            formulaLabel.setPreferredSize(new Dimension(287, 50));
            frame.add(formulaLabel, gbc);

            categoriaBox.addItemListener(e -> {
                Optional.of(e)
                    .filter(event -> event.getStateChange() == ItemEvent.SELECTED)
                    .ifPresent(event -> limpiarInputs());

            });
      DocumentListener convertir = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
            }
            public void removeUpdate(DocumentEvent e) {
                actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
            }
            public void insertUpdate(DocumentEvent e) {
                actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
            }
        };

        DocumentListener invertirConversión = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
            }
            public void removeUpdate(DocumentEvent e) {
                actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
            }
            public void insertUpdate(DocumentEvent e) {
                actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
            }
        };

        input1.getDocument().addDocumentListener(convertir);
        input2.getDocument().addDocumentListener(invertirConversión);

        ItemListener unidadCambioListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Map<Integer, Runnable> acciones = new HashMap<>();
                acciones.put(ItemEvent.SELECTED, () -> {
                    actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
                    actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
                    actualizarFormula();
                });

                Optional.ofNullable(acciones.get(e.getStateChange())).ifPresent(Runnable::run);
            }
        };
        unidad1Box.addItemListener(unidadCambioListener);
        unidad2Box.addItemListener(unidadCambioListener);

        Map<String, Runnable> categoriasMap = new HashMap<>();
        categoriasMap.put("Temperatura", () -> actualizarCategoria(new Temperatura()));
        categoriasMap.put("Presión", () -> actualizarCategoria(new Presion()));

        categoriaBox.addActionListener(new CategoriaCambioListener(categoriasMap, categoriaBox));

        actualizarCategoria(new Temperatura());

        KeyListener limpiarLetrasKeyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                limpiarLetras(input1, input2);
            }
        };

        input1.addKeyListener(limpiarLetrasKeyListener);
        input2.addKeyListener(limpiarLetrasKeyListener);

        frame.setVisible(true);

        unidad1Box.addActionListener(evt -> unitSelector1ActionPerformed(evt));
        unidad2Box.addActionListener(evt -> unitSelector2ActionPerformed(evt));

        previus1selected = unidad1Box.getSelectedIndex();
        previus2selected = 1;

        Optional.of(unidad2Box.getItemCount())
                .filter(count -> count > 1)
                .ifPresent(count -> unidad2Box.setSelectedIndex(previus2selected));

        isInitialized = true;
        });
    }

    private static void limpiarInputs() {
        input1.setText("");
        input2.setText("");
    }
    // Cambiar el tipo de datos en la función actualizarConversión
public static void actualizarConversión(JTextField inputOrigen, JTextField inputDestino, JComboBox<String> unidadOrigenBox, JComboBox<String> unidadDestinoBox, boolean desdeSegundoInput) {
    Runnable actualizar = () -> {
        try {
            String textoOrigen = inputOrigen.getText();
            String unidadOrigen = (String) unidadOrigenBox.getSelectedItem();
            String unidadDestino = (String) unidadDestinoBox.getSelectedItem();
            Optional.of(textoOrigen)
                    .filter(texto -> !texto.isEmpty())
                    .map(BigDecimal::new)
                    .ifPresentOrElse(valorOrigen -> {
                        Function<BigDecimal, BigDecimal> conversion = Optional.ofNullable(categoriaActual.obtenerConversion(unidadOrigen, unidadDestino))
                                .orElseThrow(() -> new IllegalArgumentException("Conversión no soportada: " + unidadOrigen + " a " + unidadDestino));
                        BigDecimal valorConvertido = conversion.apply(valorOrigen);

                        // Obtener el formato específico para la conversión
                        DecimalFormat formato = obtenerFormato(unidadOrigen, unidadDestino);

                        // Formatear el valor convertido
                        String valorFormateado = usarNotacionCientifica(unidadOrigen, unidadDestino, valorConvertido)
                                ? String.format("%.3e", valorConvertido) // Notación científica
                                : formato.format(valorConvertido); // Formato específico

                       

                        inputDestino.setText(valorFormateado);
                        actualizarFormula(valorOrigen, valorConvertido, unidadOrigen, unidadDestino, desdeSegundoInput);
                    }, () -> {
                        inputDestino.setText("");
                        actualizarFormula(BigDecimal.ZERO, BigDecimal.ZERO, unidadOrigen, unidadDestino, desdeSegundoInput);
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

// Cambiar el tipo de datos en la función actualizarFormula
private static void actualizarFormula(BigDecimal valorOrigen, BigDecimal valorConvertido, String unidadOrigen, String unidadDestino, boolean desdeSegundoInput) {
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

    // Obtener el formato específico para la conversión
    DecimalFormat formato = obtenerFormatoTemperatura();

    // Formatear los valores usando el formato específico
    String valorOrigenFormateado = formatearValor(valorOrigen, formato);
    String valorConvertidoFormateado = formatearValor(valorConvertido, formato);

    // Ajustar la fórmula dependiendo del campo de entrada
    String formula = desdeSegundoInput 
        ? String.format(formulaTemplate, valorConvertidoFormateado, valorOrigenFormateado)
        : String.format(formulaTemplate, valorOrigenFormateado, valorConvertidoFormateado);

    formulaLabel.setText("Fórmula: " + formula);
}

// Método para formatear el valor con notación científica si es necesario
private static String formatearValor(BigDecimal valor, DecimalFormat formato) {
    String valorFormateado = formato.format(valor);
    return (valor.precision() >= 7 || valor.abs().compareTo(BigDecimal.valueOf(1e-6)) < 0) 
        ? String.format("%.3e", valor) 
        : valorFormateado;
}


    
// Método para obtener el formato específico para cada conversión
// Método para obtener el formato específico para cada conversión
private static DecimalFormat obtenerFormato(String unidadOrigen, String unidadDestino) {
    DecimalFormat decimalFormat = new DecimalFormat();
    Set<String> unidadesPresion = Set.of("Atmósfera", "Bar", "PSI", "Pascales", "Torr");

    boolean esPresion = unidadesPresion.contains(unidadOrigen) && unidadesPresion.contains(unidadDestino);
    decimalFormat.setMaximumFractionDigits(esPresion ? 6 : 4); // Mostrar hasta 6 dígitos decimales para presión, 4 para otros
    decimalFormat.setMinimumFractionDigits(esPresion ? 0 : 3); // Mostrar al menos 0 dígitos decimales para presión, 3 para otros
    
    decimalFormat.setGroupingUsed(false); // No usar separadores de miles
    return decimalFormat;
}
private static boolean usarNotacionCientifica(String unidadOrigen, String unidadDestino, BigDecimal valor) {
    Set<String> unidadesTemperatura = Set.of("Celsius", "Fahrenheit", "Kelvin");
    Set<String> unidadesPresion = Set.of("Atmósfera", "Bar", "PSI", "Pascales", "Torr");

    return unidadesTemperatura.contains(unidadOrigen) && unidadesTemperatura.contains(unidadDestino)
        ? valor.abs().compareTo(new BigDecimal("1e20")) >= 0
        : unidadesPresion.contains(unidadOrigen) && unidadesPresion.contains(unidadDestino)
            ? valor.abs().compareTo(new BigDecimal("1e20")) >= 0
            : false;
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

        
    
        unidad1Box.setSelectedIndex(0);
        unidad2Box.setSelectedIndex(Math.min(1, unidad2Box.getItemCount() - 1));
    
        Optional.of(categoriaActual)
                .filter(cat -> cat instanceof Presion)
                .ifPresentOrElse(cat -> {
                    unidad1Box.setSelectedItem("Pascal");
                    unidad2Box.setSelectedItem("Bar");
                }, () -> Optional.of(categoriaActual)
                        .filter(cat -> cat instanceof Temperatura)
                        .ifPresentOrElse(cat -> {
                            unidad1Box.setSelectedItem("Celsius");
                            unidad2Box.setSelectedItem("Fahrenheit");
                        }, () -> {
                            previus1selected = Math.min(previus1selected, unidad1Box.getItemCount() - 1);
                            previus2selected = Math.min(previus2selected, unidad2Box.getItemCount() - 1);
                            previus2selected = (previus1selected == previus2selected) ? (previus2selected + 1) % unidad2Box.getItemCount() : previus2selected;
                            unidad1Box.setSelectedIndex(previus1selected);
                            unidad2Box.setSelectedIndex(previus2selected);
                        }));
    
        actualizarFormula();
    }
    private static void unitSelector1ActionPerformed(java.awt.event.ActionEvent evt) {
        Runnable makeSwap = unidad1Box.getSelectedIndex() == unidad2Box.getSelectedIndex() && isInitialized
                ? () -> {
                    int temp = previus1selected;
                    previus1selected = previus2selected;
                    previus2selected = temp;
                    System.out.println("swap");
                    Optional.of(unidad2Box.getItemCount())
                            .filter(count -> count > 0)
                            .ifPresent(count -> unidad2Box.setSelectedIndex(previus2selected));
                    actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
                    actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
                }
                : () -> {
                    previus1selected = unidad1Box.getSelectedIndex();
                };
        makeSwap.run();
    }
    
    private static void unitSelector2ActionPerformed(java.awt.event.ActionEvent evt) {
        Runnable makeSwap = unidad1Box.getSelectedIndex() == unidad2Box.getSelectedIndex() && isInitialized
                ? () -> {
                    int temp = previus1selected;
                    previus1selected = previus2selected;
                    previus2selected = temp;
                    System.out.println("swap");
                    Optional.of(unidad1Box.getItemCount())
                            .filter(count -> count > 0)
                            .ifPresent(count -> unidad1Box.setSelectedIndex(previus1selected));
                            
                    actualizarConversión(input1, input2, unidad1Box, unidad2Box, false);
                    actualizarConversión(input2, input1, unidad2Box, unidad1Box, true);
                }
                : () -> {
                    previus2selected = unidad2Box.getSelectedIndex();
                };
        makeSwap.run();
    }

    private static void limpiarLetras(JTextField input1, JTextField input2) {
        Runnable clearText = () -> {
            input1.setText("");
            input2.setText("");
        };
        Optional.of(input1.getText())
            .filter(text -> text.chars().anyMatch(Character::isLetter))
            .flatMap(text -> Optional.of(input2.getText())
                .filter(text2 -> text2.chars().anyMatch(Character::isLetter)))
            .ifPresent(text -> SwingUtilities.invokeLater(clearText));  }


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

   
    
    // Método para obtener el formato específico para las conversiones de temperatura
    private static DecimalFormat obtenerFormatoTemperatura() {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        decimalFormat.setMaximumFractionDigits(3); // Limitar a 3 decimales
        decimalFormat.setMinimumFractionDigits(2); // Mostrar al menos 2 dígitos decimales
        decimalFormat.setGroupingUsed(false); // No usar separadores de miles
        return decimalFormat;
    }
    
   
}