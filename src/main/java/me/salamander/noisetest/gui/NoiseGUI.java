package me.salamander.noisetest.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.NbtDeserializer;
import io.github.antiquitymc.nbt.NbtIo;
import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.gui.panels.ModulePanel;
import me.salamander.noisetest.gui.panels.ModuleSelector;
import me.salamander.noisetest.gui.panels.ParameterPanel;
import me.salamander.noisetest.modules.GUIModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class NoiseGUI extends JFrame {
    final ModulePanel modulePanel;
    final ParameterPanel parameterPanel;
    private ProjectData projectData = new ProjectData();

    private static Path dataPath = Path.of(System.getProperty("user.home"), "noise.dat");

    public NoiseGUI(){
        loadData();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
            }
        });
	    FlatLightLaf.install();

	    setLayout(new GridBagLayout());
        setSize(1000, 1000);
        setTitle("Noise Editor");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;

        modulePanel = new ModulePanel();
        add(modulePanel, constraints);

        constraints.weightx = 0.06;
        constraints.gridx = 1;
        parameterPanel = new ParameterPanel();
        add(parameterPanel, constraints);

        createMenu();

        setVisible(true);
    }

    public void showParameters(GUINoiseModule module){
        parameterPanel.showParameters(module);
    }

    private void createMenu(){
        JMenuBar menuBar = new JMenuBar();

        // todo make this
        JMenu fileMenu = new JMenu("File");
        JMenuItem open = new JMenuItem("Open Project");
        open.addActionListener(e -> {
            ProjectData project = ProjectData.queryLoad(this);
            if(project != null){
                projectData = project;
                parameterPanel.showParameters(null);
                repaint();
            }
        });
        fileMenu.add(open);
        JMenuItem save = new JMenuItem("Save Project");
        save.addActionListener(e -> projectData.save(this));
        fileMenu.add(save);
        JMenuItem saveAs = new JMenuItem("Save Project As");
        saveAs.addActionListener(e -> projectData.saveAs(this));
	    fileMenu.add(saveAs);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem addModule = new JMenuItem("Add Module");
        addModule.addActionListener(e -> new ModuleSelector(this));
        editMenu.add(addModule);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    public void addModule(Supplier<GUINoiseModule> supplier) {
        modulePanel.addModule(supplier);
    }

    public GUINoiseModule getSelected() {
        return parameterPanel.getSelectedComponent();
    }

    private void saveData(){
        CompoundTag data = new CompoundTag();
        data.putString("currentDir", ProjectData.getDirectory());

        if(!Files.exists(dataPath)){
            try {
                Files.createFile(dataPath);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(dataPath.toFile().getAbsolutePath());
            data.write(new DataOutputStream(outputStream));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void loadData(){
        if(!Files.exists(dataPath)) return;

        CompoundTag data = null;
        try {
            FileInputStream is = new FileInputStream(dataPath.toFile().getAbsolutePath());
            data = CompoundTag.read(new DataInputStream(is));
            is.close();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        ProjectData.setDirectory(data.getString("currentDir"));
    }
}
