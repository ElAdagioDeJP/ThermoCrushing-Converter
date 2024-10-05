package com.mycompany.covertidor2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Clase base para Categorías
public abstract class Categoria {
    protected final Map<String, Function<Double, Double>> conversiones = new HashMap<>();

    public abstract String[] getUnidades();
    public abstract Function<Double, Double> obtenerConversion(String deUnidad, String aUnidad);
}