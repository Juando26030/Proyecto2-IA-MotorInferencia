package org.example.inferencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MotorInferencia {
    private BaseConocimiento base;
    private static final int MAX_ITERACIONES = 100;
    private Map<String, String> sustitucionVariables = new HashMap<>();

    public MotorInferencia(BaseConocimiento base) {
        this.base = base;
    }

    public void inicializarSustituciones(Set<String> constantes) {
        sustitucionVariables.clear();
        // Las sustituciones se inicializarán con las primeras constantes disponibles
        // Se pueden agregar manualmente en el Main si se requieren sustituciones específicas
    }

    public Set<Set<String>> convertirAFNC() {
        // Extraer constantes de los hechos para posibles sustituciones
        extraerConstantesDeHechos();

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

    private void extraerConstantesDeHechos() {
        Set<String> constantes = new HashSet<>();

        // Extraer constantes de los hechos existentes
        for (Hecho hecho : base.getHechos()) {
            List<String> constantesEnHecho = extraerConstantesDeExpresion(hecho.toString());
            constantes.addAll(constantesEnHecho);
        }

        // Aquí podríamos inicializar las sustituciones automáticamente
        // Por ahora dejamos que el usuario defina las sustituciones manualmente
    }

    private List<String> extraerConstantesDeExpresion(String expresion) {
        List<String> constantes = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\([^(),]+(,[^(),]+)*\\)");
        Matcher matcher = pattern.matcher(expresion);

        if (matcher.find()) {
            String args = matcher.group(0);
            args = args.substring(1, args.length() - 1); // Quitar paréntesis
            String[] argumentos = args.split(",");

            for (String arg : argumentos) {
                arg = arg.trim();
                // Las constantes comienzan con mayúscula o son valores específicos
                if (!arg.equals("x") && !arg.equals("y") && !arg.startsWith("?")) {
                    constantes.add(arg);
                }
            }
        }

        return constantes;
    }

    public String unificarTerminos(String literal) {
        // Esta versión generalizada sustituye las variables según el mapa de sustituciones
        if (sustitucionVariables.isEmpty()) {
            return literal; // Si no hay sustituciones definidas, devolver literal sin cambios
        }

        String resultado = literal;
        for (Map.Entry<String, String> sustitucion : sustitucionVariables.entrySet()) {
            String variable = sustitucion.getKey();
            String valor = sustitucion.getValue();

            // Patrones comunes de ocurrencia de variables en predicados lógicos
            resultado = resultado.replace("(" + variable + ")", "(" + valor + ")")
                    .replace("(" + variable + ",", "(" + valor + ",")
                    .replace(", " + variable + ")", ", " + valor + ")")
                    .replace(", " + variable + ",", ", " + valor + ",");
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