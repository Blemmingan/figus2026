package com.figus2026.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestiona el mapeo de códigos de países a códigos ISO, descarga las banderas
 * de forma asíncrona desde FlagCDN y las almacena en un caché local en disco para un acceso instantáneo.
 */
public class FlagManager {

    private static final String CACHE_DIR = System.getProperty("user.home") + "/.figus2026/flags";
    private static final String FLAG_URL_TEMPLATE = "https://flagcdn.com/w80/%s.png"; // Usamos tamaño w80 para que se vea nítido al escalar

    // Mapeo estático de códigos del álbum a códigos ISO 3166-1 alpha-2 aceptados por FlagCDN
    private static final Map<String, String> CODIGOS_ISO = new HashMap<>();
    static {
        CODIGOS_ISO.put("MEX", "mx");
        CODIGOS_ISO.put("RSA", "za");
        CODIGOS_ISO.put("KOR", "kr");
        CODIGOS_ISO.put("CZE", "cz");
        CODIGOS_ISO.put("CAN", "ca");
        CODIGOS_ISO.put("BIH", "ba");
        CODIGOS_ISO.put("QAT", "qa");
        CODIGOS_ISO.put("SUI", "ch");
        CODIGOS_ISO.put("BRA", "br");
        CODIGOS_ISO.put("MAR", "ma");
        CODIGOS_ISO.put("HAI", "ht");
        CODIGOS_ISO.put("SCO", "gb-sct"); // Escocia
        CODIGOS_ISO.put("USA", "us");
        CODIGOS_ISO.put("PAR", "py");
        CODIGOS_ISO.put("AUS", "au");
        CODIGOS_ISO.put("TUR", "tr");
        CODIGOS_ISO.put("GER", "de");
        CODIGOS_ISO.put("CUW", "cw"); // Curaçao
        CODIGOS_ISO.put("CIV", "ci"); // Costa de Marfil
        CODIGOS_ISO.put("ECU", "ec");
        CODIGOS_ISO.put("NED", "nl");
        CODIGOS_ISO.put("JPN", "jp");
        CODIGOS_ISO.put("SWE", "se");
        CODIGOS_ISO.put("TUN", "tn");
        CODIGOS_ISO.put("BEL", "be");
        CODIGOS_ISO.put("EGY", "eg");
        CODIGOS_ISO.put("IRN", "ir");
        CODIGOS_ISO.put("NZL", "nz");
        CODIGOS_ISO.put("ESP", "es");
        CODIGOS_ISO.put("CPV", "cv"); // Cabo Verde
        CODIGOS_ISO.put("KSA", "sa"); // Arabia Saudita
        CODIGOS_ISO.put("URU", "uy");
        CODIGOS_ISO.put("FRA", "fr");
        CODIGOS_ISO.put("SEN", "sn");
        CODIGOS_ISO.put("IRQ", "iq");
        CODIGOS_ISO.put("NOR", "no");
        CODIGOS_ISO.put("ARG", "ar");
        CODIGOS_ISO.put("ALG", "dz"); // Argelia
        CODIGOS_ISO.put("AUT", "at");
        CODIGOS_ISO.put("JOR", "jo");
        CODIGOS_ISO.put("POR", "pt");
        CODIGOS_ISO.put("COD", "cd"); // R. D. Congo
        CODIGOS_ISO.put("UZB", "uz");
        CODIGOS_ISO.put("COL", "co");
        CODIGOS_ISO.put("ENG", "gb-eng"); // Inglaterra
        CODIGOS_ISO.put("CRO", "hr");
        CODIGOS_ISO.put("GHA", "gh");
        CODIGOS_ISO.put("PAN", "pa");
    }

    // Pool de hilos para descargas en paralelo
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // Caché en memoria para evitar leer constantemente de disco
    private final ConcurrentHashMap<String, ImageIcon> cacheMemoria = new ConcurrentHashMap<>();

    // Imagen especial para FWC (Copa del Mundo)
    private ImageIcon fwcIconCached;

    public FlagManager() {
        // Asegurar que exista la carpeta de caché
        File dir = new File(CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Obtiene de forma asíncrona la bandera de un país, escalada a las dimensiones especificadas.
     * Si no está en caché, inicia la descarga y ejecuta el callback runnable para repintar la UI al finalizar.
     *
     * @param codigoPais Código de 3 letras del álbum (e.g. "ARG").
     * @param width Ancho deseado de la bandera.
     * @param height Alto deseado de la bandera.
     * @param onLoaded Callback que se ejecutará en el Event Dispatch Thread de Swing al completarse la descarga.
     * @return El ImageIcon de la bandera (puede ser un placeholder si se está descargando).
     */
    public ImageIcon getFlagIcon(String codigoPais, int width, int height, Runnable onLoaded) {
        if ("FWC".equals(codigoPais)) {
            return getFwcIcon(width, height);
        }

        String cacheKey = codigoPais + "_" + width + "x" + height;
        if (cacheMemoria.containsKey(cacheKey)) {
            return cacheMemoria.get(cacheKey);
        }

        // Intentar cargar desde el disco
        File localFile = new File(CACHE_DIR, codigoPais.toLowerCase() + ".png");
        if (localFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(localFile);
                ImageIcon scaledIcon = createScaledRoundedIcon(img, width, height);
                cacheMemoria.put(cacheKey, scaledIcon);
                return scaledIcon;
            } catch (IOException e) {
                System.err.println("Error al leer bandera desde caché local: " + codigoPais);
            }
        }

        // Si no está local, creamos un placeholder atractivo e iniciamos la descarga asíncrona
        ImageIcon placeholder = createPlaceholderIcon(codigoPais, width, height);
        cacheMemoria.put(cacheKey, placeholder);

        String isoCode = CODIGOS_ISO.get(codigoPais);
        if (isoCode != null) {
            executorService.submit(() -> {
                try {
                    String urlStr = String.format(FLAG_URL_TEMPLATE, isoCode);
                    URL url = new URL(urlStr);
                    BufferedImage downloadedImg = ImageIO.read(url);
                    if (downloadedImg != null) {
                        // Guardar en disco
                        ImageIO.write(downloadedImg, "png", localFile);

                        // Crear icono escalado y guardar en memoria
                        ImageIcon finalIcon = createScaledRoundedIcon(downloadedImg, width, height);
                        cacheMemoria.put(cacheKey, finalIcon);

                        // Ejecutar callback de repintado en el EDT de Swing
                        SwingUtilities.invokeLater(onLoaded);
                    }
                } catch (Exception e) {
                    System.err.println("Error al descargar bandera para " + codigoPais + " (ISO: " + isoCode + "): " + e.getMessage());
                }
            });
        }

        return placeholder;
    }

    /**
     * Crea un ícono con bordes redondeados y escalado de alta calidad.
     */
    private ImageIcon createScaledRoundedIcon(BufferedImage src, int width, int height) {
        BufferedImage rounded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rounded.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Clip redondeado
        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, 8, 8));
        g2.drawImage(src, 0, 0, width, height, null);

