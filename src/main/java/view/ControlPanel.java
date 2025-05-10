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

/**
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * ControlPanel은 윷 던지기 버튼과 결과 표시, 말 선택, 게임 진행을 제어한다.

public class ControlPanel extends JPanel {

    private Game game;
    private GamePanel gamePanel;
    private JButton throwButton;
    private JButton selectButton;
    private JLabel resultLabel;
    private JComboBox<Piece> pieceSelector;

    public ControlPanel(Game game, GamePanel gamePanel) {
        this.game = game;
        this.gamePanel = gamePanel;

        setLayout(new FlowLayout());

        throwButton = new JButton("랜덤 윷 던지기");
        selectButton = new JButton("지정 윷 던지기");
        resultLabel = new JLabel("결과: ");
        pieceSelector = new JComboBox<>();

        add(throwButton);
        add(selectButton);
        add(resultLabel);
        add(new JLabel("이동할 말:"));
        add(pieceSelector);

        // 랜덤 윷 던지기
        throwButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleYutThrow(YutResult.throwYut(new Random()));
            }
        });

        // 지정 윷 던지기
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
                String selected = (String) JOptionPane.showInputDialog(
                        ControlPanel.this,
                        "지정할 윷 결과를 선택하세요:",
                        "지정 윷 던지기",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (selected != null) {
                    YutResult result = null;
                    switch (selected) {
                        case "빽도": result = YutResult.BACKDO; break;
                        case "도": result = YutResult.DO; break;
                        case "개": result = YutResult.GAE; break;
                        case "걸": result = YutResult.GEOL; break;
                        case "윷": result = YutResult.YUT; break;
                        case "모": result = YutResult.MO; break;
                    }
                    if (result != null) {
                        handleYutThrow(result);
                    }
                }
            }
        });
    }

    private void handleYutThrow(YutResult result) {
        resultLabel.setText("결과: " + result);

        Player current = game.getPlayers().get(game.getCurrentTurn());

        // 말 선택 UI 구성
        pieceSelector.removeAllItems();
        for (Piece p : current.getPieces()) {
            if (!p.isFinished()) {
                pieceSelector.addItem(p);
            }
        }

        // 결과와 말 선택을 함께 보여주는 패널 구성
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("윷 결과: " + result));
        panel.add(pieceSelector);

        int confirm = JOptionPane.showConfirmDialog(this, panel, "이동할 말을 선택하세요", JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;

        Piece selectedPiece = (Piece) pieceSelector.getSelectedItem();
        if (selectedPiece != null) {
            boolean extraTurn = game.applyYutResult(result, selectedPiece);
            gamePanel.refresh();

            if (game.isCurrentPlayerWin()) {
                JOptionPane.showMessageDialog(this, current.getName() + " 승리! 게임 종료");
                throwButton.setEnabled(false);
                selectButton.setEnabled(false);
                return;
            }

            if (extraTurn) {
                JOptionPane.showMessageDialog(this, "윷/모 또는 캡처! 한 번 더 던지세요.");
            } else {
                game.nextTurn();
            }
        }
    }
}
*/