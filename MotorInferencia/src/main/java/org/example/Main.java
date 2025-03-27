package org.example;

public class Main {
    private final Inferencia inferencia = new Inferencia();
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
    public void cargarReglasBase(){
        System.out.println("Reglas cargadas en predicados");
    };

    public void cargarHechos(){
        System.out.println("Hechos cargados en predicados");
    };

    public void cargarPreguntas(){
        System.out.println("Preguntas cargadas en predicados");
    };

    public void callConjuntiva(){

        inferencia.predicadosToConjuntiva();
    };

}