package com.mycompany.dibuixets;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class OpenCVDrawingApp extends JPanel {
    private Mat image, background;
    private BufferedImage bufferedImage;
    private java.awt.Point startPoint, endPoint;
    private Color currentColor = Color.RED;
    private int thickness = 2;
    private boolean eraserMode = false;
    private boolean drawingRectangle = false;
    private boolean drawingCircle = false;
    private Stack<Mat> undoStack = new Stack<>();
    private Stack<Mat> redoStack = new Stack<>();
    private JButton eraserButton;

    public OpenCVDrawingApp(String imagePath, JButton eraserButton) {
        System.load("C:\\GS2\\2n any\\DVI\\Tema 4\\OpenCV\\opencv\\build\\java\\x64\\opencv_java490.dll");
        this.eraserButton = eraserButton;
        image = Imgcodecs.imread(imagePath);
        background = image.clone();
        bufferedImage = matToBufferedImage(image);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                saveState();
            }

            public void mouseReleased(MouseEvent e) {
                if (drawingRectangle) {
                    drawRectangle();
                } else if (drawingCircle) {
                    drawCircle();
                }
                startPoint = null;
                endPoint = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                if (!drawingRectangle && !drawingCircle) {
                    Scalar color = eraserMode ? getBackgroundColor(startPoint) : new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed());
                    Imgproc.line(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                            new org.opencv.core.Point(e.getX(), e.getY()), color, thickness);
                    startPoint = e.getPoint();
                }
                bufferedImage = matToBufferedImage(image);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bufferedImage, 0, 0, this);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * (int) mat.elemSize()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    private Scalar getBackgroundColor(java.awt.Point point) {
        double[] bgColor = background.get(point.y, point.x);
        return new Scalar(bgColor);
    }

    private void saveState() {
        undoStack.push(image.clone());
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(image.clone());
            image = undoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(image.clone());
            image = redoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public void toggleEraser() {
        eraserMode = !eraserMode;
        eraserButton.setText(eraserMode ? "Erasing" : "Drawing");
    }

    public void toggleRectangleMode() {
        drawingRectangle = !drawingRectangle;
        drawingCircle = false;
    }

    public void toggleCircleMode() {
        drawingCircle = !drawingCircle;
        drawingRectangle = false;
    }

    private void drawRectangle() {
        if (startPoint != null && endPoint != null) {
            Imgproc.rectangle(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                    new org.opencv.core.Point(endPoint.x, endPoint.y),
                    new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()), thickness);
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    private void drawCircle() {
        if (startPoint != null && endPoint != null) {
            int radius = (int) Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));
            Imgproc.circle(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                    radius, new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()), thickness);
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public static void main(String[] args) {
        String imagePath = "images\\moon.jpg";
        JFrame frame = new JFrame("OpenCV Drawing App");

        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton eraserButton = new JButton("Drawing");
        JButton rectButton = new JButton("Rectangle");
        JButton circleButton = new JButton("Circle");

        OpenCVDrawingApp panel = new OpenCVDrawingApp(imagePath, eraserButton);

        undoButton.addActionListener(e -> panel.undo());
        redoButton.addActionListener(e -> panel.redo());
        eraserButton.addActionListener(e -> panel.toggleEraser());
        rectButton.addActionListener(e -> panel.toggleRectangleMode());
        circleButton.addActionListener(e -> panel.toggleCircleMode());

        JPanel controls = new JPanel();
        controls.add(undoButton);
        controls.add(redoButton);
        controls.add(eraserButton);
        controls.add(rectButton);
        controls.add(circleButton);

        frame.add(controls, BorderLayout.NORTH);
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
