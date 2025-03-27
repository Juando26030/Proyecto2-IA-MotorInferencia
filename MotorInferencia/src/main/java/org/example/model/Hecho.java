package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Hecho {
    private String predicado;
    private String sujeto;
    private String objeto; // Puede ser null si es unario

    public Hecho(String predicado, String sujeto) {
        this(predicado, sujeto, null);
    }

    @Override
    public String toString() {
        return objeto == null ? predicado + "(" + sujeto + ")" : predicado + "(" + sujeto + ", " + objeto + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hecho hecho = (Hecho) obj;
        return predicado.equals(hecho.predicado) &&
                sujeto.equals(hecho.sujeto) &&
                ((objeto == null && hecho.objeto == null) || (objeto != null && objeto.equals(hecho.objeto)));
    }

    @Override
    public int hashCode() {
        int result = predicado.hashCode();
        result = 31 * result + sujeto.hashCode();
        result = 31 * result + (objeto != null ? objeto.hashCode() : 0);
        return result;
    }

}
