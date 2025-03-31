package org.example.inferencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MotorInferencia {
    private BaseConocimiento base;
    private static final int MAX_ITERACIONES = 100;

    public Set<Set<String>> convertirAFNC() {
        Set<Set<String>> clausulas = new HashSet<>();

        // Convertir hechos en cláusulas
        for (Hecho hecho : base.getHechos()) {
            Set<String> clausula = new HashSet<>();
            clausula.add(unificarTerminos(hecho.toString()));
            clausulas.add(clausula);
        }

        // Convertir reglas en cláusulas en FNC
        for (Regla regla : base.getReglas()) {
            Set<String> clausula = new HashSet<>();

            // Negamos las premisas
            for (Hecho premisa : regla.getPremisas()) {
                clausula.add("¬" + unificarTerminos(premisa.toString()));
            }

            // Agregamos la conclusión
            clausula.add(unificarTerminos(regla.getConclusion().toString()));
            clausulas.add(clausula);
        }

        // Agregar cláusulas disyuntivas (para reglas tipo A ⇒ B ∨ C)
        for (Set<String> clausulaDisyuntiva : base.getClausulasDisyuntivas()) {
            Set<String> clausulaUnificada = new HashSet<>();
            for (String literal : clausulaDisyuntiva) {
                clausulaUnificada.add(unificarTerminos(literal));
            }
            clausulas.add(clausulaUnificada);
        }

        return clausulas;
    }

    private String unificarTerminos(String literal) {
        // Reemplaza variables por constantes específicas del problema
        String resultado = literal;
        if (literal.contains("(x)") || literal.contains("(x,") || literal.contains(", x)") || literal.contains(", x,")) {
            resultado = resultado.replace("(x)", "(Marco)").replace("(x,", "(Marco,").replace(", x)", ", Marco)").replace(", x,", ", Marco,");
        }
        if (literal.contains("(y)") || literal.contains("(y,") || literal.contains(", y)") || literal.contains(", y,")) {
            resultado = resultado.replace("(y)", "(Cesar)").replace("(y,", "(Cesar,").replace(", y)", ", Cesar)").replace(", y,", ", Cesar,");
        }
        return resultado;
    }

    public boolean resolver(Set<Set<String>> clausulas, String consulta) throws Exception {
        // Negamos la consulta para la refutación
        Set<String> clausulaNegada = new HashSet<>();
        clausulaNegada.add("¬" + unificarTerminos(consulta));
        clausulas.add(clausulaNegada);

        int iteraciones = 0;
        boolean nuevaClausulaGenerada = true;

        while (nuevaClausulaGenerada && iteraciones < MAX_ITERACIONES) {
            iteraciones++;
            nuevaClausulaGenerada = false;

            Set<Set<String>> nuevasClausulas = new HashSet<>();
            List<Set<String>> listaClausulas = new ArrayList<>(clausulas);

            for (int i = 0; i < listaClausulas.size(); i++) {
                for (int j = i + 1; j < listaClausulas.size(); j++) {
                    Set<String> resolvente = resolverClausulas(listaClausulas.get(i), listaClausulas.get(j));

                    if (resolvente != null) {
                        if (resolvente.isEmpty()) {
                            return true; // Se derivó la cláusula vacía (contradicción)
                        }

                        if (!clausulas.contains(resolvente)) {
                            nuevasClausulas.add(resolvente);
                            nuevaClausulaGenerada = true;
                        }
                    }
                }
            }

            clausulas.addAll(nuevasClausulas);
        }

        if (iteraciones >= MAX_ITERACIONES) {
            throw new Exception("No se puede determinar la consulta con la información disponible");
        }

        return false; // No se pudo derivar la contradicción
    }

    private Set<String> resolverClausulas(Set<String> c1, Set<String> c2) {
        for (String literal1 : c1) {
            String complemento;
            if (literal1.startsWith("¬")) {
                complemento = literal1.substring(1);
            } else {
                complemento = "¬" + literal1;
            }

            if (c2.contains(complemento)) {
                Set<String> resolvente = new HashSet<>(c1);
                resolvente.remove(literal1);
                Set<String> c2Copia = new HashSet<>(c2);
                c2Copia.remove(complemento);
                resolvente.addAll(c2Copia);
                return resolvente;
            }
        }
        return null; // No se pudo resolver
    }
}