package me.salamander.ourea.gui;

import javax.swing.*;

public class GUIHelper {
    public static void setFontSize(JLabel label, int size){
        label.setFont(label.getFont().deriveFont((float) size));
    }
}
