package com.mycompany.dibuixets;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class OpenCVDrawing extends JPanel {
    private Mat image;
    private BufferedImage bufferedImage;
    private java.awt.Point lastPoint;
    private Color currentColor = Color.GRAY;
    private int brushSize = 2;
    private Stack<Mat> undoStack = new Stack<>();
    private Stack<Mat> redoStack = new Stack<>();

    public OpenCVDrawing(String imagePath) {
        System.load("C:\\Users\\Alumne\\Downloads\\opencv\\build\\java\\x64\\opencv_java490.dll");
        image = Imgcodecs.imread(imagePath);
        bufferedImage = matToBufferedImage(image);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                saveState(undoStack);
                redoStack.clear();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Scalar color = new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()); // Cambiar a BGR
                    Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                            new org.opencv.core.Point(e.getX(), e.getY()), color, brushSize);
                    lastPoint = e.getPoint();
                    bufferedImage = matToBufferedImage(image);
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bufferedImage, 0, 0, this);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
         Mat rgbMat = new Mat();
        Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_BGR2RGB); // Convertir de BGR a RGB

        int width = rgbMat.width();
        int height = rgbMat.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * (int) rgbMat.elemSize()];
        rgbMat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    public void setColor(Color color) {
        this.currentColor = color;
    }

    public void setBrushSize(int size) {
        this.brushSize = size;
    }

    public void saveImage(String path) {
        Imgcodecs.imwrite(path, image);
        JOptionPane.showMessageDialog(this, "Imatge desada com: " + path);
    }

    private void saveState(Stack<Mat> stack) {
        stack.push(image.clone());
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            saveState(redoStack);
            image = undoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            saveState(undoStack);
            image = redoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public static void main(String[] args) {
        String imagePath = "images/moon.jpg";
        JFrame frame = new JFrame("OpenCV Drawing App");
        OpenCVDrawing panel = new OpenCVDrawing(imagePath);
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        JButton colorButton = new JButton("Seleccionar Color");
        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Escull un color", panel.currentColor);
            if (chosenColor != null) {
                panel.setColor(chosenColor);
            }
        });

        JSlider brushSlider = new JSlider(1, 10, 2);
        brushSlider.addChangeListener(e -> panel.setBrushSize(brushSlider.getValue()));

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> panel.saveImage("images/saved_image.jpg"));
        
        JButton undoButton = new JButton("Desfer");
        undoButton.addActionListener(e -> panel.undo());
        
        JButton redoButton = new JButton("Refer");
        redoButton.addActionListener(e -> panel.redo());

        controlPanel.add(colorButton);
        controlPanel.add(new JLabel("Mida del pinzell:"));
        controlPanel.add(brushSlider);
        controlPanel.add(saveButton);
        controlPanel.add(undoButton);
        controlPanel.add(redoButton);
        
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}
