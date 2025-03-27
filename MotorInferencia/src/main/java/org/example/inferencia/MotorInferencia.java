package org.example.inferencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.HashSet;
import java.util.Set;

public class MotorInferencia {
    public boolean inferir(BaseConocimiento base, Hecho consulta) {
        System.out.println("🔍 Consulta: " + consulta);
        Set<Hecho> hechosConocidos = new HashSet<>(base.getHechos());

        //Imprimir hechos conocidos
        System.out.println("Hechos conocidos: ");
        for (Hecho hecho : hechosConocidos) {
            System.out.println(hecho);
        }

        // Primero, verificar si el hecho ya está en la base de conocimiento
        if (hechosConocidos.contains(consulta)) {
            System.out.println("🎉 Hecho encontrado: " + consulta);
            return true;
        }

        boolean nuevoHechoAgregado = true;
        while (nuevoHechoAgregado) {
            nuevoHechoAgregado = false;

            for (Regla regla : base.getReglas()) {
                if (hechosConocidos.containsAll(regla.getPremisas()) && !hechosConocidos.contains(regla.getConclusion())) {
                    hechosConocidos.add(regla.getConclusion());
                    System.out.println("🧠 Inferido: " + regla.getConclusion());
                    nuevoHechoAgregado = true;

                    // Verificar si el hecho inferido es la consulta
                    if (regla.getConclusion().equals(consulta)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
