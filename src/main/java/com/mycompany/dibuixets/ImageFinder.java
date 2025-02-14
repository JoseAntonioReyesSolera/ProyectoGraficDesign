package com.mycompany.dibuixets;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Clase ImageFinder que muestra un cuadro de diálogo para seleccionar imágenes
 * desde un directorio específico.
 */
public class ImageFinder extends JDialog {
    private File selectedFile = null;
    private boolean selected = false;
    private static final String imagesPath = "images";
    private JPanel thumbnailPanel;

    /**
     * Constructor de ImageFinder.
     * @param owner La ventana propietaria del diálogo.
     */
    public ImageFinder(Frame owner) {
        super(owner, "Seleccionar Imagen, doble click para seleccionar", true);
        initComponents();
    }

    /**
     * Inicializa los componentes del cuadro de diálogo.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new GridLayout(0, 3, 10, 10)); // Grid de 3 columnas

        // Obtener rutas de imágenes desde el directorio especificado
        List<File> imagePaths = getImageFiles(imagesPath);
        
        if (imagePaths.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron imágenes.", "Información", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Cargar imágenes de manera asíncrona
            loadThumbnails(imagePaths);
        }

        JScrollPane scrollPane = new JScrollPane(thumbnailPanel);
        add(scrollPane, BorderLayout.CENTER);

        setSize(700, 400);
        setLocationRelativeTo(getOwner());
    }

    /**
     * Carga miniaturas de las imágenes en un hilo en segundo plano.
     * @param imagePaths Lista de archivos de imágenes.
     */
    private void loadThumbnails(List<File> imagePaths) {
        SwingWorker<Void, JPanel> worker = new SwingWorker<Void, JPanel>() {
            @Override
            protected Void doInBackground() {
                for (File file : imagePaths) {
                    JPanel thumbnailPanelItem = createThumbnail(file);
                    publish(thumbnailPanelItem);
                }
                return null;
            }

            @Override
            protected void process(List<JPanel> chunks) {
                for (JPanel panel : chunks) {
                    thumbnailPanel.add(panel);
                }
                thumbnailPanel.revalidate();
                thumbnailPanel.repaint();
            }
        };

        worker.execute();
    }

    /**
     * Crea un panel con la miniatura de una imagen.
     * @param file Archivo de imagen.
     * @return JPanel con la miniatura y el nombre del archivo.
     */
    private JPanel createThumbnail(File file) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        Image image = icon.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        panel.add(imageLabel, BorderLayout.CENTER);

        JLabel nameLabel = new JLabel(file.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(nameLabel, BorderLayout.SOUTH);

        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedFile != null && !selectedFile.equals(file)) {
                    selected = false;
                }

                selectedFile = file;
                highlightSelection(panel, (JPanel) panel.getParent());

                if (selected) {
                    dispose();
                }
                selected = true;
            }
        });

        return panel;
    }

    /**
     * Resalta la miniatura seleccionada cambiando su color de fondo.
     * @param selectedPanel Panel seleccionado.
     * @param thumbnailPanel Panel contenedor de miniaturas.
     */
    private void highlightSelection(JPanel selectedPanel, JPanel thumbnailPanel) {
        for (Component comp : thumbnailPanel.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(null);
            }
        }
        selectedPanel.setBackground(Color.CYAN);
    }

    /**
     * Obtiene el archivo de imagen seleccionado.
     * @return Archivo seleccionado o null si no se seleccionó ninguno.
     */
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Muestra el cuadro de diálogo para seleccionar una imagen.
     * @param owner Ventana propietaria del cuadro de diálogo.
     * @return Archivo seleccionado o null si no se seleccionó ninguno.
     */
    public static File showDialog(Frame owner) {
        ImageFinder finder = new ImageFinder(owner);
        finder.setVisible(true);
        return finder.getSelectedFile();
    }

    /**
     * Obtiene una lista de archivos de imagen desde un directorio especificado.
     * @param directoryPath Ruta del directorio a buscar.
     * @return Lista de archivos de imagen.
     */
    public static List<File> getImageFiles(String directoryPath) {
        File folder = new File(directoryPath);
        File[] files = null;
        
        if (folder.exists() && folder.isDirectory()) {
            files = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jpg") ||
                           name.toLowerCase().endsWith(".jpeg") ||
                           name.toLowerCase().endsWith(".png");
                }
            });
        }
        
        return Arrays.asList(files != null ? files : new File[0]);
    }
}
