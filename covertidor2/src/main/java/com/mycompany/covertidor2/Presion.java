package com.mycompany.covertidor2;

import java.util.function.Function;

// Clase para conversión de Presión
public class Presion extends Categoria {

    public Presion() {
        // Conversiones de presión (usando lambdas)
        conversiones.put("Pascales-Bar", pasc -> pasc * 1e-5);
        conversiones.put("Bar-Pascales", bar -> bar / 1e-5);
        conversiones.put("Pascales-Atmósfera", pasc -> pasc * 9.8692e-6);
        conversiones.put("Atmósfera-Pascales", atm -> atm / 9.8692e-6);
        conversiones.put("Pascales-PSI", pasc -> pasc * 0.000145038);
        conversiones.put("PSI-Pascales", psi -> psi / 0.000145038);
        conversiones.put("Pascales-Torr", pasc -> pasc * 0.00750062);
        conversiones.put("Torr-Pascales", torr -> torr / 0.00750062);
    }

    @Override
    public String[] getUnidades() {
        return new String[]{"Pascales", "Bar", "Atmósfera", "PSI", "Torr"};
    }

    @Override
    public Function<Double, Double> obtenerConversion(String deUnidad, String aUnidad) {
        return conversiones.get(deUnidad + "-" + aUnidad);
    }
}