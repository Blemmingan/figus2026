package com.figus2026.view;

import java.awt.*;

/**
 * Define la paleta de colores, tipografías y tokens de diseño premium
 * para lograr una estética moderna y sofisticada tipo Web/Glow.
 */
public class Theme {

    // Colores del fondo general
    public static final Color BG_DARK = new Color(15, 15, 17);         // Negro profundo
    public static final Color BG_PANEL = new Color(26, 26, 30);        // Gris grafito para paneles
    public static final Color BG_HEADER = new Color(20, 20, 24);       // Cabecera superior
    public static final Color BORDER = new Color(42, 42, 48);          // Bordes sutiles

    // Colores de texto
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);   // Blanco perlado
    public static final Color TEXT_SECONDARY = new Color(156, 163, 175); // Gris suave
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // Gris apagado

    // Colores de estados de Figuritas (Vibrantes)
    public static final Color COLOR_NOLA = new Color(39, 39, 42);         // Gris mate oscuro
    public static final Color COLOR_LATE = new Color(16, 185, 129);       // Verde esmeralda neón
    public static final Color COLOR_REPE = new Color(245, 158, 11);       // Naranja ámbar cálido

    // Colores de hover y feedback visual
    public static final Color HOVER_ADD = new Color(16, 185, 129, 200);   // Verde semitransparente
    public static final Color HOVER_REMOVE = new Color(239, 68, 68);      // Rojo coral vibrante
    public static final Color HOVER_NOLA = new Color(51, 51, 56);         // Hover sutil sobre inactivo
    
    // Colores de componentes interactivos (pestañas, botones)
    public static final Color ACCENT_VIOLET = new Color(99, 102, 241);    // Violeta índigo
    public static final Color ACCENT_VIOLET_HOVER = new Color(79, 70, 229);
    
    // Fuentes tipográficas premium
    public static final Font FONT_TITLE = new Font("sansserif", Font.BOLD, 22);
    public static final Font FONT_GROUP_HEADER = new Font("sansserif", Font.BOLD, 14);
    public static final Font FONT_COUNTRY_CODE = new Font("sansserif", Font.BOLD, 13);
    public static final Font FONT_CELL_NUMBER = new Font("sansserif", Font.BOLD, 12);
    public static final Font FONT_BUTTON = new Font("sansserif", Font.BOLD, 12);
    public static final Font FONT_ALERT = new Font("sansserif", Font.PLAIN, 12);

    /**
     * Aplica renderizado suavizado (antialiasing) a un objeto Graphics2D.
     */
    public static void enableAntiAliasing(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}