        // Borde fino estético
        g2.setClip(null);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);

        g2.dispose();
        return new ImageIcon(rounded);
    }

    /**
     * Genera un placeholder minimalista y moderno con las siglas del país si la bandera aún no se cargó.
     */
    private ImageIcon createPlaceholderIcon(String codigoPais, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo con un degradado sutil oscuro
        GradientPaint gp = new GradientPaint(0, 0, new Color(45, 52, 54), width, height, new Color(9, 132, 227));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, width, height, 8, 8);

        // Borde fino
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);

        // Siglas del país
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("sansserif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String text = codigoPais.substring(0, Math.min(codigoPais.length(), 2));
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);

        g2.dispose();
        return new ImageIcon(img);
    }

    /**
     * Dibuja un hermoso ícono de trofeo dorado de la copa del mundo para el bloque FWC.
     */
    private synchronized ImageIcon getFwcIcon(int width, int height) {
        if (fwcIconCached != null && fwcIconCached.getIconWidth() == width && fwcIconCached.getIconHeight() == height) {
            return fwcIconCached;
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo con degradado oscuro de copa del mundo
        GradientPaint bgGp = new GradientPaint(0, 0, new Color(24, 28, 36), width, height, new Color(13, 16, 20));
        g2.setPaint(bgGp);
        g2.fillRoundRect(0, 0, width, height, 8, 8);

        // Borde fino
        g2.setColor(new Color(255, 215, 0, 80)); // Borde dorado tenue
        g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);

        // Dibujar Trofeo Dorado Vectorial
        g2.translate(width / 2.0, height / 2.0);
        double scale = Math.min(width, height) / 36.0;
        g2.scale(scale, scale);

        // Colores dorados
        Color goldDark = new Color(212, 175, 55);
        Color goldLight = new Color(255, 223, 0);
        Color goldBright = new Color(255, 245, 150);

        // Base del trofeo (2 rectángulos superpuestos)
        g2.setColor(goldDark);
        g2.fillRoundRect(-10, 8, 20, 4, 2, 2);
        g2.setColor(goldLight);
        g2.fillRoundRect(-7, 4, 14, 4, 1, 1);

        // Tallo/Cuerpo curvado
        GeneralPath stem = new GeneralPath();
        stem.moveTo(-3, 4);
        stem.quadTo(-1, 0, -4, -6);
        stem.lineTo(4, -6);
        stem.quadTo(1, 0, 3, 4);
        stem.closePath();
        g2.setColor(goldLight);
        g2.fill(stem);

        // Globo / Copa arriba
        g2.setColor(goldLight);
        g2.fillOval(-8, -14, 16, 10);

        // Anillo y detalles del globo
        g2.setColor(goldBright);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawArc(-8, -12, 16, 6, 0, -180);

        // Asas/Orejas de la copa
        GeneralPath leftEar = new GeneralPath();
        leftEar.moveTo(-4, -6);
        leftEar.quadTo(-10, -8, -6, -12);
        leftEar.quadTo(-4, -10, -4, -6);
        g2.fill(leftEar);

        GeneralPath rightEar = new GeneralPath();
        rightEar.moveTo(4, -6);
        rightEar.quadTo(10, -8, 6, -12);
        rightEar.quadTo(4, -10, 4, -6);
        g2.fill(rightEar);

        // Brillo central vertical
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(-1, -12, 3, 6);

        g2.dispose();
        fwcIconCached = new ImageIcon(img);
        return fwcIconCached;
    }

    /**
     * Obtiene el emoji de la bandera para un código de país.
     *
     * @param codigoPais Código de 3 letras del álbum.
     * @return String con el emoji de la bandera o un placeholder.
     */
    public String getFlagEmoji(String codigoPais) {
        if ("FWC".equals(codigoPais)) {
            return "🏆";
        }
        
        String iso = CODIGOS_ISO.get(codigoPais);
        if (iso == null || iso.length() < 2) {
            return "🏳️"; // Default flag
        }
        
        if (iso.equals("gb-sct")) return "🏴󠁧󠁢󠁳󠁣󠁴󠁿";
        if (iso.equals("gb-eng")) return "🏴󠁧󠁢󠁥󠁮󠁧󠁿";
        
        int firstChar = Character.toUpperCase(iso.charAt(0)) - 'A' + 0x1F1E6;
        int secondChar = Character.toUpperCase(iso.charAt(1)) - 'A' + 0x1F1E6;
        
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    /**
     * Apaga ordenadamente el pool de descargas.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
