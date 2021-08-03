package me.salamander.noisetest.gui;

import me.salamander.noisetest.modules.NoiseModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Supplier;

public class GUINoiseModule extends JPanel {
    private final NoiseModule noiseModule;

    private JLabel title;
    private final String[] inputNames;
    private JLabel[] inputLabels;
    private final int height;

    private volatile int screenX = 0;
    private volatile int screenY = 0;
    private volatile int myX = 0;
    private volatile int myY = 0;

    private static final int WIDTH = 120;
    private static final int TOP_HEIGHT = 40;
    private static final int INPUT_HEIGHT = 40;
    private static final float INPUT_WEIGHT = INPUT_HEIGHT / (float) TOP_HEIGHT;
    private static final int TITLE_FONT_SIZE = 20;

    private static boolean drawBorders = false;

    public GUINoiseModule(Supplier<NoiseModule> supplier){
        System.out.println(INPUT_WEIGHT);
        noiseModule = supplier.get();
        inputNames = noiseModule.inputNames();
        height = inputNames.length * INPUT_HEIGHT + TOP_HEIGHT;

        setLayout(new GridBagLayout());
        //setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setBackground(Color.GRAY);
        setPreferredSize(new Dimension(WIDTH, height));

        createTitle();
        createInputs();
        addDragging();
    }

    private void createTitle(){
        title = new JLabel(noiseModule.getName());
        if(drawBorders) title.setBorder(BorderFactory.createLineBorder(Color.RED));

        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setVerticalAlignment(SwingConstants.TOP);

        Font font = title.getFont();
        title.setFont(new Font(font.getName(), font.getStyle(), TITLE_FONT_SIZE));

        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridy = 0;
        titleConstraints.gridx = 0;
        titleConstraints.gridwidth = 3;
        titleConstraints.insets = new Insets(0, 0, 0, 0);
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleConstraints.weightx = 0.5;
        titleConstraints.ipady = (TOP_HEIGHT - title.getHeight()) / 2;
        add(title, titleConstraints);
    }

    private void createInputs(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);
        constraints.weightx = 0.5;
        inputLabels = new JLabel[inputNames.length];
        int i = 0;
        for(String inputName : inputNames){
            JLabel inputLabel = new JLabel(inputName);
            constraints.ipady = (INPUT_HEIGHT - inputLabel.getHeight()) / 2;
            if(drawBorders) inputLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
            add(inputLabel, constraints);
            constraints.gridy++;
            inputLabels[i] = inputLabel;
            i++;
        }
    }

    private void addDragging(){
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Mouse Pressed!");

                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();

                myX = getX();
                myY = getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
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
                int deltaX = e.getXOnScreen() - screenX;
                int deltaY = e.getYOnScreen() - screenY;

                //System.out.println("Delta X: " + deltaX);

                setLocation(myX + deltaX, myY + deltaY);
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.setColor(Color.YELLOW);
        g.fillOval(getWidth() - 5, title.getY() + title.getHeight() / 2 - 5, 10, 10);

        int yOffset = title.getY() + title.getHeight() - 5;

        for(int i = 0; i < inputNames.length; i++){
            g.fillOval(-5, yOffset + inputLabels[i].getHeight() / 2, 10, 10);
            yOffset += inputLabels[i].getHeight();
        }
    }
}
