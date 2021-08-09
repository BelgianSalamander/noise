package me.salamander.noisetest.gui;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.ListTag;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectData {
    private static final JFileChooser fileChooser = new JFileChooser();
    private String pathToSave = null;

    public ProjectData(){ }

    public void save(NoiseGUI gui){
        if(pathToSave == null) saveAs(gui);
        else saveToPath(pathToSave, gui);
    }

    public static String getDirectory(){
        return fileChooser.getCurrentDirectory().getAbsolutePath();
    }

    public static void setDirectory(String absolutePath){
        fileChooser.setCurrentDirectory(new File(absolutePath));
    }

    public void saveAs(NoiseGUI gui){
        int returnVal = fileChooser.showSaveDialog(gui);

        if(returnVal != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        if(!file.exists()){
            try {
                Files.createFile(file.toPath());
            }catch (IOException e){
                e.printStackTrace();
                return;
            }
        }

        pathToSave = file.getAbsolutePath();

        saveToPath(pathToSave, gui);
    }

    public static ProjectData queryLoad(NoiseGUI gui){
        int returnVal = fileChooser.showOpenDialog(gui);

        if(returnVal != JFileChooser.APPROVE_OPTION) return null;

        File file = fileChooser.getSelectedFile();
        return load(file.getAbsolutePath(), gui);
    }

    public static ProjectData load(String path, NoiseGUI gui){
        ProjectData project = new ProjectData();
        project.pathToSave = path;
        CompoundTag data = null;
        try {
            FileInputStream in = new FileInputStream(path);
            data = CompoundTag.read(new DataInputStream(in));
            in.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        ListTag<CompoundTag> modules = (ListTag<CompoundTag>) data.get("modules");
        gui.modulePanel.loadNodes(modules);

        return project;
    }

    private void saveToPath(String path, NoiseGUI gui){
        CompoundTag result = new CompoundTag();

        ListTag<CompoundTag> moduleInfo = gui.modulePanel.serializeModules();
        result.put("modules", moduleInfo);

        if(!Files.exists(Path.of(path))){
            try {
                Files.createFile(Path.of(path));
            }catch (IOException e){
                e.printStackTrace();
                return;
            }
        }

        try{
            FileOutputStream out = new FileOutputStream(path);
            result.write(new DataOutputStream(out));
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    static {
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getPath().endsWith(".sn") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "*.sn";
            }
        });
    }
}
