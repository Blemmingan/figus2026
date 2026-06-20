package com.figus2026.model;

import java.util.Objects;

/**
 * Representa una figurita individual en el álbum del Mundial 2026.
 */
public class Figurita {
    private final String codigo;
    private final String pais;
    private int qty;

    /**
     * Crea una nueva figurita con el código y estado inicial.
     * El país se extrae automáticamente de las primeras 3 letras del código.
     *
     * @param codigo Código de la figurita (3 letras mayúsculas seguidas de un
     *               número sin espacios).
     * @throws IllegalArgumentException si el formato del código es inválido.
     */
    public Figurita(String codigo) {
        if (codigo == null || !codigo.matches("^[A-Z]{3}\\d+$")) {
            throw new IllegalArgumentException("Código de figurita inválido: " + codigo +
                    ". Debe estar compuesto por 3 letras mayúsculas seguidas de un número.");
        }
        this.codigo = codigo;
        this.pais = codigo.substring(0, 3);
        this.qty = 0;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getPais() {
        return pais;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    /**
     * Obtiene el número de figurita a partir de su código.
     * Por ejemplo, si el código es "ARG12", devuelve 12.
     *
     * @return El número entero de la figurita.
     */
    public int getNumero() {
        return Integer.parseInt(codigo.substring(3));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Figurita figurita = (Figurita) o;
        return Objects.equals(codigo, figurita.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Figurita{" +
                "codigo='" + codigo + '\'' +
                ", pais='" + pais + '\'' +
                ", qty=" + qty +
                '}';
    }
}
