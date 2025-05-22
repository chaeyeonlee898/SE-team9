package app;

import view.swing.GameFrame;

import javax.swing.*;

public class MainSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}