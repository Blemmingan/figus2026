package com.figus2026.view;

import com.figus2026.model.Album;
import com.figus2026.model.Figurita;
import com.figus2026.util.FlagManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ventana principal y controladora de la interfaz gráfica.
 * Implementa el diseño premium oscuro con navegación de pestañas de alta gama,
 * panel de estadísticas de progreso y grilla scrolleable agrupada por grupos.
 */
public class MainFrame extends JFrame {

    private final Album album;
    private final FlagManager flagManager;

    // Estados de la interfaz
    private boolean vistaTengoActiva = true; // true: TENGO, false: REPETIDAS
    private boolean modoAgregarActivo = true; // true: Agregar, false: Quitar

    // Componentes interactivos que requieren actualizar su estado visual
    private JButton btnPestañaTengo;
    private JButton btnPestañaRepetidas;
    private JButton btnToggleModo;
    private JLabel labelProgresoTengo;
    private JLabel labelProgresoRepes;
    private JProgressBar progressBarTengo;
    
    private JPanel gridContainer;

    public MainFrame(Album album) {
        this.album = album;
        this.flagManager = new FlagManager();

        // Configuración básica de la ventana
        setTitle("Figus 2026 — Gestor de Álbum Oficial");
        setSize(1100, 720);
        setMinimumSize(new Dimension(850, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        // Diseñar la ventana
        initUI();

        // Apagar el executor de banderas al cerrar la app
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                flagManager.shutdown();
            }
        });
    }

    public Album getAlbum() {
        return album;
    }

    public boolean isVistaTengoActiva() {
        return vistaTengoActiva;
    }

    public boolean isModoAgregarActivo() {
        return modoAgregarActivo;
    }

    /**
     * Inicializa y ensambla los componentes de la interfaz de usuario.
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // --- 1. Panel Superior (Header & Barra de Herramientas) ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Theme.BG_HEADER);
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Título del Álbum a la izquierda
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panelTitulo.setOpaque(false);
        JLabel iconWorldCup = new JLabel(flagManager.getFlagIcon("FWC", 32, 24, null));
        JLabel labelTitulo = new JLabel("FIGUS 2026");
        labelTitulo.setFont(Theme.FONT_TITLE);
        labelTitulo.setForeground(Theme.TEXT_PRIMARY);
        panelTitulo.add(iconWorldCup);
        panelTitulo.add(labelTitulo);
        panelSuperior.add(panelTitulo, BorderLayout.WEST);

        // Panel de Pestañas (Centro)
        JPanel panelPestañas = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panelPestañas.setOpaque(false);

        btnPestañaTengo = crearBotonPestaña("VISTA TENGO (LATE)", true);
        btnPestañaRepetidas = crearBotonPestaña("VISTA REPETIDAS (REPE)", false);
        
        panelPestañas.add(btnPestañaTengo);
        panelPestañas.add(btnPestañaRepetidas);
        panelSuperior.add(panelPestañas, BorderLayout.CENTER);

        // Panel de Acciones (Derecha: Modo Toggle + Exportar)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelAcciones.setOpaque(false);

        btnToggleModo = new JButton("Modo: Agregar");
        btnToggleModo.setFont(Theme.FONT_BUTTON);
        btnToggleModo.setFocusPainted(false);
        btnToggleModo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggleModo.setBackground(Theme.BG_PANEL);
        btnToggleModo.setForeground(Theme.COLOR_LATE);
        btnToggleModo.setBorder(BorderFactory.createLineBorder(Theme.COLOR_LATE, 1));
        btnToggleModo.setPreferredSize(new Dimension(160, 36));
        btnToggleModo.addActionListener(e -> alternarModoGrilla());

        JButton btnExportar = new JButton("Exportar");
        btnExportar.setFont(Theme.FONT_BUTTON);
        btnExportar.setFocusPainted(false);
        btnExportar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExportar.setBackground(Theme.ACCENT_VIOLET);
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnExportar.setPreferredSize(new Dimension(110, 36));
        btnExportar.addActionListener(e -> {
            JPopupMenu menuExportar = new JPopupMenu();
            
            JMenuItem itemTengo = new JMenuItem("Exportar TENGO");
            itemTengo.setFont(Theme.FONT_BUTTON);
            itemTengo.addActionListener(ev -> exportarAlPortapapeles("TENGO"));
            
            JMenuItem itemRepe = new JMenuItem("Exportar REPETIDAS");
            itemRepe.setFont(Theme.FONT_BUTTON);
            itemRepe.addActionListener(ev -> exportarAlPortapapeles("REPETIDAS"));
            
            JMenuItem itemNola = new JMenuItem("Exportar NOLA (Faltantes)");
            itemNola.setFont(Theme.FONT_BUTTON);
            itemNola.addActionListener(ev -> exportarAlPortapapeles("NOLA"));
            
            menuExportar.add(itemTengo);
            menuExportar.add(itemRepe);
            menuExportar.add(itemNola);
            
            menuExportar.show(btnExportar, 0, btnExportar.getHeight());
        });

        panelAcciones.add(btnToggleModo);
        panelAcciones.add(btnExportar);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);

        // --- 2. Panel Inferior (Dashboard de Estadísticas) ---
        JPanel panelDashboard = new JPanel(new BorderLayout(20, 0));
        panelDashboard.setBackground(Theme.BG_HEADER);
        panelDashboard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));

        // Progreso de Figuritas
        JPanel panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panelEstadisticas.setOpaque(false);

        labelProgresoTengo = new JLabel("Tengo: 0 / 979 (0.0%)");
        labelProgresoTengo.setFont(Theme.FONT_COUNTRY_CODE);
        labelProgresoTengo.setForeground(Theme.TEXT_SECONDARY);

        labelProgresoRepes = new JLabel("Repetidas: 0");
        labelProgresoRepes.setFont(Theme.FONT_COUNTRY_CODE);
        labelProgresoRepes.setForeground(Theme.TEXT_SECONDARY);

        panelEstadisticas.add(labelProgresoTengo);
        panelEstadisticas.add(labelProgresoRepes);
        panelDashboard.add(panelEstadisticas, BorderLayout.WEST);

        // Barra de Progreso Visual
        progressBarTengo = new JProgressBar(0, 979);
        progressBarTengo.setPreferredSize(new Dimension(300, 10));
        progressBarTengo.setForeground(Theme.COLOR_LATE);
        progressBarTengo.setBackground(Theme.COLOR_NOLA);
        progressBarTengo.setBorderPainted(false);
        panelDashboard.add(progressBarTengo, BorderLayout.EAST);

        add(panelDashboard, BorderLayout.SOUTH);

        // --- 3. Panel Central (Grilla Scrolleable) ---
        gridContainer = new JPanel();
        gridContainer.setLayout(new BoxLayout(gridContainer, BoxLayout.Y_AXIS));
        gridContainer.setBackground(Theme.BG_DARK);
        gridContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Rellenar la grilla con todos los grupos y países
        construirGrillaCompleta();

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20); // Scroll ultra fluido
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.setBackground(Theme.BG_DARK);
        scrollPane.getViewport().setBackground(Theme.BG_DARK);

        add(scrollPane, BorderLayout.CENTER);

        // Cargar estadísticas iniciales
        actualizarEstadisticas();
        actualizarVisualPestañas();
    }

    /**
     * Construye las filas agrupadas por grupos en la grilla.
     */
    private void construirGrillaCompleta() {
        gridContainer.removeAll();

        for (Map.Entry<String, String[]> entry : Album.GRUPOS.entrySet()) {
            String grupoNombre = entry.getKey();
            String[] paises = entry.getValue();

            // Cabecera del Grupo
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 8, 10));

            JLabel labelGrupo = new JLabel(grupoNombre.toUpperCase());
            labelGrupo.setFont(Theme.FONT_GROUP_HEADER);
            // Colores especiales de acento para la cabecera
            if (grupoNombre.equals("Especiales")) {
                labelGrupo.setForeground(Theme.COLOR_REPE); // Dorado para Especiales
            } else {
                labelGrupo.setForeground(Theme.ACCENT_VIOLET);
            }
            headerPanel.add(labelGrupo, BorderLayout.WEST);

            // Línea separadora horizontal estética
            JSeparator separator = new JSeparator();
            separator.setForeground(Theme.BORDER);
            separator.setBackground(Theme.BORDER);
            headerPanel.add(separator, BorderLayout.CENTER);
            
            // Espaciador entre texto de grupo y la línea
            labelGrupo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

            gridContainer.add(headerPanel);

            // Contenedor de las filas de países del grupo
            JPanel groupRowsPanel = new JPanel();
            groupRowsPanel.setLayout(new BoxLayout(groupRowsPanel, BoxLayout.Y_AXIS));
            groupRowsPanel.setBackground(Theme.BG_PANEL);
            // Bordes redondeados y borde fino para el panel del grupo
            groupRowsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(5, 0, 5, 0)
            ));

            for (String pais : paises) {
                LinkedList<Figurita> figs = album.getFiguritasDelPais(pais);
                CountryRow row = new CountryRow(pais, figs, flagManager, this);
                groupRowsPanel.add(row);
            }

            gridContainer.add(groupRowsPanel);
            gridContainer.add(Box.createVerticalStrut(10)); // Espacio entre grupos
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    /**
     * Crea un botón de pestaña superior totalmente personalizado.
     */
    private JButton crearBotonPestaña(String texto, boolean esTengo) {
        JButton btn = new JButton(texto);
        btn.setFont(Theme.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 36));
        btn.addActionListener(e -> cambiarVista(esTengo));
        return btn;
    }

    /**
     * Alterna la vista principal entre TENGO y REPETIDAS.
     */
    private void cambiarVista(boolean mostrarTengo) {
        if (this.vistaTengoActiva == mostrarTengo) return;
        this.vistaTengoActiva = mostrarTengo;

        // Actualizar visualmente la barra de navegación y los botones
        actualizarVisualPestañas();
        
        // Resetear el botón de modo a "Agregar" por defecto para evitar accidentes de borrado masivo
        modoAgregarActivo = true;
        actualizarVisualModo();

        // Repintar toda la grilla para refrescar los estados (muy rápido)
        gridContainer.repaint();
    }

    /**
     * Alterna el modo interactivo de la grilla entre Agregar y Quitar.
     */
    private void alternarModoGrilla() {
        modoAgregarActivo = !modoAgregarActivo;
        actualizarVisualModo();
        gridContainer.repaint(); // Fuerza a las celdas a rehacer el cursor y hover
    }

    /**
     * Actualiza la apariencia del botón del modo interactivo según el estado actual.
     */
    private void actualizarVisualModo() {
        if (modoAgregarActivo) {
            btnToggleModo.setText("Modo: Agregar");
            if (vistaTengoActiva) {
                btnToggleModo.setForeground(Theme.COLOR_LATE);
                btnToggleModo.setBorder(BorderFactory.createLineBorder(Theme.COLOR_LATE, 1));
            } else {
                btnToggleModo.setForeground(Theme.COLOR_REPE);
                btnToggleModo.setBorder(BorderFactory.createLineBorder(Theme.COLOR_REPE, 1));
            }
        } else {
            btnToggleModo.setText("Modo: Quitar");
            btnToggleModo.setForeground(Theme.HOVER_REMOVE);
            btnToggleModo.setBorder(BorderFactory.createLineBorder(Theme.HOVER_REMOVE, 1));
        }
    }

    /**
     * Actualiza el diseño visual de las pestañas superiores.
     */
    private void actualizarVisualPestañas() {
        if (vistaTengoActiva) {
            btnPestañaTengo.setBackground(Theme.COLOR_LATE);
            btnPestañaTengo.setForeground(Color.WHITE);
            btnPestañaTengo.setBorder(BorderFactory.createEmptyBorder());

            btnPestañaRepetidas.setBackground(Theme.BG_PANEL);
            btnPestañaRepetidas.setForeground(Theme.TEXT_SECONDARY);
            btnPestañaRepetidas.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        } else {
            btnPestañaTengo.setBackground(Theme.BG_PANEL);
            btnPestañaTengo.setForeground(Theme.TEXT_SECONDARY);
            btnPestañaTengo.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));

            btnPestañaRepetidas.setBackground(Theme.COLOR_REPE);
            btnPestañaRepetidas.setForeground(Color.WHITE);
            btnPestañaRepetidas.setBorder(BorderFactory.createEmptyBorder());
        }
        actualizarVisualModo();
    }

    /**
     * Recalcula y actualiza la información en la barra de estadísticas.
     */
    public void actualizarEstadisticas() {
        int totalTengo = 0;
        int totalRepes = 0;

        for (LinkedList<Figurita> figs : album.getPaisesFiguritas().values()) {
            for (Figurita f : figs) {
                int qty = f.getQty();
                if (qty >= 1) {
                    totalTengo++;
                    if (qty > 1) {
                        totalRepes += (qty - 1);
                    }
                }
            }
        }

        double porcentaje = (totalTengo / 979.0) * 100.0;
        labelProgresoTengo.setText(String.format("Tengo: %d / 979 (%.1f%%)", totalTengo, porcentaje));
        labelProgresoRepes.setText("Repetidas: " + totalRepes);
        progressBarTengo.setValue(totalTengo);
    }

    /**
     * Forzar el repintado de las grillas y estadísticas de progreso al realizar un click.
     */
    public void refrescarGrillas() {
        actualizarEstadisticas();
        gridContainer.repaint();
    }

    /**
     * Exporta el contenido filtrado por el tipo indicado al portapapeles en el formato plano exacto requerido.
     * 
     * @param tipo El tipo de exportación ("TENGO", "REPETIDAS", o "NOLA").
     */
    private void exportarAlPortapapeles(String tipo) {
        StringBuilder sb = new StringBuilder();

        sb.append(tipo).append(":\n");

        boolean hayFigus = false;

        // Iterar en el orden estricto del álbum
        for (Map.Entry<String, LinkedList<Figurita>> entry : album.getPaisesFiguritas().entrySet()) {
            String pais = entry.getKey();
            LinkedList<Figurita> figuritas = entry.getValue();

            // Filtrar las figuritas que corresponden a este estado
            java.util.List<Figurita> filteredFigs = figuritas.stream()
                    .filter(f -> {
                        int qty = f.getQty();
                        if (tipo.equals("TENGO")) {
                            return qty >= 1;
                        } else if (tipo.equals("REPETIDAS")) {
                            return qty > 1;
                        } else {
                            return qty == 0;
                        }
                    })
                    .sorted(java.util.Comparator.comparingInt(Figurita::getNumero))
                    .collect(Collectors.toList());

            if (!filteredFigs.isEmpty()) {
                hayFigus = true;
                String emoji = flagManager.getFlagEmoji(pais);
                sb.append(emoji).append(" ").append(pais).append(": ");
                for (int i = 0; i < filteredFigs.size(); i++) {
                    Figurita f = filteredFigs.get(i);
                    sb.append(f.getNumero());
                    if (tipo.equals("REPETIDAS") && f.getQty() > 2) {
                        sb.append(" (x").append(f.getQty() - 1).append(")");
                    }
                    if (i < filteredFigs.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("\n");
            }
        }

        if (!hayFigus) {
            sb.append("(Ninguna figurita en este estado todavía)\n");
        }

        // Copiar al portapapeles
        try {
            StringSelection selection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);

            // Toast feedback popup premium
            JOptionPane.showMessageDialog(this,
                    "¡Listado de " + tipo + " exportado al portapapeles con éxito!",
                    "Exportar",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al exportar al portapapeles: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
