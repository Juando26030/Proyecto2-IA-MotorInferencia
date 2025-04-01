package org.example.inferencia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;
import org.example.utils.Unificador;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class MotorInferencia {
    private BaseConocimiento base;
    private static final int MAX_ITERACIONES = 1000;
    private Map<String, String> sustitucionVariables = new HashMap<>();
    private Unificador unificador = new Unificador();

    public MotorInferencia(BaseConocimiento base) {
        this.base = base;
    }

    public Set<Set<String>> convertirAFNC() {
        Set<Set<String>> clausulas = new HashSet<>();

        // Convertir hechos en cláusulas
        for (Hecho hecho : base.getHechos()) {
            Set<String> clausula = new HashSet<>();
            clausula.add(hecho.toString());
            clausulas.add(clausula);
        }

        // Convertir reglas en cláusulas en FNC
        for (Regla regla : base.getReglas()) {
            Set<String> clausula = new HashSet<>();

            // Negamos las premisas
            for (Hecho premisa : regla.getPremisas()) {
                clausula.add("¬" + premisa.toString());
            }

            // Agregamos la conclusión
            clausula.add(regla.getConclusion().toString());
            clausulas.add(clausula);
        }

        // Agregar cláusulas disyuntivas (para reglas tipo A ⇒ B ∨ C)
        for (Set<String> clausulaDisyuntiva : base.getClausulasDisyuntivas()) {
            clausulas.add(new HashSet<>(clausulaDisyuntiva));
        }

        return clausulas;
    }

    public boolean resolver(Set<Set<String>> clausulas, String consulta) throws Exception {
        // Negamos la consulta para la refutación
        Set<String> clausulaNegada = new HashSet<>();
        clausulaNegada.add("¬" + consulta);

        // Creamos una copia de las cláusulas para no modificar el conjunto original
        Set<Set<String>> clausulasConConsulta = new HashSet<>(clausulas);
        clausulasConConsulta.add(clausulaNegada);

        int iteraciones = 0;
        boolean nuevaClausulaGenerada = true;

        while (nuevaClausulaGenerada && iteraciones < MAX_ITERACIONES) {
            iteraciones++;
            nuevaClausulaGenerada = false;

            Set<Set<String>> nuevasClausulas = new HashSet<>();
            List<Set<String>> listaClausulas = new ArrayList<>(clausulasConConsulta);

            for (int i = 0; i < listaClausulas.size(); i++) {
                for (int j = i + 1; j < listaClausulas.size(); j++) {
                    // Intentar resolver con unificación
                    List<Set<String>> resolventes = resolverClausulasConUnificacion(
                            listaClausulas.get(i), listaClausulas.get(j));

                    if (resolventes != null && !resolventes.isEmpty()) {
                        for (Set<String> resolvente : resolventes) {
                            if (resolvente.isEmpty()) {
                                return true; // Se derivó la cláusula vacía (contradicción)
                            }

                            if (!clausulasConConsulta.contains(resolvente)) {
                                nuevasClausulas.add(resolvente);
                                nuevaClausulaGenerada = true;
                            }
                        }
                    }
                }
            }

            clausulasConConsulta.addAll(nuevasClausulas);
        }

        if (iteraciones >= MAX_ITERACIONES) {
            throw new Exception("No se puede determinar la consulta con la información disponible (excedido número máximo de iteraciones)");
        }

        return false; // No se pudo derivar la contradicción
    }

    private List<Set<String>> resolverClausulasConUnificacion(Set<String> c1, Set<String> c2) {
        List<Set<String>> resultado = new ArrayList<>();

        for (String literal1 : c1) {
            String literal1SinNegacion = literal1.startsWith("¬") ? literal1.substring(1) : literal1;
            String literal1Negado = literal1.startsWith("¬") ? literal1.substring(1) : "¬" + literal1;

            for (String literal2 : c2) {
                // Comprobar si los literales son unificables y complementarios
                Map<String, String> sustitucion = null;

                if (literal2.equals(literal1Negado)) {
                    // Caso simple: literales idénticos pero de signo contrario
                    sustitucion = new HashMap<>();
                } else if ((literal2.startsWith("¬") && !literal1.startsWith("¬")) ||
                        (!literal2.startsWith("¬") && literal1.startsWith("¬"))) {
                    // Intentar unificar literal1 sin negación con literal2 sin negación
                    String literal2SinNegacion = literal2.startsWith("¬") ? literal2.substring(1) : literal2;

                    sustitucion = unificador.unificarExpresiones(literal1SinNegacion, literal2SinNegacion);
                }

                if (sustitucion != null) {
                    // Crear resolvente aplicando la sustitución
                    Set<String> resolvente = new HashSet<>();

                    // Añadir todas las cláusulas de c1 excepto literal1
                    for (String l : c1) {
                        if (!l.equals(literal1)) {
                            resolvente.add(aplicarSustitucion(l, sustitucion));
                        }
                    }

                    // Añadir todas las cláusulas de c2 excepto literal2
                    for (String l : c2) {
                        if (!l.equals(literal2)) {
                            resolvente.add(aplicarSustitucion(l, sustitucion));
                        }
                    }

                    resultado.add(resolvente);
                }
            }
        }

        return resultado;
    }

    private String aplicarSustitucion(String literal, Map<String, String> sustitucion) {
        String resultado = literal;
        Pattern pattern = Pattern.compile("\\b[xyz]\\b|\\([xyz]\\)|\\([xyz],|,[xyz]\\)|,[xyz],");
        Matcher matcher = pattern.matcher(resultado);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            for (Map.Entry<String, String> entry : sustitucion.entrySet()) {
                String variable = entry.getKey();
                String valor = entry.getValue();

                if (match.contains(variable)) {
                    String reemplazo = match.replace(variable, valor);
                    matcher.appendReplacement(sb, reemplazo);
                    break;
                }
            }
        }
        matcher.appendTail(sb);

        return sb.length() > 0 ? sb.toString() : resultado;
    }
}