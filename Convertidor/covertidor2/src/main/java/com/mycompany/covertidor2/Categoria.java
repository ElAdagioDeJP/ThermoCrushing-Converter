package com.mycompany.covertidor2;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Categoria {

    protected final Map<String, Function<BigDecimal, BigDecimal>> conversiones = new HashMap<>();
    protected final Map<String, DecimalFormat> formatos = new HashMap<>();

    public abstract String[] getUnidades();
    public abstract Function<BigDecimal, BigDecimal> obtenerConversion(String deUnidad, String aUnidad);
    public DecimalFormat obtenerFormato(String conversion) {
        return formatos.getOrDefault(conversion, new DecimalFormat("0.#####E0"));
    }
}