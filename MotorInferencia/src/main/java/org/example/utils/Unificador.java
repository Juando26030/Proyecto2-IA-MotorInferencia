package org.example.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unificador {

    /**
     * Clase que representa un término en lógica de primer orden
     * Puede ser una variable, constante o un término compuesto (predicado)
     */
    public static class Termino {
        private String nombre;
        private List<Termino> argumentos;
        private boolean esVariable;

        public Termino(String expresion) {
            parsearExpresion(expresion);
        }

        private void parsearExpresion(String expresion) {
            // Verificar si es negación
            boolean esNegacion = expresion.startsWith("¬");
            if (esNegacion) {
                expresion = expresion.substring(1);
            }

            // Buscar paréntesis para determinar si es predicado con argumentos
            int abreParentesis = expresion.indexOf('(');
            if (abreParentesis != -1) {
                // Es un predicado (término compuesto)
                this.nombre = (esNegacion ? "¬" : "") + expresion.substring(0, abreParentesis);
                this.esVariable = false;
                this.argumentos = new ArrayList<>();

                // Extraer argumentos
                String argsStr = expresion.substring(abreParentesis + 1, expresion.length() - 1);
                String[] args = argsStr.split(",");
                for (String arg : args) {
                    this.argumentos.add(new Termino(arg.trim()));
                }
            } else {
                // Es una variable o constante
                this.nombre = expresion;
                this.argumentos = new ArrayList<>();
                // En lógica de primer orden, las variables comienzan con minúscula o son símbolos específicos
                this.esVariable = expresion.equals("x") || expresion.equals("y") ||
                        expresion.equals("z") || expresion.startsWith("?");
            }
        }

        public boolean esVariable() {
            return this.esVariable;
        }

        public String getNombre() {
            return this.nombre;
        }

        public List<Termino> getArgumentos() {
            return this.argumentos;
        }

        public boolean tieneArgumentos() {
            return !this.argumentos.isEmpty();
        }

        @Override
        public String toString() {
            if (!tieneArgumentos()) {
                return nombre;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(nombre).append("(");
                for (int i = 0; i < argumentos.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(argumentos.get(i).toString());
                }
                sb.append(")");
                return sb.toString();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Termino otroTermino = (Termino) obj;

            if (!nombre.equals(otroTermino.nombre)) return false;
            if (argumentos.size() != otroTermino.argumentos.size()) return false;

            for (int i = 0; i < argumentos.size(); i++) {
                if (!argumentos.get(i).equals(otroTermino.argumentos.get(i))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nombre, argumentos);
        }
    }

    /**
     * Realiza la unificación de dos términos
     * @param t1 Primer término
     * @param t2 Segundo término
     * @return Mapa de sustituciones que hace que t1 y t2 sean idénticos, o null si no es posible
     */
    public Map<String, Termino> unificar(Termino t1, Termino t2) {
        return unificar(t1, t2, new HashMap<>());
    }

    private Map<String, Termino> unificar(Termino t1, Termino t2, Map<String, Termino> sustituciones) {
        // Si ya tenemos sustituciones, aplicarlas primero
        if (!sustituciones.isEmpty()) {
            t1 = aplicarSustituciones(t1, sustituciones);
            t2 = aplicarSustituciones(t2, sustituciones);
        }

        // Si los términos son idénticos, no se necesita sustitución adicional
        if (t1.equals(t2)) {
            return sustituciones;
        }

        // Si t1 es variable
        if (t1.esVariable()) {
            return unificarVariable(t1, t2, sustituciones);
        }

        // Si t2 es variable
        if (t2.esVariable()) {
            return unificarVariable(t2, t1, sustituciones);
        }

        // Si ambos son términos compuestos (predicados)
        if (t1.tieneArgumentos() && t2.tieneArgumentos()) {
            // Los nombres de los predicados deben coincidir
            if (!t1.getNombre().equals(t2.getNombre())) {
                return null; // No se pueden unificar predicados diferentes
            }

            // Los predicados deben tener el mismo número de argumentos
            if (t1.getArgumentos().size() != t2.getArgumentos().size()) {
                return null; // No se pueden unificar
            }

            // Unificar recursivamente cada argumento
            Map<String, Termino> theta = new HashMap<>(sustituciones);
            for (int i = 0; i < t1.getArgumentos().size(); i++) {
                theta = unificar(t1.getArgumentos().get(i), t2.getArgumentos().get(i), theta);
                if (theta == null) {
                    return null; // Fallo en la unificación de argumentos
                }
            }

            return theta;
        }

        // Si llegamos aquí, los términos no se pueden unificar
        return null;
    }

    private Map<String, Termino> unificarVariable(Termino var, Termino termino, Map<String, Termino> sustituciones) {
        String varNombre = var.getNombre();

        // Verificar si la variable ya tiene una sustitución
        if (sustituciones.containsKey(varNombre)) {
            return unificar(sustituciones.get(varNombre), termino, sustituciones);
        }

        // Verificar "occur check" - La variable no debe aparecer en el término
        if (ocurreEn(var, termino)) {
            return null; // No se puede unificar (ciclo infinito)
        }

        // Crear nueva sustitución
        Map<String, Termino> resultado = new HashMap<>(sustituciones);
        resultado.put(varNombre, termino);
        return resultado;
    }

    private boolean ocurreEn(Termino var, Termino termino) {
        if (termino.esVariable()) {
            return termino.getNombre().equals(var.getNombre());
        }

        // Buscar recursivamente en los argumentos
        for (Termino arg : termino.getArgumentos()) {
            if (ocurreEn(var, arg)) {
                return true;
            }
        }

        return false;
    }

    private Termino aplicarSustituciones(Termino termino, Map<String, Termino> sustituciones) {
        // Si es una variable y tiene sustitución
        if (termino.esVariable() && sustituciones.containsKey(termino.getNombre())) {
            return sustituciones.get(termino.getNombre());
        }

        // Si es un término compuesto, aplicar sustituciones a los argumentos
        if (termino.tieneArgumentos()) {
            List<Termino> nuevosArgs = new ArrayList<>();
            for (Termino arg : termino.getArgumentos()) {
                nuevosArgs.add(aplicarSustituciones(arg, sustituciones));
            }

            // Crear nuevo término con los argumentos sustituidos
            Termino nuevoTermino = new Termino(termino.getNombre() + "()");
            nuevoTermino.argumentos = nuevosArgs;
            return nuevoTermino;
        }

        // Si no es variable o no tiene sustitución, devolver el mismo término
        return termino;
    }

    /**
     * Convierte un mapa de sustituciones de Términos a un mapa de Strings para uso en el motor
     */
    public Map<String, String> convertirSustitucionesAString(Map<String, Termino> sustituciones) {
        Map<String, String> resultado = new HashMap<>();
        for (Map.Entry<String, Termino> entry : sustituciones.entrySet()) {
            resultado.put(entry.getKey(), entry.getValue().toString());
        }
        return resultado;
    }

    /**
     * Unifica dos expresiones lógicas dadas como strings
     */
    public Map<String, String> unificarExpresiones(String expr1, String expr2) {
        try {
            Termino t1 = new Termino(expr1);
            Termino t2 = new Termino(expr2);

            Map<String, Termino> sustituciones = unificar(t1, t2);
            if (sustituciones == null) {
                return null;
            }

            return convertirSustitucionesAString(sustituciones);
        } catch (Exception e) {
            return null; // Error al parsear o unificar
        }
    }
}