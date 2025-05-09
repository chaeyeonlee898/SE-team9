package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GamePanel은 게임의 보드 및 말 위치를 시각적으로 표시하는 컴포넌트
 */
public class GamePanel extends JPanel {

    private Game game;
    private final Map<Integer, Point> nodePositions;


    public GamePanel(Game game) {
        this.game = game;
        if (game.getBoard() instanceof SquareBoard) {
            this.nodePositions = defineSquarePositions();
        } else if (game.getBoard() instanceof PentagonBoard) {
            this.nodePositions = definePentagonPositions();
        } else if (game.getBoard() instanceof HexagonBoard) {
            this.nodePositions = defineHexagonPositions();
        } else {
            this.nodePositions = new HashMap<>();
        }
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<BoardNode> nodes = game.getBoard().getAllNodes();
        int radius = 10;
        int padding = 5;

        for (BoardNode node : nodes) {
            int id = node.getId();
            Point pos = nodePositions.get(id);
            if (pos == null) continue;

            int x = 60 + pos.x * (radius * 2 + padding);
            int y = 60 + pos.y * (radius * 2 + padding);

            // 노드 표시
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(x, y, radius * 4, radius * 4);
            g2.setColor(Color.BLACK);
            g2.drawOval(x, y, radius * 4, radius * 4);
            g2.drawString(node.getName(), x + 10, y + radius * 4 + 12);

            // 말 표시 (플레이어별 색상)
            int offset = 0;
            for (Piece piece : node.getPieces()) {
                g2.setColor(getColorForPlayer(piece.getOwner()));
                g2.fillOval(x + 5 + offset, y + 5, 10, 10);
                offset += 12;
            }
        }
    }

    private Map<Integer, Point> defineSquarePositions() {
        Map<Integer, Point> map = new HashMap<>();
        map.put(10, new Point(0, 0)); map.put(9, new Point(4, 0));
        map.put(8, new Point(8, 0)); map.put(7, new Point(12, 0));
        map.put(6, new Point(16, 0)); map.put(5, new Point(20, 0));
        map.put(22, new Point(3, 3)); map.put(20, new Point(17, 3));
        map.put(11, new Point(0, 4)); map.put(4, new Point(20, 4));
        map.put(23, new Point(7, 7)); map.put(21, new Point(13, 7));
        map.put(12, new Point(0, 8)); map.put(3, new Point(20, 8));
        map.put(28, new Point(10, 10));
        map.put(13, new Point(0, 12)); map.put(2, new Point(20, 12));
        map.put(24, new Point(7, 13)); map.put(26, new Point(13, 13));
        map.put(14, new Point(0, 16)); map.put(1, new Point(20, 16));
        map.put(25, new Point(3, 17)); map.put(27, new Point(17, 17));
        map.put(15, new Point(0, 20)); map.put(16, new Point(4, 20));
        map.put(17, new Point(8, 20)); map.put(18, new Point(12, 20));
        map.put(19, new Point(16, 20)); map.put(0, new Point(20, 20));
        return map;
    }

    private Map<Integer, Point> definePentagonPositions() {
        Map<Integer, Point> map = new HashMap<>();
        map.put(1, new Point(16, 17)); map.put(2, new Point(17, 15));
        map.put(3, new Point(18, 12)); map.put(4, new Point(19, 10));
        map.put(5, new Point(20, 8)); map.put(6, new Point(18, 7));
        map.put(7, new Point(16, 5)); map.put(8, new Point(14, 3));
        map.put(9, new Point(12, 1)); map.put(10, new Point(10, 0));
        map.put(11, new Point(8, 1)); map.put(12, new Point(6, 3));
        map.put(13, new Point(4, 5)); map.put(14, new Point(2, 7));
        map.put(15, new Point(0, 8)); map.put(16, new Point(1, 10));
        map.put(17, new Point(2, 12)); map.put(18, new Point(3, 15));
        map.put(19, new Point(4, 17)); map.put(20, new Point(5, 19));
        map.put(21, new Point(7, 19)); map.put(22, new Point(9, 19));
        map.put(23, new Point(11, 19)); map.put(24, new Point(13, 19));
        map.put(25, new Point(17, 9)); map.put(26, new Point(14, 10));
        map.put(27, new Point(10, 3)); map.put(28, new Point(10, 7));
        map.put(29, new Point(3, 9)); map.put(30, new Point(6, 10));
        map.put(31, new Point(8, 13)); map.put(32, new Point(7, 16));
        map.put(33, new Point(12, 13)); map.put(34, new Point(13, 16));
        map.put(35, new Point(10, 11)); map.put(0, new Point(15, 19));
        return map;
    }

    private Map<Integer, Point> defineHexagonPositions() {
        Map<Integer, Point> map = new HashMap<>();
        map.put(0, new Point(15, 20)); map.put(1, new Point(16, 18));
        map.put(2, new Point(17, 16)); map.put(3, new Point(18, 14));
        map.put(4, new Point(19, 12)); map.put(5, new Point(20, 10));
        map.put(6, new Point(19, 8)); map.put(7, new Point(18, 6));
        map.put(8, new Point(17, 4)); map.put(9, new Point(16, 2));
        map.put(10, new Point(15, 0)); map.put(11, new Point(13, 0));
        map.put(12, new Point(11, 0)); map.put(13, new Point(9, 0));
        map.put(14, new Point(7, 0)); map.put(15, new Point(5, 0));
        map.put(16, new Point(4, 2)); map.put(17, new Point(3, 4));
        map.put(18, new Point(2, 6)); map.put(19, new Point(1, 8));
        map.put(20, new Point(0, 10)); map.put(21, new Point(1, 12));
        map.put(22, new Point(2, 14)); map.put(23, new Point(3, 16));
        map.put(24, new Point(4, 18)); map.put(25, new Point(5, 20));
        map.put(26, new Point(7, 20)); map.put(27, new Point(9, 20));
        map.put(28, new Point(11, 20)); map.put(29, new Point(13, 20));
        map.put(30, new Point(17, 10)); map.put(31, new Point(13, 10));
        map.put(32, new Point(13, 3)); map.put(33, new Point(12, 7));
        map.put(34, new Point(7, 3)); map.put(35, new Point(8, 7));
        map.put(36, new Point(3, 10)); map.put(37, new Point(7, 10));
        map.put(38, new Point(8, 13)); map.put(39, new Point(7, 17));
        map.put(40, new Point(12, 13)); map.put(41, new Point(13, 17));
        map.put(42, new Point(10, 10));
        return map;
    }

    private Color getColorForPlayer(Player player) {
        int idx = game.getPlayers().indexOf(player);
        Color[] palette = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA};
        return palette[idx % palette.length];
    }


    public void refresh() {
        repaint();
    }
}
