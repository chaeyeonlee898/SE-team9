package view.swing;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * GamePanel은 게임의 보드 및 말 위치를 시각적으로 표시하는 컴포넌트
 */
public class GamePanel extends JPanel {

    private Game game;
    private final Map<Integer, Point2D.Double> nodePositions;
    private final Map<Player, Image> pawnImages = new HashMap<>();
    private final Map<Player, Point> waitingPos = new HashMap<>();
    private final Map<Piece, Rectangle> pieceBounds = new HashMap<>();
    private final Map<Player, Rectangle> waitingAreas = new HashMap<>();
    private final Map<Player, List<Point>> waitingSlots = new HashMap<>();
    private Player highlightedPlayer = null;

    public interface PieceClickedListener {
        void onPieceClicked(Piece piece);
    }
    private PieceClickedListener clickedListener;

    public void setPieceClickListener(PieceClickedListener listener) {
        this.clickedListener = listener;}

    public void highlightCurrentPlayer(Player p) {
        this.highlightedPlayer = p;
        repaint();
    }

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
        List<String> pawnFiles = List.of(
                "pawn_red.png",
                "pawn_blue.png",
                "pawn_green.png",
                "pawn_yellow.png"
        );

        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            String fileName = pawnFiles.get(i % pawnFiles.size());
            URL url = getClass().getResource("/images/" + fileName);
            if (url != null) {
                Image raw = Toolkit.getDefaultToolkit().getImage(url);
                // 한 번만 크기 조정
                Image pawnImg = raw.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                pawnImages.put(players.get(i), pawnImg);
            } else {
                System.err.println("이미지를 찾을 수 없습니다: " + fileName);
            }
        }

        // 기존 노드 위치 계산…
        calculateWaitingPositions();

        // 크기 변경될 때마다 다시 계산
        addComponentListener(new ComponentAdapter() {
                                 @Override
                                 public void componentResized(ComponentEvent e) {
                                     calculateWaitingPositions();
                                     repaint();
                                 }
                             });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Piece hit = findClickedPiece(e.getPoint());
                if (hit != null && clickedListener != null) {
                    clickedListener.onPieceClicked(hit);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        pieceBounds.clear();
        super.paintComponent(g);
        calculateWaitingPositions();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<BoardNode> nodes = game.getBoard().getAllNodes();
        int radius = 10;
        int padding = 5;

        // Draw connection routes
        Map<String, List<int[]>> allConnections = defineBoardConnections();
        String boardType = game.getBoard().getClass().getSimpleName();
        Set<Integer> specials = SPECIAL_NODES.getOrDefault(boardType, Collections.emptySet());

        List<int[]> routes = allConnections.get(boardType);
        if (routes != null) {
            for (int[] route : routes) {
                drawConnection(g2, route, nodePositions, 25, 80, 80);
            }
        }

        // 1) waitingAreas 먼저 그리기
        for (var entry : waitingAreas.entrySet()) {
            Player p = entry.getKey();
            Rectangle r = entry.getValue();
            if (p.equals(highlightedPlayer)) {
                g2.setColor(Color.ORANGE);
                g2.setStroke(new BasicStroke(3));
            } else {
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(1));
            }
            g2.draw(r);
        }

        for (BoardNode node : nodes) {
            int id = node.getId();
            Point2D.Double pos = nodePositions.get(id);
            if (pos == null) continue;

            int x = (int) (60 + pos.x * (radius * 2 + padding));
            int y = (int) (60 + pos.y * (radius * 2 + padding));
            int diameter = radius * 4;

            // 노드 표시
            if (specials.contains(id)) {
                // 바깥 원 (흰색·검은 테두리 굵게)
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(x, y, diameter, diameter);

                // 안쪽 원 (작게)
                int outerRadius = diameter/2;       // radius*2
                int innerRadius = (int)(outerRadius * 0.7);
                int cx = x + outerRadius;
                int cy = y + outerRadius;
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillOval(
                        cx - innerRadius,
                        cy - innerRadius,
                        innerRadius*2,
                        innerRadius*2
                );

                // 스트로크 두께 원복
                g2.setStroke(new BasicStroke(1));
            }
            else {
                // 일반 노드
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(Color.BLACK);
                g2.drawOval(x, y, diameter, diameter);
            }

            // 노드 이름
            g2.setColor(Color.BLACK);
            g2.drawString(node.getName(), x + 10, y + diameter + 12);

            // 말 표시 (플레이어별 색상)
            int offset = 0;
            // 원들로 말 표시
//            for (Piece piece : node.getPieces()) {
//                g2.setColor(getColorForPlayer(piece.getOwner()));
//                g2.fillOval(x + 5 + offset, y + 5, 10, 10);
//                offset += 12;
//            }
            for (Piece piece : node.getPieces()) {
                Image pawnImg = pawnImages.get(piece.getOwner());
                if (pawnImg != null) {
                    int w = 30, h = 30;
                    int drawX = x + 5 + offset;
                    int drawY = y + 5;

                    g2.drawImage(pawnImg, drawX, drawY, w, h, this);
                    pieceBounds.put(piece, new Rectangle(drawX, drawY, w, h));
                } else {
                    // 이미지가 없을 땐 기존 방식으로 fallback
                    g2.setColor(getColorForPlayer(piece.getOwner()));
                    g2.fillOval(x + 5 + offset, y + 5, 10, 10);
                }
                offset += 12;
            }
        }
        for (Player p : game.getPlayers()) {
            List<Point> slots = waitingSlots.get(p);
            int k = 0;
            for (Piece piece : p.getUnfinishedPieces()) {
                if (piece.getPosition() != null || piece.isFinished()) continue;
                Image pawn = pawnImages.get(p);
                Point pt = slots.get(k++);
                int w = 30, h = 30;
                int drawX = pt.x - w/2;
                int drawY = pt.y - h/2;

                // 1) 이미지 그리기
                g2.drawImage(pawn, drawX, drawY, w, h, this);

                // 2) 클릭 판정용 사각형 등록
                pieceBounds.put(piece, new Rectangle(drawX, drawY, w, h));
            }
        }
