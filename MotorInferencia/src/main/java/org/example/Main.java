package org.example;

import org.example.inferencia.MotorInferencia;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        BaseConocimiento base = new BaseConocimiento();

        // Hechos iniciales
        Hecho hombreMarco = new Hecho("Hombre(Marco)");
        Hecho pompeyanoMarco = new Hecho("Pompeyano(Marco)");
        Hecho gobernanteCesar = new Hecho("Gobernante(Cesar)");
        Hecho intentaAsesinarMarcoCesar = new Hecho("IntentaAsesinar(Marco, Cesar)");

        base.agregarHecho(hombreMarco);
        base.agregarHecho(pompeyanoMarco);
        base.agregarHecho(gobernanteCesar);
        base.agregarHecho(intentaAsesinarMarcoCesar);

        // Regla 3: ∀x Pompeyano(x) ⇒ Romano(x)
        Set<Hecho> premisas1 = new HashSet<>();
        premisas1.add(new Hecho("Pompeyano(x)"));
        base.agregarRegla(new Regla(premisas1, new Hecho("Romano(x)")));

        // Regla 5: ∀x Romano(x) ⇒ (Leal(x, Cesar) ∨ Odia(x, Cesar))
        // Para FNC, se convierte a: ¬Romano(x) ∨ Leal(x, Cesar) ∨ Odia(x, Cesar)
        Set<Set<String>> clausulasDisyuntivas = new HashSet<>();
        Set<String> disyuncion = new HashSet<>();
        disyuncion.add("¬Romano(x)");
        disyuncion.add("Leal(x, Cesar)");
        disyuncion.add("Odia(x, Cesar)");
        clausulasDisyuntivas.add(disyuncion);
        base.agregarClausulasDisyuntivas(clausulasDisyuntivas);

        // Regla 6: ∀x ∀y (Hombre(x) ∧ Gobernante(y) ∧ IntentaAsesinar(x, y)) ⇒ ¬Leal(x, y)
        Set<Hecho> premisas3 = new HashSet<>();
        premisas3.add(new Hecho("Hombre(x)"));
        premisas3.add(new Hecho("Gobernante(y)"));
        premisas3.add(new Hecho("IntentaAsesinar(x, y)"));
        base.agregarRegla(new Regla(premisas3, new Hecho("¬Leal(x, y)")));

        // Motor de inferencia
        MotorInferencia motor = new MotorInferencia(base);
        Set<Set<String>> clausulas = motor.convertirAFNC();

        System.out.println("Cláusulas en FNC:");
        for (Set<String> clausula : clausulas) {
            System.out.println(clausula);
        }

        // Pruebas con distintas consultas
        realizarConsulta(motor, clausulas, "Romano(Marco)");
        realizarConsulta(motor, clausulas, "Pompeyano(Cesar)");
        realizarConsulta(motor, clausulas, "Leal(Marco, Cesar)");
        realizarConsulta(motor, clausulas, "IntentaAsesinar(Cesar, Marco)");
        realizarConsulta(motor, clausulas, "Romano(Cesar)");
        realizarConsulta(motor, clausulas, "Odia(Marco, Cesar)");
    }

    private static void realizarConsulta(MotorInferencia motor, Set<Set<String>> clausulas, String consulta) {
        try {
            boolean resultado = motor.resolver(new HashSet<>(clausulas), consulta);
            System.out.println("¿" + consulta + "? " + (resultado ? "Sí" : "No"));
        } catch (Exception e) {
            System.out.println("¿" + consulta + "? No hay hechos o reglas para deducir esto con certeza");
        }
    }
}