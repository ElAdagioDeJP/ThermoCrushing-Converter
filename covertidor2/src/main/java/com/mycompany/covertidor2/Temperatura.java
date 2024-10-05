package com.mycompany.covertidor2;

import java.util.function.Function;

// Clase para conversión de Temperatura
public class Temperatura extends Categoria {

    public Temperatura() {
        // Conversiones de temperatura (usando lambdas)
        conversiones.put("Celsius-Kelvin", c -> c + 273.15);
        conversiones.put("Kelvin-Celsius", k -> k - 273.15);
        conversiones.put("Celsius-Fahrenheit", c -> (c * 9 / 5) + 32);
        conversiones.put("Fahrenheit-Celsius", f -> (f - 32) * 5 / 9);
        conversiones.put("Kelvin-Fahrenheit", k -> (k - 273.15) * 9 / 5 + 32);
        conversiones.put("Fahrenheit-Kelvin", f -> (f - 32) * 5 / 9 + 273.15);
    }

    @Override
    public String[] getUnidades() {
        return new String[]{"Celsius", "Kelvin", "Fahrenheit"};
    }

    @Override
    public Function<Double, Double> obtenerConversion(String deUnidad, String aUnidad) {
        return conversiones.get(deUnidad + "-" + aUnidad);
    }
}