package com.mycompany.dibuixets;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public class ImageFinder extends JDialog {
    private File selectedFile = null;
    private boolean selected = false;
    private static final String imagesPath = "images";
    private JPanel thumbnailPanel;

    public ImageFinder(Frame owner) {
        super(owner, "Seleccionar Imagen, doble click para seleccionar", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new GridLayout(0, 3, 10, 10)); // Grid de 3 columnas

        // Obtener rutas de imágenes desde DataAccess
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

    private void loadThumbnails(List<File> imagePaths) {
        // Usamos SwingWorker para cargar las miniaturas en un hilo en segundo plano
        SwingWorker<Void, JPanel> worker = new SwingWorker<Void, JPanel>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (File file : imagePaths) {
                    JPanel thumbnailPanelItem = createThumbnail(file);
                    publish(thumbnailPanelItem); // Pasar cada panel al hilo de la interfaz gráfica
                }
                return null;
            }

            @Override
            protected void process(List<JPanel> chunks) {
                for (JPanel panel : chunks) {
                    thumbnailPanel.add(panel); // Añadir los paneles creados en el hilo principal
                }
                thumbnailPanel.revalidate(); // Actualizar la interfaz
                thumbnailPanel.repaint();
            }

            @Override
            protected void done() {
                // No es necesario hacer nada cuando termine
            }
        };

        worker.execute(); // Ejecutar el hilo en segundo plano
    }

    private JPanel createThumbnail(File file) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Miniatura de imagen
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        Image image = icon.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        panel.add(imageLabel, BorderLayout.CENTER);

        // Nombre del archivo
        JLabel nameLabel = new JLabel(file.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(nameLabel, BorderLayout.SOUTH);

        // Click para seleccionar
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedFile != null && !selectedFile.equals(file)) {
                    selected = false; // Reiniciar la selección si cambias de archivo
                }

                selectedFile = file; // Guardar archivo seleccionado
                highlightSelection(panel, (JPanel) panel.getParent()); // Resaltar panel seleccionado

                if(selected) {
                    dispose(); // Cerrar el diálogo
                }
                selected = true;
            }
        });

        return panel;
    }

    private void highlightSelection(JPanel selectedPanel, JPanel thumbnailPanel) {
        // Restaurar fondo de todos los paneles
        for (Component comp : thumbnailPanel.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(null); // Restaurar fondo a su color original
            }
        }
        // Resaltar el fondo del panel seleccionado
        selectedPanel.setBackground(Color.CYAN); // Cambiar el fondo al color deseado
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    // Método estático para mostrar el diálogo
    public static File showDialog(Frame owner) {
        ImageFinder sercher = new ImageFinder(owner);
        sercher.setVisible(true);
        return sercher.getSelectedFile();
    }

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
