package com.mycompany.covertidor2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Temperatura extends Categoria {

    private static final Map<String, Function<Double, Double>> conversiones = new HashMap<>();

    static {
        // Conversiones de Celsius a otras unidades
        conversiones.put("Celsius a Fahrenheit", c -> (c * 9/5) + 32);
        conversiones.put("Celsius a Kelvin", c -> c + 273.15);

        // Conversiones de Fahrenheit a otras unidades
        conversiones.put("Fahrenheit a Celsius", f -> (f - 32) * 5/9);
        conversiones.put("Fahrenheit a Kelvin", f -> (f - 32) * 5/9 + 273.15);

        // Conversiones de Kelvin a otras unidades
        conversiones.put("Kelvin a Celsius", k -> k - 273.15);
        conversiones.put("Kelvin a Fahrenheit", k -> (k - 273.15) * 9/5 + 32);
    }

    @Override
    public String[] getUnidades() {
        return new String[]{"Celsius", "Fahrenheit", "Kelvin"};
    }

    @Override
    public Function<Double, Double> obtenerConversion(String unidadOrigen, String unidadDestino) {
        return conversiones.get(unidadOrigen + " a " + unidadDestino);
    }
}