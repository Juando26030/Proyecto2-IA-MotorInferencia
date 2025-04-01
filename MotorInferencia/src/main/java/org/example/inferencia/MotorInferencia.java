package org.example.inferencia;

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
    private int resolucionesRealizadas = 0;

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

        System.out.println("\n>> Agregada cláusula de la consulta negada: [¬" + consulta + "]");

        int iteraciones = 0;
        boolean nuevaClausulaGenerada = true;
        resolucionesRealizadas = 0;

        while (nuevaClausulaGenerada && iteraciones < MAX_ITERACIONES) {
            iteraciones++;
            nuevaClausulaGenerada = false;

            Set<Set<String>> nuevasClausulas = new HashSet<>();
            List<Set<String>> listaClausulas = new ArrayList<>(clausulasConConsulta);

            if (iteraciones % 10 == 1) {
                System.out.println("\n>> Iteración " + iteraciones + " (" + listaClausulas.size() + " cláusulas)");
            }

            for (int i = 0; i < listaClausulas.size(); i++) {
                for (int j = i + 1; j < listaClausulas.size(); j++) {
                    // Intentar resolver con unificación
                    List<Set<String>> resolventes = resolverClausulasConUnificacion(
                            listaClausulas.get(i), listaClausulas.get(j));

                    if (resolventes != null && !resolventes.isEmpty()) {
                        for (Set<String> resolvente : resolventes) {
                            if (resolvente.isEmpty()) {
                                System.out.println("\n>> ENCONTRADA CLÁUSULA VACÍA!");
                                System.out.println(">> Resolución entre: " + listaClausulas.get(i) + " y " + listaClausulas.get(j));
                                System.out.println(">> Total de resoluciones realizadas: " + resolucionesRealizadas);
                                return true; // Se derivó la cláusula vacía (contradicción)
                            }

                            if (!clausulasConConsulta.contains(resolvente)) {
                                nuevasClausulas.add(resolvente);
                                nuevaClausulaGenerada = true;

                                // Solo imprimimos algunas resoluciones clave para no saturar la consola
                                if (resolucionesRealizadas % 20 == 0 || resolvente.size() <= 2) {
                                    System.out.println(">> Resolución #" + resolucionesRealizadas + ": " +
                                            listaClausulas.get(i) + " + " +
                                            listaClausulas.get(j) + " = " + resolvente);
                                }
                            }
                        }
                    }
                }
            }

            if (nuevasClausulas.size() > 0 && iteraciones % 10 == 0) {
                System.out.println(">> Generadas " + nuevasClausulas.size() + " nuevas cláusulas en iteración " + iteraciones);
            }

            clausulasConConsulta.addAll(nuevasClausulas);
        }

        if (iteraciones >= MAX_ITERACIONES) {
            System.out.println("\n>> LÍMITE DE ITERACIONES ALCANZADO: " + MAX_ITERACIONES);
            throw new Exception("No se puede determinar la consulta con la información disponible (excedido número máximo de iteraciones)");
        }

        System.out.println("\n>> NO SE ENCONTRÓ CONTRADICCIÓN después de " + resolucionesRealizadas + " resoluciones");
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

                    resolucionesRealizadas++;
                    resultado.add(resolvente);
                }
            }
        }

        return resultado;
    }

    private String aplicarSustitucion(String literal, Map<String, String> sustitucion) {
        String resultado = literal;
        Pattern pattern = Pattern.compile("\\b[mxyz]\\b|\\([mxyz]\\)|\\([mxyz],|,[mxyz]\\)|,[mxyz],");
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