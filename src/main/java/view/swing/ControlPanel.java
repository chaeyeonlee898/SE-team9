package view.swing;

import controller.SwingGameController;

import javax.swing.*;
import java.awt.*;

/**
 * ControlPanel은 게임 조작 버튼(윷 던지기 등)을 제공하며,
 * GameController를 통해 게임 로직과 연결된다.
 */
public class ControlPanel extends JPanel {

    private final JButton throwButton;

    public ControlPanel(SwingGameController controller) {

        setLayout(new FlowLayout());

        throwButton = new JButton("윷 던지기");
        add(throwButton);

        // 버튼 클릭 시 GameController의 onRoll() 메서드를 호출
        throwButton.addActionListener(e -> controller.onRoll());
    }
}