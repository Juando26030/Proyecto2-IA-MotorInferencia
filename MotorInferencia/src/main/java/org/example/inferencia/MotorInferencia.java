package org.example.inferencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MotorInferencia {
    public boolean inferir(BaseConocimiento base, Hecho consulta) {
        System.out.println("--- Consulta: " + consulta + " ---");

        // Se convierte la base de conocimiento, tanto hechos como reglas, a Forma
        // Normal Conjuntiva (FNC)
        Set<Set<String>> clausulas = convertirAFNC(base);

        // Se niega la consulta y se añade a la lista de cláusulas para hacer resolución
        // por contradicción
        Set<String> negacionConsulta = negarHecho(consulta);
        clausulas.add(negacionConsulta);

        System.out.println("Base de conocimiento:");
        imprimirClausulas(clausulas);

        Set<Set<String>> nuevasClausulas = new HashSet<>();
        while (true) {
            int k = 1;
            List<Set<String>> listaClausulas = new ArrayList<>(clausulas);

            // Se realiza un recorrido por todas las claúsulas existentes
            for (int i = 0; i < listaClausulas.size(); i++) {
                for (int j = i + 1; j < listaClausulas.size(); j++) {
                    Set<String> resolvente = resolver(listaClausulas.get(i), listaClausulas.get(j));

                    if (resolvente != null) {
                        // Si se produce la cláusula nula, la consulta es verdadera
                        if (resolvente.isEmpty()) {
                            System.out.println("Se produjo la cláusula nula. La consulta es verdadera.");
                            return true;
                        }
                        nuevasClausulas.add(resolvente);
                    }
                }
            }

            // Si no existen nuevas cláusulas, entonces se detiene el ciclo
            if (nuevasClausulas.isEmpty()) {
                System.out.println("No se pueden generar más hijos en el árbol. La consulta es falsa.");
                return false;
            }

            if (clausulas.containsAll(nuevasClausulas)) {
                System.out.println("La consulta es falsa.");
                return false;
            }

            clausulas.addAll(nuevasClausulas);
            nuevasClausulas.clear();

            System.out.println("\n------ Cláusulas después de la inferencia------");
            imprimirClausulas(clausulas);
            k++;
        }
    }

    private Set<Set<String>> convertirAFNC(BaseConocimiento base) {
        Set<Set<String>> clausulas = new HashSet<>();
        for (Hecho hecho : base.getHechos()) {
            clausulas.add(new HashSet<>(Collections.singleton(hecho.toString())));
        }
        for (Regla regla : base.getReglas()) {
            Set<String> clausula = new HashSet<>();
            for (Hecho premisa : regla.getPremisas()) {
                clausula.add("¬" + premisa.toString());
            }
            clausula.add(regla.getConclusion().toString());
            clausulas.add(clausula);
        }
        return clausulas;
    }

    private Set<String> negarHecho(Hecho hecho) {
        Set<String> negacion = new HashSet<>();
        negacion.add("¬" + hecho.toString());
        return negacion;
    }

    private Set<String> resolver(Set<String> clausula1, Set<String> clausula2) {

        for (String literal1 : clausula1) {
            // Se genera el complemento del literal actual
            // Si el literal comienza con "¬", su complemento es el literal sin "¬".
            // Si no comienza con "¬", su complemento es el literal con "¬" añadido al
            // inicio.
            String complemento = literal1.startsWith("¬") ? literal1.substring(1) : "¬" + literal1;

            // Se verifica si el complemento del literal existe en la segunda cláusula
            if (clausula2.contains(complemento)) {
                // Si se encuentra un literal complementario, se procede a resolver las
                // cláusulas

                // Se crear un nuevo conjunto que combine los literales de ambas cláusulas
                Set<String> resultado = new HashSet<>(clausula1);
                resultado.addAll(clausula2);

                // Se elimina el literal actual y su complemento del conjunto combinado
                resultado.remove(literal1);
                resultado.remove(complemento);

                // Se imprimen las cláusulas que se están resolviendo y su respectivo resultado
                System.out.println("Resolviendo:");
                System.out.println(" - Clausula 1: " + clausula1);
                System.out.println(" - Clausula 2: " + clausula2);
                System.out.println(" - Resultado: " + resultado);
                System.out.println();

                // Se retorna la nueva cláusula resultante
                return resultado;
            }
        }
        // Si no se encuentra ningún literal complementario, se retorna null para
        // indicar que no se puede resolver
        return null;
    }

    private void imprimirClausulas(Set<Set<String>> clausulas) {
        for (Set<String> clausula : clausulas) {
            System.out.println(" - " + clausula + " \n");
        }
    }
}
