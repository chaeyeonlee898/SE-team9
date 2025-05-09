package view;

import controller.GameController;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * GameFrame은 전체 윷놀이 Swing UI의 최상위 JFrame이다.
 * - 시작 시 플레이어 수/말 수/보드 타입을 설정하는 초기 화면 제공
 * - 설정 완료 후 GamePanel과 ControlPanel로 전환
 */
public class GameFrame extends JFrame {

    private JPanel startPanel;
    private JComboBox<Integer> playerCountBox;
    private JComboBox<Integer> pieceCountBox;
    private JComboBox<String> boardTypeBox;
    private JButton startButton;

    private GamePanel gamePanel;
    private ControlPanel controlPanel;
    private JLabel turnLabel;
    private JLabel statusLabel;
    private JTextArea logArea;
    private Board board;
    private Game game;

    public GameFrame() {
        setTitle("윷놀이 게임");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initStartPanel();
        add(startPanel, BorderLayout.CENTER);
    }

    /**
     * 초기 설정 화면 구성 (플레이어 수, 말 수, 보드 타입)
     */
    private void initStartPanel() {
        startPanel = new JPanel();
        startPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        startPanel.add(new JLabel("플레이어 수 (2~4):"), gbc);
        gbc.gridx = 1;
        playerCountBox = new JComboBox<>(new Integer[]{2, 3, 4});
        startPanel.add(playerCountBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        startPanel.add(new JLabel("말 수 (2~5):"), gbc);
        gbc.gridx = 1;
        pieceCountBox = new JComboBox<>(new Integer[]{2, 3, 4, 5});
        startPanel.add(pieceCountBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        startPanel.add(new JLabel("보드 타입:"), gbc);
        gbc.gridx = 1;
        boardTypeBox = new JComboBox<>(new String[]{"사각형", "오각형", "육각형"});
        startPanel.add(boardTypeBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        startButton = new JButton("게임 시작");
        startPanel.add(startButton, gbc);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int numPlayers = (Integer) playerCountBox.getSelectedItem();
                int numPieces = (Integer) pieceCountBox.getSelectedItem();
                String boardType = (String) boardTypeBox.getSelectedItem();

                switch (boardType) {
                    case "오각형": board = new PentagonBoard(); break;
                    case "육각형": board = new HexagonBoard(); break;
                    default: board = new SquareBoard(); break;
                }

                game = new Game(numPlayers, numPieces, board);
                startGame();
            }
        });
    }

    /**
     * 설정 후 실제 게임 UI 로딩
     */
    private void startGame() {
        getContentPane().removeAll();

        gamePanel = new GamePanel(game);
        turnLabel = new JLabel();
        turnLabel.setFont(new Font("SanaSerif", Font.BOLD, 18));
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(turnLabel, BorderLayout.NORTH);

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(statusLabel, BorderLayout.WEST);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(250, 0));
        add(scrollPane, BorderLayout.EAST);

        GameController controller = new GameController(game, gamePanel, turnLabel, logArea, this, statusLabel);
        controlPanel = new ControlPanel(controller);

        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    public void showStartPanel() {
        getContentPane().removeAll();
        initStartPanel();
        add(startPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }




}

