package com.mycompany.covertidor2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

import java.util.HashMap;
import java.util.Map;

public class Presion extends Categoria {
    private static final Map<String, Function<BigDecimal, BigDecimal>> conversiones = new HashMap<>();

    static {
        // Conversiones de Atmósfera a otras unidades
        conversiones.put("Atmósfera-Bar", atm -> atm.multiply(new BigDecimal("1.01325")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Atmósfera-Pascales", atm -> atm.multiply(new BigDecimal("101325")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Atmósfera-PSI", atm -> atm.multiply(new BigDecimal("14.696")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Atmósfera-Torr", atm -> atm.multiply(new BigDecimal("760")).setScale(10, RoundingMode.HALF_UP));

        // Conversiones de Bar a otras unidades
        conversiones.put("Bar-Atmósfera", bar -> bar.multiply(new BigDecimal("0.986923")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Bar-Pascales", bar -> bar.multiply(new BigDecimal("100000")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Bar-PSI", bar -> bar.multiply(new BigDecimal("14.5038")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Bar-Torr", bar -> bar.multiply(new BigDecimal("750.062")).setScale(10, RoundingMode.HALF_UP));

        // Conversiones de Pascales a otras unidades
        conversiones.put("Pascales-Atmósfera", pasc -> pasc.divide(new BigDecimal("101325"), 10, RoundingMode.HALF_UP));
        conversiones.put("Pascales-Bar", pasc -> pasc.divide(new BigDecimal("100000"), 10, RoundingMode.HALF_UP));
        conversiones.put("Pascales-PSI", pasc -> pasc.divide(new BigDecimal("6894.757"), 10, RoundingMode.HALF_UP));
        conversiones.put("Pascales-Torr", pasc -> pasc.divide(new BigDecimal("133.322"), 10, RoundingMode.HALF_UP));

        // Conversiones de PSI a otras unidades
        conversiones.put("PSI-Atmósfera", psi -> psi.divide(new BigDecimal("14.696"), 10, RoundingMode.HALF_UP));
        conversiones.put("PSI-Bar", psi -> psi.divide(new BigDecimal("14.5038"), 10, RoundingMode.HALF_UP));
        conversiones.put("PSI-Pascales", psi -> psi.multiply(new BigDecimal("6894.757")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("PSI-Torr", psi -> psi.multiply(new BigDecimal("51.7149")).setScale(10, RoundingMode.HALF_UP));

        // Conversiones de Torr a otras unidades
        conversiones.put("Torr-Atmósfera", torr -> torr.divide(new BigDecimal("760"), 10, RoundingMode.HALF_UP));
        conversiones.put("Torr-Bar", torr -> torr.divide(new BigDecimal("750.062"), 10, RoundingMode.HALF_UP));
        conversiones.put("Torr-Pascales", torr -> torr.multiply(new BigDecimal("133.322")).setScale(10, RoundingMode.HALF_UP));
        conversiones.put("Torr-PSI", torr -> torr.divide(new BigDecimal("51.7149"), 10, RoundingMode.HALF_UP));
    }
    @Override
    public String[] getUnidades() {
        return new String[]{"Pascales", "Bar", "Atmósfera", "PSI", "Torr"};
    }

    @Override
    public Function<BigDecimal, BigDecimal> obtenerConversion(String deUnidad, String aUnidad) {
        return conversiones.get(deUnidad + "-" + aUnidad);
    }

    
    
}