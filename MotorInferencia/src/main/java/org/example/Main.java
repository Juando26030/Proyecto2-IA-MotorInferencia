package org.example;

import org.example.inferencia.MotorInferencia;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        BaseConocimiento base = new BaseConocimiento();

        // Agregar hechos iniciales
        Hecho aliceConoceBob = new Hecho("Conoce", "Alice", "Bob");
        Hecho bobConoceJames = new Hecho("Conoce", "Bob", "James");

        base.agregarHecho(aliceConoceBob);
        base.agregarHecho(bobConoceJames);

        // Agregar regla lógica: Si A conoce B y B conoce C, entonces A conoce C
        Regla transitividad = new Regla(
                Arrays.asList(aliceConoceBob, bobConoceJames),
                new Hecho("Conoce", "Alice", "James")
        );
        base.agregarRegla(transitividad);

        // Crear motor de inferencia
        MotorInferencia motor = new MotorInferencia();

        // Hacer consulta
        Hecho consulta = new Hecho("Conoce", "Alice", "James");
        boolean resultado = motor.inferir(base, consulta);

        System.out.println("¿Alice conoce a James por transitividad? " + resultado);
    }
}
