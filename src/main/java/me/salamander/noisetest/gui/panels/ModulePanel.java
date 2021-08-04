package me.salamander.noisetest.gui.panels;

import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.Parameter;
import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.modules.GUIModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ModulePanel extends JPanel {
    Random random = new Random();
    List<Connection> activeConnections = new ArrayList<>();

    public ModulePanel(){
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.RED));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                resetSelection();
                cancelConnection();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if(creatingConnection)
                    repaint();
            }
        });

        setMinimumSize(new Dimension(400, 300));
        setSize(400, 300);

        addModule(Modules.ADD);
        addModule(Modules.PERLIN);
        addModule(Modules.CHECKERBOARD);
    }

    private void resetSelection(){
        Window topFrame = SwingUtilities.getWindowAncestor(this);
        if(topFrame instanceof NoiseGUI){
            ((NoiseGUI) topFrame).showParameters(null);
        }else{
            throw new IllegalStateException("GUINoiseModule is not the child of a ModulePanel");
        }
    }

    public void addModule(Supplier<GUINoiseModule> supplier){
        GUINoiseModule module = supplier.get();
        add(module);
        module.setLocation(random.nextInt(getWidth() - module.getWidth()), random.nextInt(getHeight() - module.getHeight()));
    }

    private boolean creatingConnection = false;
    private int connectionStartX, connectionStartY;
    private GUINoiseModule connectingFrom;
    public void beginConnection(int x, int y, GUINoiseModule module){
        connectionStartX = x;
        connectionStartY = y;
        connectingFrom = module;
        creatingConnection = true;

        System.out.println("Began Connection from " + x + " " + y);
    }

    public void createConnection(GUINoiseModule to, int index){
        Connection currentConnection = to.getInputConnection(index);
        if(currentConnection != null){
            removeConnection(currentConnection);
        }
        to.getNoiseModule().setInput(index, connectingFrom.getNoiseModule());
        activeConnections.add(new Connection(connectingFrom, to, index));
        cancelConnection();
    }

    public void cancelConnection(){
        creatingConnection = false;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if(creatingConnection){
            drawNiceLineBetween(new Point(connectionStartX, connectionStartY), getMousePosition(), g);
        }

        List<Integer> connectionIndicesToRemove = new ArrayList<>();
        {
            int i = -1;
            for (Connection connection : activeConnections) {
                i++;
                GUINoiseModule from = connection.input();
                GUINoiseModule to = connection.output();
                if (!(from.isVisible() && to.isVisible())) {
                    connectionIndicesToRemove.add(i);
                    continue;
                }
                drawNiceLineBetween(connection.output().getOutputLocation(), connection.input().getInputLocation(connection.inputIndex()), g);
            }
        }

        for(int i = connectionIndicesToRemove.size() - 1; i >= 0; i--){
            Connection connection = activeConnections.get(i);
            destroyConnection(connection);
            activeConnections.remove(i);
        }
    }

    private void destroyConnection(Connection connection){
        connection.output().destroyOutputConnection();
        connection.input().destroyInputConnection(connection.inputIndex());
    }

    public void removeConnection(Connection connection){
        connection.output().destroyOutputConnection();
        connection.input().destroyInputConnection(connection.inputIndex());
        activeConnections.remove(connection);
    }

    private void drawNiceLineBetween(Point from, Point to, Graphics g){
        int midX = (from.x + to.x) / 2;

        int connectionSize = GUINoiseModule.getConnectionSize();
        if(to.x < from.x) connectionSize *= -1;
        g.setColor(Color.BLACK);
        fillRect(from.x, from.y - connectionSize / 2, midX - from.x + connectionSize / 2, connectionSize, g);
        fillRect(midX - connectionSize / 2, to.y - connectionSize / 2, to.x - midX + connectionSize / 2, connectionSize, g);
        fillRect(midX - connectionSize / 2, from.y + connectionSize / 2, connectionSize, to.y - from.y - connectionSize, g);
    }

    private void fillRect(int x, int y, int width, int height, Graphics g){
        if(width < 0){
            x += width;
            width = -width;
        }

        if(height < 0){
            y += height;
            height = -height;
        }

        g.fillRect(x, y, width, height);
    }

    public static record Connection(GUINoiseModule output, GUINoiseModule input, int inputIndex){
        public Connection(GUINoiseModule output, GUINoiseModule input, int inputIndex){
            this.output = output;
            this.input = input;
            this.inputIndex = inputIndex;

            this.output.setOutputConnection(this);
            this.input.setInputConnection(this, inputIndex);
        }
    }
}
