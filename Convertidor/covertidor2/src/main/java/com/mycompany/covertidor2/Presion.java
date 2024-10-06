package com.mycompany.covertidor2;

import java.util.function.Function;

// Clase para conversión de Presión
public class Presion extends Categoria {

    public Presion() {
        // Conversiones de presión (usando lambdas)
        conversiones.put("Pascales-Bar", pasc -> pasc * 1e-5);
        conversiones.put("Pascales-Atmósfera", pasc -> pasc * 9.86923e-6);
        conversiones.put("Pascales-PSI", pasc -> pasc * 0.000145038);
        conversiones.put("Pascales-Torr", pasc -> pasc * 0.00750062);
        conversiones.put("Bar-Pascales", bar -> bar / 1e-5);
        conversiones.put("Atmósfera-Pascales", atm -> atm * 101325);
        conversiones.put("PSI-Pascales", psi -> psi / 0.000145038);
        conversiones.put("Torr-Pascales", torr -> torr / 0.00750062);

        conversiones.put("Bar-Atmósfera", bar -> bar * 0.986923);
        conversiones.put("Bar-PSI", bar -> bar * 14.5038);
        conversiones.put("Bar-Torr", bar -> bar * 750.062);
        conversiones.put("Atmósfera-Bar", atm -> atm / 1.01325);
        conversiones.put("PSI-Bar", psi -> psi / 14.5038);
        conversiones.put("Torr-Bar", torr -> torr / 750.062);

        conversiones.put("Atmósfera-PSI", atm -> atm * 14.696);
        conversiones.put("Atmósfera-Torr", atm -> atm * 760);
        conversiones.put("PSI-Atmósfera", psi -> psi / 14.696);
        conversiones.put("Torr-Atmósfera", torr -> torr / 760);

        conversiones.put("PSI-Torr", psi -> psi * 51.7149);
        conversiones.put("Torr-PSI", torr -> torr / 51.7149);
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