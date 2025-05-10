package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * ControlPanel은 게임 조작 버튼(윷 던지기 등)을 제공하며,
 * GameController를 통해 게임 로직과 연결된다.
 */
public class ControlPanel extends JPanel {

    private final GameController controller;
    private final JButton throwButton;
    //private final JButton selectButton;

    public ControlPanel(GameController controller) {
        this.controller = controller;

        setLayout(new FlowLayout());

        throwButton = new JButton("윷 던지기");
        //selectButton = new JButton("지정 윷 던지기");

        add(throwButton);
        //add(selectButton);

        throwButton.addActionListener(e -> controller.chooseThrowMode());
        //selectButton.addActionListener(e -> controller.chooseThrowMode());


        // throwButton.addActionListener(e -> controller.throwYutRandom());
        //selectButton.addActionListener(e -> controller.throwYutManual());
    }
}