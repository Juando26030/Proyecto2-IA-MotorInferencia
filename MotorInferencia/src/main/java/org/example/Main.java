package org.example;

import org.example.inferencia.MotorInferencia;
import org.example.model.BaseConocimiento;
import org.example.model.Hecho;
import org.example.model.Regla;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        //Debido a que usamos impresiones de consola, se recomienda ejecutar este código ejemplo por ejemplo, por eso está comentado la sección del ejemplo Marco y César
        //Si quiere probar esta sección, descomente el código y comente la sección del ejemplo Jack y animales
        //System.out.println("\n\n###### EJEMPLO: MARCO Y CESAR ######");
        //ejecutarEjemploMarcoYCesar();

        //System.out.println("\n\n###### EJEMPLO: JACK Y ANIMALES ######");
        //ejecutarEjemploJackAnimales();

        System.out.println("\n\n ###### EJEMPLO: CRIMEN DEL CORONEL WEST #####");
        ejecutarEjemploCrimenNono();
    }

    private static void ejecutarEjemploMarcoYCesar() {
        System.out.println("\nEjemplo Marco y César:");
        System.out.println("Base de conocimiento:");
        System.out.println("1. Hombre(Marco)");
        System.out.println("2. Pompeyano(Marco)");
        System.out.println("3. ∀x Pompeyano(x) ⇒ Romano(x)");
        System.out.println("4. Gobernante(Cesar)");
        System.out.println("5. ∀x Romano(x) ⇒ (Leal(x, Cesar) ∨ Odia(x, Cesar))");
        System.out.println("6. ∀x ∀y (Hombre(x) ∧ Gobernante(y) ∧ IntentaAsesinar(x, y)) ⇒ ¬Leal(x, y)");
        System.out.println("7. IntentaAsesinar(Marco, Cesar)");

        BaseConocimiento base = crearBaseMarcoYCesar();

        // Motor de inferencia
        MotorInferencia motor = new MotorInferencia(base);
        Set<Set<String>> clausulas = motor.convertirAFNC();

        System.out.println("\nCláusulas en FNC:");
        imprimirClausulas(clausulas);

        // Pruebas con distintas consultas
        realizarConsulta(motor, clausulas, "Romano(Marco)");
        realizarConsulta(motor, clausulas, "Odia(Marco, Cesar)");
    }

    private static BaseConocimiento crearBaseMarcoYCesar() {
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

        return base;
    }

    private static void ejecutarEjemploJackAnimales() {
        System.out.println("\nEjemplo Jack y Animales:");
        System.out.println("Base de conocimiento:");
        System.out.println("1. ∀x_1{∃y_1[Animal(y_1) ∧ Ama(x_1, y_1)] ⇒ ∃z_1 Ama(z_1, x_1)}");
        System.out.println("2. ∀x_2[∃y_2[Mata(x_2, y_2) ∧ Animal(y_2)] ⇒ ∀z_2 ¬Ama(z_2, x_2)]");
        System.out.println("3. ∀x_3 Animal(x_3) ⇒ Ama(Jack, x_3)");
        System.out.println("4. Mata(Jack, Tuna) V Mata(Curiosidad, Tuna)");
        System.out.println("5. ∀x_5 Gato(x_5) ⇒ Animal(x_5)");
        System.out.println("6. Gato(Tuna)");

        BaseConocimiento base = crearBaseJackAnimales();

        // Motor de inferencia
        MotorInferencia motor = new MotorInferencia(base);
        Set<Set<String>> clausulas = motor.convertirAFNC();

        System.out.println("\nCláusulas en FNC (ejemplo Jack y animales):");
        imprimirClausulas(clausulas);

        System.out.println("\n=== CONSULTAS CLAVE ===");

        // Consulta si Jack mata a Tuna
        realizarConsulta(motor, clausulas, "Ama(Jack, Tuna)");
    }


    private static BaseConocimiento crearBaseJackAnimales() {
        
        BaseConocimiento base = new BaseConocimiento();

        // Hechos iniciales del ejemplo Jack y animales
        Hecho gatoTuna = new Hecho("Gato(Tuna)");
        base.agregarHecho(gatoTuna);

        // Regla 3: ∀x_3 Animal(x_3) ⇒ Ama(Jack, x_3)
        Set<Hecho> premisasR3 = new HashSet<>();
        premisasR3.add(new Hecho("Animal(x)"));
        base.agregarRegla(new Regla(premisasR3, new Hecho("Ama(Jack, x)")));

        // Regla 5: ∀x_5 Gato(x_5) ⇒ Animal(x_5)
        Set<Hecho> premisasR5 = new HashSet<>();
        premisasR5.add(new Hecho("Gato(x)"));
        base.agregarRegla(new Regla(premisasR5, new Hecho("Animal(x)")));

        // Regla 1: ∀x_1{∃y_1[Animal(y_1) ∧ Ama(x_1, y_1)] ⇒ ∃z_1 Ama(z_1, x_1)}
        // Transformamos a FNC: ¬Animal(y) ∨ ¬Ama(x, y) ∨ Ama(z, x)
        Set<Set<String>> clausulasR1 = new HashSet<>();
        Set<String> disyuncionR1 = new HashSet<>();
        disyuncionR1.add("¬Animal(y)");
        disyuncionR1.add("¬Ama(x, y)");
        disyuncionR1.add("Ama(z, x)");
        clausulasR1.add(disyuncionR1);
        base.agregarClausulasDisyuntivas(clausulasR1);

        // Regla 2: ∀x_2[∃y_2[Mata(x_2, y_2) ∧ Animal(y_2)] ⇒ ∀z_2 ¬Ama(z_2, x_2)]
        // Transformamos a FNC: ¬Mata(x, y) ∨ ¬Animal(y) ∨ ¬Ama(z, x)
        Set<Set<String>> clausulasR2 = new HashSet<>();
        Set<String> disyuncionR2 = new HashSet<>();
        disyuncionR2.add("¬Mata(x, y)");
        disyuncionR2.add("¬Animal(y)");
        disyuncionR2.add("¬Ama(z, x)");
        clausulasR2.add(disyuncionR2);
        base.agregarClausulasDisyuntivas(clausulasR2);

        // Regla 4: Mata(Jack, Tuna) V Mata(Curiosidad, Tuna)
        Set<Set<String>> clausulasR4 = new HashSet<>();
        Set<String> disyuncionR4 = new HashSet<>();
        disyuncionR4.add("Mata(Jack, Tuna)");
        disyuncionR4.add("Mata(Curiosidad, Tuna)");
        clausulasR4.add(disyuncionR4);
        base.agregarClausulasDisyuntivas(clausulasR4);

        return base;
    }

    private static void ejecutarEjemploCrimenNono(){
        System.out.println("\nEjemplo Crimen Nono:");
        System.out.println("Base de conocimiento:");
        System.out.println("1. Enemigo(Nono, America)");
        System.out.println("2. Misil(M)");
        System.out.println("3. Americano(West)");
        System.out.println("4. Tiene(Nono, M)");
        System.out.println("5. ∀x Americano(x) ∧ Arma(y) ∧ Vende(x, y, z) ∧ Hostil(z) ⇒ Criminal(x)");
        System.out.println("6. ∀x Tiene(Nono, x) ⇒ Misil(x)");
        System.out.println("7. ∀x Misil(x) ∧ Tiene(Nono, x) ⇒ Vende(West, x, Nono)");
        System.out.println("8. ∀x Enemigo(x, America) ⇒ Hostil(x)");
        System.out.println("9. Misil(x) ⇒ Arma(x)");

        BaseConocimiento base = crearBaseCrimenNono();

        // Motor de inferencia
        MotorInferencia motor = new MotorInferencia(base);
        Set<Set<String>> clausulas = motor.convertirAFNC();

        System.out.println("\nCláusulas en FNC:");
        imprimirClausulas(clausulas);

        realizarConsulta(motor, clausulas, "Criminal(West)");

    }
    
    private static BaseConocimiento crearBaseCrimenNono(){
        BaseConocimiento base = new BaseConocimiento();

        // Hechos iniciales
        Hecho enemigoNonoAmerica = new Hecho("Enemigo(Nono, America)"); // Clausula 2
        Hecho misilM1 = new Hecho("Misil(m)"); // Clausula 4
        Hecho americanoWest = new Hecho("Americano(West)"); // Clausula 5
        Hecho tieneNonoM1 = new Hecho("Tiene(Nono, m)"); // Clusula 3

        base.agregarHecho(enemigoNonoAmerica);
        base.agregarHecho(misilM1);
        base.agregarHecho(americanoWest);
        base.agregarHecho(tieneNonoM1);

        // Regla 1
        Set<Hecho> premisasR1 = new HashSet<>();
        premisasR1.add(new Hecho("Americano(x)"));
        premisasR1.add(new Hecho("Arma(y)"));
        premisasR1.add(new Hecho("Vende(x, y, z)"));
        premisasR1.add(new Hecho("Hostil(z)"));
        base.agregarRegla(new Regla(premisasR1, new Hecho("Criminal(x)")));

        // Regla 3 y 4
        Set<Hecho> premisasR3 = new HashSet<>();
        premisasR3.add(new Hecho("Tiene(Nono, x)"));
        base.agregarRegla(new Regla(premisasR3, new Hecho("Misil(x)")));

        // Regla 5
        Set<Hecho> premisasR4 = new HashSet<>();
        premisasR4.add(new Hecho("Misil(x)"));
        premisasR4.add(new Hecho("Tiene(Nono, x)"));
        base.agregarRegla(new Regla(premisasR4, new Hecho("Vende(West, x, Nono)")));

        // Regla 6
        Set<Hecho> premisasR6 = new HashSet<>();
        premisasR6.add(new Hecho("Enemigo(x, America)"));
        base.agregarRegla(new Regla(premisasR6, new Hecho("Hostil(x)")));

        Set<Hecho> premisasR7 = new HashSet<>();
        premisasR7.add(new Hecho("Misil(m)"));
        base.agregarRegla(new Regla(premisasR7, new Hecho("Arma(m)")));

        return base;

    }


    private static void imprimirClausulas(Set<Set<String>> clausulas) {
        int i = 1;
        for (Set<String> clausula : clausulas) {
            System.out.println((i++) + ". " + clausula);
        }
    }

    private static void realizarConsulta(MotorInferencia motor, Set<Set<String>> clausulas, String consulta) {
        System.out.println("\n=============================================");
        System.out.println("CONSULTA: ¿" + consulta + "?");
        System.out.println("=============================================");

        try {
            boolean resultado = motor.resolver(new HashSet<>(clausulas), consulta);

            System.out.println("\n>> RESULTADO FINAL: " + (resultado ? "SÍ" : "NO"));
            System.out.println("=============================================");
        } catch (Exception e) {
            System.out.println("Error al procesar la consulta: " + e.getMessage());
        }
    }
}