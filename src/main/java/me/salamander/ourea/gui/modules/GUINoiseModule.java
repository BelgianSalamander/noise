package me.salamander.ourea.gui.modules;

import me.salamander.ourea.gui.ModulePanel;
import me.salamander.ourea.modules.NoiseSampler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GUINoiseModule extends JPanel {
    private static final int TITLE_HEIGHT = 40;
    private static final int INPUT_HEIGHT = 20;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int DEFAULT_WIDTH = 100;
    private static final int CONNECTION_SIZE = 10;

    private ModulePanel parent;

    private final GUINoiseModuleType<?> type;
    private final NoiseSampler sampler;
    private final int height;
    private int width;

    private JLabel titleLabel;

    private int screenX, screenY, myX, myY; //For dragging
    private boolean dragging;

    public GUINoiseModule(GUINoiseModuleType<?> type, NoiseSampler sampler) {
        this.type = type;
        this.sampler = sampler;
        this.height = TITLE_HEIGHT + INPUT_HEIGHT * type.getInputs().length;

        setLayout(null);
        setBackground(Color.GRAY);

        createTitle();
        addDragging();

        createInputs();

        setSize(width, height);
        System.out.println("Created Noise Module: " + type.getName());
    }

    public void setModulePanel(ModulePanel parent) {
        this.parent = parent;
    }

    private void createTitle() {
        titleLabel = new JLabel(type.getName());
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), titleLabel.getFont().getStyle(), TITLE_FONT_SIZE));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel);
        width = Math.max(titleLabel.getPreferredSize().width + 20, DEFAULT_WIDTH);
        titleLabel.setBounds(0, 0, width, TITLE_HEIGHT);
    }

    private void addDragging(){
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;

                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();
                myX = getX();
                myY = getY();

                parent.select(GUINoiseModule.this);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Color.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.GRAY);
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(!dragging){
                    return;
                }
                getParent().repaint();

                int deltaX = e.getXOnScreen() - screenX;
                int deltaY = e.getYOnScreen() - screenY;

                if(deltaX != 0 || deltaY != 0){
                    int newX = myX + deltaX;
                    int newY = myY + deltaY;
                    setLocation(clampX(newX), clampY(newY));
                }
            }

            private int clampX(int n){
                int max = getParent().getWidth() - getWidth();

                if(n < 0) return 0;
                else if(n > max) return max;
                return n;
            }
            private int clampY(int n){
                int max = getParent().getHeight() - getHeight();

                if(n < 0) return 0;
                else if(n > max) return max;
                return n;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                ((ModulePanel) getParent()).mouseMovement(e);
            }
        });
    }

    private void createInputs(){
        for (int i = 0; i < type.getInputs().length; i++) {
            Input<?> input = type.getInputs()[i];
            JLabel label = new JLabel(input.getName());
            label.setBounds(10, TITLE_HEIGHT + i * INPUT_HEIGHT, width, INPUT_HEIGHT);
            label.setHorizontalAlignment(JLabel.LEFT);
            label.setVerticalAlignment(JLabel.CENTER);
            add(label);
        }
    }

    public GUINoiseModuleType<?> getType() {
        return type;
    }

    public NoiseSampler getSampler() {
        return sampler;
    }
}
