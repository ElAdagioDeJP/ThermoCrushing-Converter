package com.mycompany.covertidor2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JComboBox;

public class CategoriaCambioListener implements ActionListener {
    private Map<String, Runnable> categoriasMap;
    private JComboBox<String> categoriaBox;

    public CategoriaCambioListener(Map<String, Runnable> categoriasMap, JComboBox<String> categoriaBox) {
        this.categoriasMap = categoriasMap;
        this.categoriaBox = categoriaBox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String categoriaSeleccionada = (String) categoriaBox.getSelectedItem();
        categoriasMap.get(categoriaSeleccionada).run();
    }
}