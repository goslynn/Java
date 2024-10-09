// StringUtils.java
package taskmanager; // Cambia 'tu.paquete.util' a la ubicación adecuada en tu proyecto

public class StringUtils {

    /**
     * Normaliza una cadena de texto eliminando espacios en blanco y convirtiéndola a minúsculas.
     *
     * @param input La cadena de texto a normalizar.
     * @return La cadena de texto normalizada sin espacios y en minúsculas.
     */
    public static String normalizeString(String input) {
        if (input == null) {
            return "";  // Devolver una cadena vacía si la entrada es null para evitar errores.
        }

        // Convertir a minúsculas y eliminar espacios en blanco.
        return input.toLowerCase().replaceAll("\\s", "");
    }
}
