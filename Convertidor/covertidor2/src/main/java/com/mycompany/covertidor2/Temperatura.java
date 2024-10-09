package com.mycompany.covertidor2;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Temperatura extends Categoria {

    private static final Map<String, Function<BigDecimal, BigDecimal>> conversiones = new HashMap<>();

    static {
        // Conversiones de Celsius a otras unidades
        conversiones.put("Celsius a Fahrenheit", c -> c.multiply(new BigDecimal("9")).divide(new BigDecimal("5"), 10, BigDecimal.ROUND_HALF_UP).add(new BigDecimal("32")));
        conversiones.put("Celsius a Kelvin", c -> c.add(new BigDecimal("273.15")));

        // Conversiones de Fahrenheit a otras unidades
        conversiones.put("Fahrenheit a Celsius", f -> f.subtract(new BigDecimal("32")).multiply(new BigDecimal("5")).divide(new BigDecimal("9"), 10, BigDecimal.ROUND_HALF_UP));
        conversiones.put("Fahrenheit a Kelvin", f -> f.subtract(new BigDecimal("32")).multiply(new BigDecimal("5")).divide(new BigDecimal("9"), 10, BigDecimal.ROUND_HALF_UP).add(new BigDecimal("273.15")));

        // Conversiones de Kelvin a otras unidades
        conversiones.put("Kelvin a Celsius", k -> k.subtract(new BigDecimal("273.15")));
        conversiones.put("Kelvin a Fahrenheit", k -> k.subtract(new BigDecimal("273.15")).multiply(new BigDecimal("9")).divide(new BigDecimal("5"), 10, BigDecimal.ROUND_HALF_UP).add(new BigDecimal("32")));
    }

    @Override
    public String[] getUnidades() {
        return new String[]{"Celsius", "Fahrenheit", "Kelvin"};
    }

    @Override
    public Function<BigDecimal, BigDecimal> obtenerConversion(String unidadOrigen, String unidadDestino) {
        return conversiones.get(unidadOrigen + " a " + unidadDestino);
    }
}