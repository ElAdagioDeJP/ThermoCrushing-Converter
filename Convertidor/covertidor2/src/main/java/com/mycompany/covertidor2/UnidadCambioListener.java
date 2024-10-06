package com.mycompany.covertidor2;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class UnidadCambioListener implements ItemListener {
    private JTextField input1;
    private JTextField input2;
    private JComboBox<String> unidad1Box;
    private JComboBox<String> unidad2Box;

    public UnidadCambioListener(JTextField input1, JTextField input2, JComboBox<String> unidad1Box, JComboBox<String> unidad2Box) {
        this.input1 = input1;
        this.input2 = input2;
        this.unidad1Box = unidad1Box;
        this.unidad2Box = unidad2Box;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Predicate<Integer> esSeleccionado = state -> state == ItemEvent.SELECTED;
        Consumer<ItemEvent> ejecutarConversion = event -> {
            ConvertidorUnidades.actualizarConversión(input1, input2, unidad1Box, unidad2Box, true);
            cambiarUnidadSiEsIgual(unidad1Box, unidad2Box);
        };

        // Verifica el Predicate y ejecuta el Consumer si es verdadero
        Optional.of(e.getStateChange())
                .filter(esSeleccionado)
                .ifPresent(state -> ejecutarConversion.accept(e));
    }

    private void cambiarUnidadSiEsIgual(JComboBox<String> unidad1Box, JComboBox<String> unidad2Box) {
        Runnable cambiarUnidad = () -> {
            int siguienteIndice = (unidad2Box.getSelectedIndex() + 1) % unidad2Box.getItemCount();
            unidad2Box.setSelectedIndex(siguienteIndice);
        };

        // Verifica si las unidades son iguales y cambia la unidad
        Optional.of(unidad1Box.getSelectedItem())
                .filter(unidad -> unidad.equals(unidad2Box.getSelectedItem()))
                .ifPresent(unidad -> cambiarUnidad.run());
    }
}