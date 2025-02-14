/**
 * La clase Preferences gestiona la configuración de la ruta de la biblioteca OpenCV.
 * Se encarga de leer la ruta desde un archivo de preferencias y, si no existe o es inválida,
 * solicita al usuario que seleccione una nueva ubicación.
 */
package com.mycompany.dibuixets;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preferences {
    /**
     * Obtiene la ruta de la biblioteca OpenCV desde un archivo de preferencias.
     * Si la ruta no existe o no es válida, solicita al usuario que seleccione una nueva ubicación.
     *
     * @return La ruta del archivo OpenCV o null si no se selecciona una nueva ruta válida.
     */
    public static String getOpenCVPath() {
        File preferenciasFolder = new File("data");
        if (!preferenciasFolder.exists()) {
            preferenciasFolder.mkdir();
        }
        File preferenciasFile = new File("data/preferencias.txt");
        HashMap<String, String> preferencias = new HashMap<>();

        if (preferenciasFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(preferenciasFile))) {
                while (br.ready()) {
                    String line = br.readLine();
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        preferencias.put(parts[0], parts[1]);
                    }
                }
                if (preferencias.containsKey("opencv") && preferencias.get("opencv").endsWith("opencv_java490.dll")) {
                    try {
                        File file = new File(preferencias.get("opencv"));
                        return preferencias.get("opencv");
                    } catch (Exception e) {
                        getNewRoute(preferenciasFile);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return getNewRoute(preferenciasFile);
    }
    
    /**
     * Solicita al usuario que seleccione manualmente la ruta de la biblioteca OpenCV.
     * Si el usuario selecciona una ruta válida, se guarda en el archivo de preferencias.
     *
     * @param preferenciasFile Archivo donde se guardarán las preferencias.
     * @return La nueva ruta seleccionada o null si no se selecciona ninguna.
     */
    private static String getNewRoute(File preferenciasFile) {
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(dialog, "No se ha encontrado la ruta de OpenCV o no es válida. Por favor, seleccione la carpeta de OpenCV.", "Error", JOptionPane.ERROR_MESSAGE);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona la carpeta de OpenCV");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String opencvPath = selectedFile.getAbsolutePath();
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(preferenciasFile, true))) {
                bw.write("opencv," + opencvPath);
                bw.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return opencvPath;
        }
        return null;
    }
}
