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
        Hecho hombre = new Hecho("Hombre", "Marco");
        Hecho gobernante = new Hecho("Gobernante", "Cesar");
        Hecho intentaAsesinar = new Hecho("IntentaAsesinar", "Marco", "Cesar");

        base.agregarHecho(hombre);
        base.agregarHecho(hombre);
        base.agregarHecho(gobernante);
        base.agregarHecho(intentaAsesinar);

        // Regla: Todo el mundo es leal a alguien
        Hecho lealMarcoAlguien = new Hecho("Leal", "Marco", "Alguien");

        Regla reglaLealtadUniversal = new Regla(
                Arrays.asList(hombre), // Todo hombre es leal a alguien
                lealMarcoAlguien
        );

        base.agregarRegla(reglaLealtadUniversal);

        // Regla: Si Marco intenta asesinar a César, no es leal a él
        Hecho noLealMarcoCesar = new Hecho("NoLeal", "Marco", "Cesar");

        Regla reglaNoLealtad = new Regla(
                Arrays.asList(hombre, gobernante, intentaAsesinar),
                noLealMarcoCesar
        );

        base.agregarRegla(reglaNoLealtad);

        // Crear motor de inferencia
        MotorInferencia motor = new MotorInferencia();

        // Consulta: ¿Marco era leal a César?
        Hecho consulta = new Hecho("Leal", "Marco", "Cesar");
        boolean resultado = motor.inferir(base, consulta);

        System.out.println("¿Cesar era hombre? " + resultado);
    }
}
