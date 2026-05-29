package com.figus2026;

import com.figus2026.model.Album;
import com.figus2026.view.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

/**
 * Punto de entrada principal de la aplicación.
 * Inicializa el Look and Feel moderno de FlatLaf y lanza la interfaz de usuario en el hilo correcto de Swing.
 */
public class Main {
    public static void main(String[] args) {
        // Inicializar el Look & Feel FlatLaf Dark para una base oscura consistente y moderna
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Personalizaciones adicionales del look and feel
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ProgressBar.arc", 999);
        } catch (Exception e) {
            System.err.println("No se pudo inicializar FlatLaf L&F, se usará el predeterminado del sistema.");
        }

        // Ejecutar en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar el modelo del álbum (carga automáticamente el estado previo si existe)
                Album album = new Album();

                // Crear y mostrar la ventana principal
                MainFrame frame = new MainFrame(album);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                        "Error fatal al inicializar la aplicación:\n" + e.getMessage(), 
                        "Error Crítico", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
