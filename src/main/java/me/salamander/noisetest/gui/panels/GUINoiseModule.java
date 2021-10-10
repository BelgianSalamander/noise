package me.salamander.noisetest.gui.panels;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.Parameter;
import me.salamander.noisetest.modules.GUIModule;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GUINoiseModule extends JPanel {

    private JLabel titleLabel;

    private final GUIModule noiseModule;
    private final String title;
    private final int height;
    private int width;

    private final Parameter[] parameters;
    private final String[] inputNames;

    private List<ModulePanel.Connection> outputConnections = new ArrayList<>();
    private ModulePanel.Connection[] inputConnections;

    private static final int TITLE_HEIGHT = 40;
    private static final int INPUT_HEIGHT = 20;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int DEFAULT_WIDTH = 100;

    private static final int CONNECTION_SIZE = 10;

    private boolean visible = true;

    @Override
    public boolean isVisible() {
        return visible;
    }

    public static int getConnectionSize() {
        return CONNECTION_SIZE;
    }

    private int screenX, screenY, myX, myY; //For dragging

    public GUINoiseModule(String title, GUIModule module, Parameter[] parameters, String[] inputNames){
        this.title = title;
        this.parameters = parameters;
        this.inputNames = inputNames;
        this.inputConnections = new ModulePanel.Connection[module.numInputs()];
        height = TITLE_HEIGHT + module.numInputs() * INPUT_HEIGHT;

        setLayout(null);
        setBackground(Color.GRAY);
        this.noiseModule = module;

        createTitle();
        addDragging();

        createOutput();
        createInputs();

        setSize(width, height);
    }

    private void createInputs(){
        int i = 0;
        for(String inputName : inputNames){
            JLabel label = new JLabel(inputName);
            System.out.println("Create input " + inputName);
            label.setBounds(10, TITLE_HEIGHT + INPUT_HEIGHT * i, width, INPUT_HEIGHT);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setVerticalAlignment(SwingConstants.CENTER);
            add(label);

            JPanel panel = new JPanel();
            panel.setBackground(Color.BLACK);
            panel.setBounds(- CONNECTION_SIZE / 2, (int) (TITLE_HEIGHT + INPUT_HEIGHT * (i + 0.5) - CONNECTION_SIZE / 2), CONNECTION_SIZE, CONNECTION_SIZE);
            add(panel);
            i++;
        }
    }

    private void createOutput(){
        JComponent test = new JPanel();
        test.setBackground(Color.BLACK);
        test.setBounds(width - CONNECTION_SIZE / 2, TITLE_HEIGHT / 2 - CONNECTION_SIZE / 2, CONNECTION_SIZE, CONNECTION_SIZE);
        add(test);
    }

    private void createTitle(){
        titleLabel = new JLabel(title);
        //titleLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), titleLabel.getFont().getStyle(), TITLE_FONT_SIZE));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel);
        width = Math.max(titleLabel.getPreferredSize().width + 20, DEFAULT_WIDTH);
        titleLabel.setBounds(0, 0, width, TITLE_HEIGHT);
    }

    private void showParameters(){
        Window topFrame = SwingUtilities.getWindowAncestor(this);
        if(topFrame instanceof NoiseGUI){
            ((NoiseGUI) topFrame).showParameters(this);
        }else{
            throw new IllegalStateException("GUINoiseModule is not the child of a ModulePanel");
        }
    }

    private void startConnection(MouseEvent e){
        ((ModulePanel) getParent()).beginConnection(getX() + width, getY() + TITLE_HEIGHT / 2, this);
    }

    private boolean checkForInputs(MouseEvent e){
        if(e.getX() < CONNECTION_SIZE * 1.5){
            int y = e.getY() - TITLE_HEIGHT;
            int input = y / INPUT_HEIGHT;
            y %= INPUT_HEIGHT;
            if(y >= (INPUT_HEIGHT - 2 * CONNECTION_SIZE) / 2 && y <= (INPUT_HEIGHT + 2 * CONNECTION_SIZE) / 2){
                System.out.println("Tried to connect to input #"+input);
                ((ModulePanel) getParent()).createConnection(this, input);
                return true;
            }
        }
        return false;
    }

    public void destroyOutputConnections(){
        outputConnections.clear();
    }

    public void destroyInputConnection(int index){
        noiseModule.setInput(index, null);
        inputConnections[index] = null;
    }

    public Point getOutputLocation(){
        return new Point(getX() + width, getY() + TITLE_HEIGHT / 2);
    }

    public Point getInputLocation(int index){
        return new Point( getX(), getY() + (int) (TITLE_HEIGHT + INPUT_HEIGHT * (index + 0.5)));
    }

    private boolean dragging = false;
    private void addDragging(){
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getX() > width - 15 && e.getY() < TITLE_HEIGHT){
                    System.out.println("Clicked connection!");
                    startConnection(e);
                    return;
                }

                if(checkForInputs(e)){
                    return;
                }

                ((ModulePanel) getParent()).cancelConnection();

                dragging = true;
                showParameters();

                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();
                myX = getX();
                myY = getY();
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

    public GUIModule getNoiseModule() {
        return noiseModule;
    }

    public String getTitle() {
        return title;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public ModulePanel.Connection getInputConnection(int index){
        return inputConnections[index];
    }

    public void setInputConnection(ModulePanel.Connection connection, int index){
        inputConnections[index] = connection;
    }

    public void addOutputConnection(ModulePanel.Connection connection){
        outputConnections.add(connection);
    }

    public void removeOutputConnection(ModulePanel.Connection connection){
        outputConnections.remove(connection);
    }

    public List<ModulePanel.Connection> getOutputConnections(){
        return outputConnections;
    }

    public void writeNBT(CompoundTag tag){
        tag.putInt("x", getX());
        tag.putInt("y", getY());
    }

    public void readNBT(CompoundTag tag) {
        setLocation(tag.getInt("x"), tag.getInt("y"));
    }
}
