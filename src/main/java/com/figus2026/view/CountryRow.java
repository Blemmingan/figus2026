package com.figus2026.view;

import com.figus2026.model.Figurita;
import com.figus2026.util.FlagManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Representa una fila individual en la grilla del álbum.
 * Contiene a la izquierda la bandera redondeada del país y su código ISO (3 letras),
 * y a la derecha una grilla horizontal con las 20 (o 19) figuritas.
 */
public class CountryRow extends JPanel {

    private final String codigoPais;
    private final JLabel labelBandera;

    public CountryRow(String codigoPais, LinkedList<Figurita> figuritas, FlagManager flagManager, MainFrame mainFrame) {
        this.codigoPais = codigoPais;

        setLayout(new BorderLayout(15, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // --- Panel de Identificación del País (Izquierda) ---
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelInfo.setOpaque(false);
        panelInfo.setPreferredSize(new Dimension(140, 36));

        // Label para la bandera
        labelBandera = new JLabel();
        actualizarBandera(flagManager);
        panelInfo.add(labelBandera);

        // Label para el nombre/código de país
        JLabel labelCodigo = new JLabel(codigoPais);
        labelCodigo.setFont(Theme.FONT_COUNTRY_CODE);
        labelCodigo.setForeground(Theme.TEXT_PRIMARY);
        panelInfo.add(labelCodigo);

        add(panelInfo, BorderLayout.WEST);

        // --- Panel de Grilla de Figuritas (Centro/Derecha) ---
        JPanel panelCeldas = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelCeldas.setOpaque(false);

        for (Figurita f : figuritas) {
            StickerCell cell = new StickerCell(f, mainFrame);
            panelCeldas.add(cell);
        }

        add(panelCeldas, BorderLayout.CENTER);
    }

    /**
     * Carga y escala la bandera usando FlagManager de forma asíncrona.
     */
    public final void actualizarBandera(FlagManager flagManager) {
        // Bandera de tamaño 34x24 px
        ImageIcon icon = flagManager.getFlagIcon(codigoPais, 34, 24, () -> {
            // Callback: repintar la bandera cuando se complete la descarga
            actualizarBandera(flagManager);
        });
        labelBandera.setIcon(icon);
        labelBandera.repaint();
    }
}
