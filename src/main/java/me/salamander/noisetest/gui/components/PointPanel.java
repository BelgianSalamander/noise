package me.salamander.noisetest.gui.components;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.color.SingleColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class PointPanel extends JPanel {
    private List<SlidingPoint> points = new ArrayList<>();

    public PointPanel(){
        setLayout(null);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "addPoint");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "deletePoint");

        getActionMap().put("addPoint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPoint();
            }
        });
        getActionMap().put("deletePoint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
    }

    public ColorSampler getColorSampler(){
        if(points.size() == 0) return ColorGradient.DEFAULT;
        if(points.size() == 1) return new SingleColor(points.get(0).color);

        ColorGradient gradient = new ColorGradient();

        for(SlidingPoint point : points){
            float n = ((point.getX() + point.getWidth() / 2) / (float) getWidth()) * 2 - 1;
            gradient.addColorPoint(n, point.color);
        }

        gradient.generate();
        return gradient;
    }

    private void addPoint(){
        SlidingPoint point = new SlidingPoint(this);
        point.setLocation(20, 20);
        points.add(point);
        add(point);
        repaint();
        updateGradient();
    }

    private void updateGradient(){
        GradientEditor parent = (GradientEditor) getParent();
        parent.setGradient(getColorSampler());
    }

    private void deleteSelected(){
        SlidingPoint point = ((GradientEditor) getParent()).selectedPoint;
        points.remove(point);
        remove(point);

        updateGradient();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        float quarter = getWidth() / 4.f;
        float x = quarter;

        for(int i = 0; i < 3; i++){
            g.drawLine((int) x, 0, (int) x, 20);
            x += quarter;
        }
    }

    static class SlidingPoint extends JComponent{
        private int previousX = 0;
        private int previousScreenX = 0;
        private Color color = Color.WHITE;

        public void setColor(Color color) {
            this.color = color;
        }

        private SlidingPoint(PointPanel container){
            setSize(20, 20);
            SlidingPoint actualThis = this;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    previousX = getX();
                    previousScreenX = e.getXOnScreen();

                    ((GradientEditor) container.getParent()).selectedPoint = actualThis;
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int deltaX = e.getXOnScreen() - previousScreenX;
                    int newX = previousX + deltaX;

                    if(newX < 0) newX = 0;
                    else if(newX > container.getWidth() - getWidth()) newX = container.getWidth() - getWidth();

                    setLocation(newX, getY());

                    container.updateGradient();
                }
            });


        }

        @Override
        public void paintComponent(Graphics g){
            g.setColor(color);
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.drawOval(0, 0, getWidth(), getHeight());
        }
    }
}
