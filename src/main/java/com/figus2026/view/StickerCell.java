package com.figus2026.view;

import com.figus2026.model.Estado;
import com.figus2026.model.Figurita;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Componente interactivo personalizado de Swing que dibuja una celda cuadrada
 * de figurita con bordes redondeados, feedback táctil al pasar el mouse (hover),
 * y colores adaptativos según el estado y el modo de la grilla.
 */
public class StickerCell extends JPanel {

    private final Figurita figurita;
    private final MainFrame mainFrame;
    private boolean mouseHovering = false;

    public StickerCell(Figurita figurita, MainFrame mainFrame) {
        this.figurita = figurita;
        this.mainFrame = mainFrame;

        // Tamaño preferido y mínimo cuadrado para la celda
        Dimension size = new Dimension(36, 36);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setOpaque(false); // Permite dibujar esquinas redondeadas limpias sin pintar el fondo del panel contenedor

        // Listener para hover y clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseHovering = true;
                updateCursor();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseHovering = false;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    procesarClick();
                }
            }
        });
    }

    /**
     * Determina si la celda está activa en la vista actual.
     * En vista TENGO: activa si el estado es LATE o REPE.
     * En vista REPETIDAS: activa si el estado es REPE.
     */
    public boolean isActivaEnVistaActual() {
        boolean esVistaTengo = mainFrame.isVistaTengoActiva();
        Estado est = figurita.getEstado();
        if (esVistaTengo) {
            return est == Estado.LATE || est == Estado.REPE;
        } else {
            return est == Estado.REPE;
        }
    }

    /**
     * Determina si la celda es interactuable según el modo y vista actual.
     */
    public boolean isInteractuable() {
        boolean esModoAgregar = mainFrame.isModoAgregarActivo();
        boolean activa = isActivaEnVistaActual();

        if (esModoAgregar) {
            // En modo Agregar, solo interactúan las celdas inactivas en esta vista
            return !activa;
        } else {
            // En modo Quitar, solo interactúan las celdas activas en esta vista
            return activa;
        }
    }

    /**
     * Actualiza el cursor del mouse a mano (HAND) o por defecto.
     */
    private void updateCursor() {
        if (isInteractuable()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Procesa la acción de hacer click en la celda según el estado y el modo.
     */
    private void procesarClick() {
        if (!isInteractuable()) return;

        boolean esVistaTengo = mainFrame.isVistaTengoActiva();
        boolean esModoAgregar = mainFrame.isModoAgregarActivo();

        if (esVistaTengo) {
            if (esModoAgregar) {
                // Agregar a TENGO -> pasa a LATE
                mainFrame.getAlbum().cambiarEstadoFigurita(figurita.getCodigo(), Estado.LATE);
            } else {
                // Quitar de TENGO -> pasa a NOLA
                mainFrame.getAlbum().cambiarEstadoFigurita(figurita.getCodigo(), Estado.NOLA);
            }
        } else {
            if (esModoAgregar) {
                // Agregar a REPETIDAS -> pasa a REPE
                mainFrame.getAlbum().cambiarEstadoFigurita(figurita.getCodigo(), Estado.REPE);
            } else {
                // Quitar de REPETIDAS -> pasa a LATE
                mainFrame.getAlbum().cambiarEstadoFigurita(figurita.getCodigo(), Estado.LATE);
            }
        }

        // Feedback sonoro opcional o visual inmediato: refrescar toda la UI
        mainFrame.refrescarGrillas();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Theme.enableAntiAliasing(g2d);

        int w = getWidth();
        int h = getHeight();
        boolean activa = isActivaEnVistaActual();
        boolean esModoAgregar = mainFrame.isModoAgregarActivo();
        boolean esVistaTengo = mainFrame.isVistaTengoActiva();

        // Determinar el color de fondo base
        Color colorFondo;
        Color colorTexto;
        Color colorBorde = Theme.BORDER;

        if (activa) {
            // Celda activa (LATE en verde, REPE en naranja)
            if (esVistaTengo) {
                colorFondo = Theme.COLOR_LATE;
            } else {
                colorFondo = Theme.COLOR_REPE;
            }
            colorTexto = Color.WHITE;
            
            // Si el mouse está encima y estamos en modo Quitar, mostramos alerta en rojo (eliminar)
            if (mouseHovering && !esModoAgregar) {
                colorFondo = Theme.HOVER_REMOVE;
            }
        } else {
            // Celda atenuada (inactiva en esta vista)
            colorFondo = Theme.COLOR_NOLA;
            colorTexto = Theme.TEXT_MUTED;

            // Si el mouse está encima y estamos en modo Agregar, mostramos glow del estado correspondiente
            if (mouseHovering && esModoAgregar) {
                if (esVistaTengo) {
                    colorFondo = Theme.HOVER_ADD;
                } else {
                    colorFondo = new Color(Theme.COLOR_REPE.getRed(), Theme.COLOR_REPE.getGreen(), Theme.COLOR_REPE.getBlue(), 200);
                }
                colorTexto = Color.WHITE;
            } else if (mouseHovering) {
                // Hover pasivo si no se puede interactuar
                colorFondo = Theme.HOVER_NOLA;
            }
        }

        // Dibujar fondo redondeado
        g2d.setColor(colorFondo);
        g2d.fillRoundRect(0, 0, w, h, 8, 8);

        // Dibujar borde sutil
        if (mouseHovering && isInteractuable()) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.2f));
        } else {
            g2d.setColor(colorBorde);
            g2d.setStroke(new BasicStroke(1.0f));
        }
        g2d.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

        // Escribir el número de la figurita
        g2d.setFont(Theme.FONT_CELL_NUMBER);
        g2d.setColor(colorTexto);

        // Si está en hover activo y es interactuable, opcionalmente dibujamos un símbolo sutil (+ o -)
        String texto = String.valueOf(figurita.getNumero());
        if (mouseHovering && isInteractuable()) {
            if (esModoAgregar) {
                texto = "+";
            } else {
                texto = "-";
            }
            g2d.setFont(Theme.FONT_TITLE.deriveFont(Font.BOLD, 14f));
        }

        FontMetrics fm = g2d.getFontMetrics();
        int tx = (w - fm.stringWidth(texto)) / 2;
        int ty = ((h - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(texto, tx, ty);

        g2d.dispose();
    }
}
