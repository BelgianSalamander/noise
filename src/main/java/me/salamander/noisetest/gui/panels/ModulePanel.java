package me.salamander.noisetest.gui.panels;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.ListTag;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.Parameter;
import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModulePanel extends JPanel {
    Random random = new Random();
    List<Connection> activeConnections = new ArrayList<>();
    private JFileChooser fileChooser = new JFileChooser();

    public ModulePanel(){
        setLayout(null);
        //setBorder(BorderFactory.createLineBorder(Color.RED));

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
                mouseMovement(e);
            }
        });

        setMinimumSize(new Dimension(400, 300));
        setSize(400, 300);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "deleteSelected");
        getActionMap().put("deleteSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
    }

    private void deleteSelected(){
        System.out.println("Deleting Selected");
        NoiseGUI containing = (NoiseGUI) (SwingUtilities.getWindowAncestor(this));
        GUINoiseModule selected = containing.getSelected();
        if(selected != null){
            for(int i = selected.getOutputConnections().size() - 1; i >= 0; i--){
                removeConnection(selected.getOutputConnections().get(i));
            }
            for(int i = 0; i < selected.getNoiseModule().numInputs(); i++){
                removeConnection(selected.getInputConnection(i));
            }
        }
        remove(selected);
        containing.showParameters(null);
        repaint();
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
            repaint();
        }
        if(!creatingConnection) return;
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
        connection.output().removeOutputConnection(connection);
        connection.input().destroyInputConnection(connection.inputIndex());
    }

    public void removeConnection(Connection connection){
        if(connection == null) return;
        connection.output().removeOutputConnection(connection);
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

    public void mouseMovement(MouseEvent e) {
        if(creatingConnection)
            repaint();
    }

    public ListTag<CompoundTag> serializeModules(){
        List<GUINoiseModule> GUIModules = Arrays.stream(getComponents()).filter(m -> m instanceof GUINoiseModule).map(m -> (GUINoiseModule) m).collect(Collectors.toList());
        List<NoiseModule> modules = GUIModules.stream().map(m -> m.getNoiseModule()).collect(Collectors.toList());
        ListTag<CompoundTag> serializedModules = Modules.serializeNodes(modules);
        for(int i = 0; i < serializedModules.size(); i++){
            CompoundTag guiInfo = new CompoundTag();
            GUIModules.get(i).writeNBT(guiInfo);
            serializedModules.get(i).put("GUIInfo", guiInfo);
        }

        return serializedModules;
    }

    public void loadNodes(ListTag<CompoundTag> modules) {
        removeAll();
        activeConnections.clear();

        List<GUINoiseModule> newModules = Modules.deserializeNodes(modules).stream().map(m -> Modules.createComponent((GUIModule) m)).collect(Collectors.toList());
        for(int i = 0; i < newModules.size(); i++){
            CompoundTag extraData = (CompoundTag) modules.get(i).get("GUIInfo");
            GUINoiseModule module = newModules.get(i);
            module.readNBT(extraData);
            add(module);

            for(int j = 0; j < module.getNoiseModule().numInputs(); j++){
                NoiseModule connectedTo = module.getNoiseModule().getInput(j);
                if(connectedTo != null){
                    for(GUINoiseModule potentialConnection : newModules){
                        if(potentialConnection.getNoiseModule() == connectedTo){
                            Connection connection = new Connection(potentialConnection, module, j);
                            potentialConnection.addOutputConnection(connection);
                            module.setInputConnection(connection, j);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static record Connection(GUINoiseModule output, GUINoiseModule input, int inputIndex){
        public Connection(GUINoiseModule output, GUINoiseModule input, int inputIndex){
            this.output = output;
            this.input = input;
            this.inputIndex = inputIndex;

            this.output.addOutputConnection(this);
            this.input.setInputConnection(this, inputIndex);
        }
    }
}