//        for (Player player : game.getPlayers()) {
//            Point pos = waitingPos.get(player);
//            int offset = 0;
//            // 아직 보드에 올라가지 않은 말만 꺼내기
//            for (Piece p : player.getUnfinishedPieces()) {
//                // (모델에 isOnBoard() 같은 게 있으면 그걸 쓰고,
//                // 없다면 p.getCurrentNode() == null 체크)
//                if (p.getPosition() != null) continue;
//                Image pawn = pawnImages.get(player);
//                int w = 30, h = 30;
//                int drawX = pos.x + 5 + offset;
//                int drawY = pos.y + 5;
//
//                g2.drawImage(pawn, drawX, drawY, w, h, this);
//                pieceBounds.put(p, new Rectangle(drawX, drawY, w, h));
//                offset += 35;
//            }
//        }
    }

    private void drawConnection(Graphics2D g2, int[] nodeIds, Map<Integer, Point2D.Double> posMap, int scale, int offsetX, int offsetY) {
        for (int i = 0; i < nodeIds.length - 1; i++) {
            Point2D.Double p1 = posMap.get(nodeIds[i]);
            Point2D.Double p2 = posMap.get(nodeIds[i + 1]);
            if (p1 == null || p2 == null) continue;

            int x1 = (int)(offsetX + p1.x * scale);
            int y1 = (int)(offsetY + p1.y * scale);
            int x2 = (int)(offsetX + p2.x * scale);
            int y2 = (int)(offsetY + p2.y * scale);

            g2.setColor(Color.DARK_GRAY); // 선 색상
            g2.setStroke(new BasicStroke(2)); // 선 두께
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    /** 플레이어별 '대기 영역' 좌표 계산 */
    private void calculateWaitingPositions() {
        waitingPos.clear();
        waitingAreas.clear();
        waitingSlots.clear();
        int margin = 20;
        int areaH = 30; // 말 그릴 y 좌표
        int areaY = getHeight() - areaH - margin;
        int spacing = 40;
        int cols = game.getPlayers().size();
        int colW = (getWidth() - 2 * margin) / cols;

        int x = margin;

        for (Player p : game.getPlayers()) {
            // 1) 이 플레이어의 대기 말 개수
            long count = p.getUnfinishedPieces().stream()
                    .filter(pc -> pc.getPosition() == null && !pc.isFinished())
                    .count();

            // 2) 영역 폭 = (말 개수 × spacing), 단 말이 하나도 없으면 최소 spacing 확보
            int rectW = (int)(count > 0 ? count * spacing : spacing);

            // 3) 영역 사각형 저장
            Rectangle area = new Rectangle(x, areaY - 5, rectW, areaH + 10);
            waitingAreas.put(p, area);

            // 4) 각 말의 슬롯 위치 계산
            List<Point> slots = new ArrayList<>();
            double gap = (double)rectW / (count + 1);
            for (int j = 0; j < count; j++) {
                int sx = (int)(x + gap * (j + 1));
                int sy = areaY + areaH / 2;
                slots.add(new Point(sx, sy));
            }
            waitingSlots.put(p, slots);

            // 5) 다음 플레이어 영역의 x 시작점
            x += rectW + margin;
        }
    }

    private static final Map<String, Set<Integer>> SPECIAL_NODES = Map.of(
            "SquareBoard", Set.of(0, 5, 10, 15, 28),
            "PentagonBoard", Set.of(0, 5, 10, 15, 20, 35),
            "HexagonBoard", Set.of(0, 5, 10, 15, 20, 25, 42)
    );

    private Map<String, List<int[]>> defineBoardConnections() {
        Map<String, List<int[]>> connections = new HashMap<>();

        // 사각형 보드에서 연결할 경로들
        connections.put("SquareBoard", List.of(
                new int[]{5, 20, 21, 28},    // 예: 대각선
                new int[]{10, 22, 23, 28},
                new int[]{15, 25, 24, 28},
                new int[]{0, 27, 26, 28},
                new int[]{0, 1, 2, 3, 4, 5},
                new int[]{5, 6, 7, 8, 9, 10},
                new int[]{10, 11, 12, 13, 14, 15},
                new int[]{15, 16, 17, 18, 19, 0}
        ));

        // 오각형 보드에서 연결할 경로들
        connections.put("PentagonBoard", List.of(
                new int[]{15, 29, 30, 35},
                new int[]{5, 25, 26, 35},
                new int[]{20, 32, 31, 35},
                new int[]{0, 34, 33, 35},
                new int[]{10, 27, 28, 35},
                new int[]{0, 1, 2, 3, 4, 5},
                new int[]{5, 6, 7, 8, 9, 10},
                new int[]{10, 11, 12, 13, 14, 15},
                new int[]{15, 16, 17, 18, 19, 20},
                new int[]{20, 21, 22, 23, 24, 0}
        ));

        // 육각형 보드 (예시)
        connections.put("HexagonBoard", List.of(
                new int[]{5, 30, 31, 42},
                new int[]{10, 32, 33, 42},
                new int[]{15, 34, 35, 42},
                new int[]{20, 36, 37, 42},
                new int[]{25, 39, 38, 42},
                new int[]{0, 41, 40, 42},
                new int[]{0, 1, 2, 3, 4, 5},
                new int[]{5, 6, 7, 8, 9, 10},
                new int[]{10, 11, 12, 13, 14, 15},
                new int[]{15, 16, 17, 18, 19, 20},
                new int[]{20, 21, 22, 23, 24, 25},
                new int[]{25, 26, 27, 28, 29, 0}

        ));

        return connections;
    }

    private Map<Integer, Point2D.Double> defineSquarePositions() {
        Map<Integer, Point2D.Double> map = new HashMap<>();
        map.put(10, new Point2D.Double(0, 0)); map.put(9, new Point2D.Double(4, 0));
        map.put(8, new Point2D.Double(8, 0)); map.put(7, new Point2D.Double(12, 0));
        map.put(6, new Point2D.Double(16, 0)); map.put(5, new Point2D.Double(20, 0));
        map.put(22, new Point2D.Double(3.5, 3.5)); map.put(20, new Point2D.Double(16.5, 3.5));
        map.put(11, new Point2D.Double(0, 4)); map.put(4, new Point2D.Double(20, 4));
        map.put(23, new Point2D.Double(6.5, 6.5)); map.put(21, new Point2D.Double(13.5, 6.5));
        map.put(12, new Point2D.Double(0, 8)); map.put(3, new Point2D.Double(20, 8));
        map.put(28, new Point2D.Double(10, 10));
        map.put(13, new Point2D.Double(0, 12)); map.put(2, new Point2D.Double(20, 12));
        map.put(24, new Point2D.Double(6.5, 13.5)); map.put(26, new Point2D.Double(13.5, 13.5));
        map.put(14, new Point2D.Double(0, 16)); map.put(1, new Point2D.Double(20, 16));
        map.put(25, new Point2D.Double(3.5, 16.5)); map.put(27, new Point2D.Double(16.5, 16.5));
        map.put(15, new Point2D.Double(0, 20)); map.put(16, new Point2D.Double(4, 20));
        map.put(17, new Point2D.Double(8, 20)); map.put(18, new Point2D.Double(12, 20));
        map.put(19, new Point2D.Double(16, 20)); map.put(0, new Point2D.Double(20, 20));
        return map;
    }

    private Map<Integer, Point2D.Double> definePentagonPositions() {
        Map<Integer, Point2D.Double> map = new HashMap<>();
        // 외곽 노드 (반시계 방향)
        map.put(10, new Point2D.Double(10.0, 0.0));
        map.put(9,  new Point2D.Double(12.0, 1.5));
        map.put(8,  new Point2D.Double(14.0, 3.0));
        map.put(7,  new Point2D.Double(16.0, 5.0));
        map.put(6,  new Point2D.Double(18.0, 7.0));
        map.put(5,  new Point2D.Double(20.0, 9.0));
        map.put(4,  new Point2D.Double(19.0, 11.0));
        map.put(3,  new Point2D.Double(18.0, 13.5));
        map.put(2,  new Point2D.Double(17.0, 15.5));
        map.put(1,  new Point2D.Double(16.0, 17.5));
        map.put(0,  new Point2D.Double(15.0, 19.5));
        map.put(24, new Point2D.Double(13.0, 19.5));
        map.put(23, new Point2D.Double(11.0, 19.5));
        map.put(22, new Point2D.Double(9.0, 19.5));
        map.put(21, new Point2D.Double(7.0, 19.5));
        map.put(20, new Point2D.Double(5.0, 19.5));
        map.put(19, new Point2D.Double(4.0, 17.5));
        map.put(18, new Point2D.Double(3.0, 15.5));
        map.put(17, new Point2D.Double(2.0, 13.0));
        map.put(16, new Point2D.Double(1.0, 11.0));
        map.put(15, new Point2D.Double(0.0, 9.0));
        map.put(14, new Point2D.Double(2.0, 7.0));
        map.put(13, new Point2D.Double(4.0, 5.0));
        map.put(12, new Point2D.Double(6.0, 3.0));
        map.put(11, new Point2D.Double(8.0, 1.5));

        // 중앙 교차점 및 내부 지름길
        map.put(25, new Point2D.Double(16.5, 9.5));
        map.put(26, new Point2D.Double(13.3, 10.0));
        map.put(27, new Point2D.Double(10.0, 3.5));
        map.put(28, new Point2D.Double(10.0, 7.0));
        map.put(29, new Point2D.Double(3.5, 9.5));
        map.put(30, new Point2D.Double(6.7, 10.0));
        map.put(31, new Point2D.Double(8.5, 13.0));
        map.put(32, new Point2D.Double(7.0, 16.0));
        map.put(33, new Point2D.Double(11.5, 13.0));
        map.put(34, new Point2D.Double(13.0, 16.0));
        map.put(35, new Point2D.Double(10.0, 10.5)); // 중심
        return map;
    }

    private Map<Integer, Point2D.Double> defineHexagonPositions() {
        Map<Integer, Point2D.Double> map = new HashMap<>();
        map.put(0, new Point2D.Double(15, 20)); map.put(1, new Point2D.Double(16, 18));
        map.put(2, new Point2D.Double(17, 16)); map.put(3, new Point2D.Double(18, 14));
        map.put(4, new Point2D.Double(19, 12)); map.put(5, new Point2D.Double(20, 10));
        map.put(6, new Point2D.Double(19, 8)); map.put(7, new Point2D.Double(18, 6));
        map.put(8, new Point2D.Double(17, 4)); map.put(9, new Point2D.Double(16, 2));
        map.put(10, new Point2D.Double(15, 0)); map.put(11, new Point2D.Double(13, 0));
        map.put(12, new Point2D.Double(11, 0)); map.put(13, new Point2D.Double(9, 0));
        map.put(14, new Point2D.Double(7, 0)); map.put(15, new Point2D.Double(5, 0));
        map.put(16, new Point2D.Double(4, 2)); map.put(17, new Point2D.Double(3, 4));
        map.put(18, new Point2D.Double(2, 6)); map.put(19, new Point2D.Double(1, 8));
        map.put(20, new Point2D.Double(0, 10)); map.put(21, new Point2D.Double(1, 12));
        map.put(22, new Point2D.Double(2, 14)); map.put(23, new Point2D.Double(3, 16));
        map.put(24, new Point2D.Double(4, 18)); map.put(25, new Point2D.Double(5, 20));
        map.put(26, new Point2D.Double(7, 20)); map.put(27, new Point2D.Double(9, 20));
        map.put(28, new Point2D.Double(11, 20)); map.put(29, new Point2D.Double(13, 20));
        map.put(30, new Point2D.Double(17, 10)); map.put(31, new Point2D.Double(13.5, 10));
        map.put(32, new Point2D.Double(13.3, 3.5)); map.put(33, new Point2D.Double(11.7, 7));
        map.put(34, new Point2D.Double(6.7, 3.5)); map.put(35, new Point2D.Double(8.3, 7));
        map.put(36, new Point2D.Double(3, 10)); map.put(37, new Point2D.Double(6.5, 10));
        map.put(38, new Point2D.Double(8.3, 13)); map.put(39, new Point2D.Double(6.7, 16.5));
        map.put(40, new Point2D.Double(11.7, 13)); map.put(41, new Point2D.Double(13.3, 16.5));
        map.put(42, new Point2D.Double(10, 10));
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

    private Piece findClickedPiece(Point click) {
        for (var entry : pieceBounds.entrySet()) {
            Rectangle r = entry.getValue();
            if (r.contains(click)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
