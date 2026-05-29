package com.figus2026.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Representa el álbum completo de figuritas, organizando las figuritas por países y grupos,
 * y gestionando la persistencia de datos de forma automática.
 */
public class Album {

    // Archivo donde se guardará el estado de las figuritas
    private static final String FILE_NAME = "album.json";
    private static final String FALLBACK_DIR = System.getProperty("user.home") + "/.figus2026";
    private static final String FALLBACK_FILE_PATH = FALLBACK_DIR + "/album.json";

    // Estructura de grupos predefinida
    public static final LinkedHashMap<String, String[]> GRUPOS = new LinkedHashMap<>();
    static {
        GRUPOS.put("Grupo A", new String[]{"MEX", "RSA", "KOR", "CZE"});
        GRUPOS.put("Grupo B", new String[]{"CAN", "BIH", "QAT", "SUI"});
        GRUPOS.put("Grupo C", new String[]{"BRA", "MAR", "HAI", "SCO"});
        GRUPOS.put("Grupo D", new String[]{"USA", "PAR", "AUS", "TUR"});
        GRUPOS.put("Grupo E", new String[]{"GER", "CUW", "CIV", "ECU"});
        GRUPOS.put("Grupo F", new String[]{"NED", "JPN", "SWE", "TUN"});
        GRUPOS.put("Grupo G", new String[]{"BEL", "EGY", "IRN", "NZL"});
        GRUPOS.put("Grupo H", new String[]{"ESP", "CPV", "KSA", "URU"});
        GRUPOS.put("Grupo I", new String[]{"FRA", "SEN", "IRQ", "NOR"});
        GRUPOS.put("Grupo J", new String[]{"ARG", "ALG", "AUT", "JOR"});
        GRUPOS.put("Grupo K", new String[]{"POR", "COD", "UZB", "COL"});
        GRUPOS.put("Grupo L", new String[]{"ENG", "CRO", "GHA", "PAN"});
        GRUPOS.put("Especiales", new String[]{"FWC"});
    }

    // Mapa principal que asocia cada código de país con su lista doblemente enlazada (LinkedList) de figuritas
    private final LinkedHashMap<String, LinkedList<Figurita>> paisesFiguritas;

    /**
     * Inicializa un nuevo álbum.
     * Si existe un archivo guardado, carga su estado. De lo contrario, inicializa todo en NOLA.
     */
    public Album() {
        this.paisesFiguritas = new LinkedHashMap<>();
        inicializarEstructuraVacia();
        cargarEstado();
    }

    /**
     * Inicializa la estructura de países y figuritas con todas en estado NOLA.
     * Mantiene estrictamente el orden preestablecido de los grupos y países.
     */
    private void inicializarEstructuraVacia() {
        for (String[] paises : GRUPOS.values()) {
            for (String pais : paises) {
                LinkedList<Figurita> figuritas = new LinkedList<>();
                int cantidad = pais.equals("FWC") ? 19 : 20;
                for (int i = 1; i <= cantidad; i++) {
                    String codigo = pais + i;
                    figuritas.add(new Figurita(codigo, Estado.NOLA));
                }
                paisesFiguritas.put(pais, figuritas);
            }
        }
    }

    /**
     * Retorna el mapa completo de figuritas agrupadas por país.
     *
     * @return LinkedHashMap que preserva el orden de países, asociando cada uno a su LinkedList de figuritas.
     */
    public LinkedHashMap<String, LinkedList<Figurita>> getPaisesFiguritas() {
        return paisesFiguritas;
    }

    /**
     * Obtiene la LinkedList de figuritas para un país determinado.
     *
     * @param codigoPais Código del país (e.g. "ARG").
     * @return LinkedList de Figuritas de ese país.
     */
    public LinkedList<Figurita> getFiguritasDelPais(String codigoPais) {
        return paisesFiguritas.get(codigoPais);
    }

    /**
     * Busca una figurita específica en el álbum mediante su código único.
     *
     * @param codigo Código de la figurita (e.g., "ARG12").
     * @return La Figurita correspondiente, o null si no se encuentra.
     */
    public Figurita getFigurita(String codigo) {
        if (codigo == null || codigo.length() < 4) return null;
        String pais = codigo.substring(0, 3);
        LinkedList<Figurita> figuritas = paisesFiguritas.get(pais);
        if (figuritas != null) {
            for (Figurita f : figuritas) {
                if (f.getCodigo().equals(codigo)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Actualiza el estado de una figurita y guarda el álbum de forma automática.
     *
     * @param codigo Código de la figurita.
     * @param nuevoEstado El nuevo Estado (NOLA, LATE, REPE).
     */
    public void cambiarEstadoFigurita(String codigo, Estado nuevoEstado) {
        Figurita figurita = getFigurita(codigo);
        if (figurita != null) {
            figurita.setEstado(nuevoEstado);
            guardarEstado();
        }
    }

    /**
     * Guarda el estado actual del álbum en formato JSON en el archivo album.json.
     * Si no puede escribir en el directorio actual, lo guarda en el directorio de usuario.
     */
    public synchronized void guardarEstado() {
        Map<String, String> estadoMap = new HashMap<>();
        for (LinkedList<Figurita> lista : paisesFiguritas.values()) {
            for (Figurita f : lista) {
                estadoMap.put(f.getCodigo(), f.getEstado().name());
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr = gson.toJson(estadoMap);

        // Intentar guardar en el directorio local del proyecto
        File fileLocal = new File(FILE_NAME);
        try (FileWriter writer = new FileWriter(fileLocal)) {
            writer.write(jsonStr);
            return; // Guardado exitoso localmente
        } catch (IOException e) {
            System.err.println("No se pudo guardar en " + FILE_NAME + ", intentando fallback...");
        }

        // Si falla localmente, intentar guardar en la carpeta personal de usuario
        File dirFallback = new File(FALLBACK_DIR);
        if (!dirFallback.exists()) {
            dirFallback.mkdirs();
        }
        File fileFallback = new File(FALLBACK_FILE_PATH);
        try (FileWriter writer = new FileWriter(fileFallback)) {
            writer.write(jsonStr);
        } catch (IOException e) {
            System.err.println("Error grave: No se pudo persistir el álbum en ninguna ubicación: " + e.getMessage());
        }
    }

    /**
     * Carga el estado del álbum desde el archivo JSON si existe.
     * Primero intenta en el directorio local, luego en la carpeta del usuario.
     */
    public synchronized void cargarEstado() {
        File fileToLoad = new File(FILE_NAME);
        if (!fileToLoad.exists()) {
            fileToLoad = new File(FALLBACK_FILE_PATH);
        }

        if (!fileToLoad.exists()) {
            System.out.println("No se encontró archivo de guardado previo. Inicializando álbum vacío...");
            return;
        }

        try (FileReader reader = new FileReader(fileToLoad)) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> estadoMap = gson.fromJson(reader, mapType);

            if (estadoMap != null) {
                for (LinkedList<Figurita> lista : paisesFiguritas.values()) {
                    for (Figurita f : lista) {
                        String estadoNombre = estadoMap.get(f.getCodigo());
                        if (estadoNombre != null) {
                            try {
                                f.setEstado(Estado.valueOf(estadoNombre));
                            } catch (IllegalArgumentException e) {
                                f.setEstado(Estado.NOLA);
                            }
                        }
                    }
                }
                System.out.println("Estado del álbum cargado con éxito desde " + fileToLoad.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo de guardado. Se inicializará con valores por defecto. Detalle: " + e.getMessage());
        }
    }
}
