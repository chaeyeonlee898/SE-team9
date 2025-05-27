package view.javafx;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import model.*;


public class BoardPane extends Pane {

    private final Map<String, Image> pawnCache = new HashMap<>();
    private Map<Integer,Point2D> nodePositions;
    private Map<String,List<int[]>> boardConnections;
    private final Map<Player, Color> playerColor = new HashMap<>();

    public BoardPane() {
        // 크기 변경 시 다시 그리기
        nodePositions = new HashMap<>();
        boardConnections = defineBoardConnections();
        widthProperty().addListener(o -> redraw());
        heightProperty().addListener(o -> redraw());
    }

    private Board currentBoard;
    private List<Piece> currentPieces;
    private List<Player> players;
    private final Map<Player, Rectangle> waitingAreas = new HashMap<>();
    private final Map<Player, List<Point2D>> waitingSlots = new HashMap<>();
    private Consumer<Piece> pieceClickHandler; // 말 이동시 클릭

    public void setOnPieceClick(Consumer<Piece> pieceClickHandler) {
        this.pieceClickHandler = pieceClickHandler;
    }

    public void drawBoard(Board board, List<Piece> pieces, List<Player> players) {
        // 보드 타입에 따라 nodePositions 세팅
        if (board instanceof SquareBoard) {
            nodePositions = defineSquarePositions();
        } else if (board instanceof PentagonBoard) {
            nodePositions = definePentagonPositions();
        } else if (board instanceof HexagonBoard) {
            nodePositions = defineHexagonPositions();
        }
        this.currentBoard = board;
        this.currentPieces = pieces;
        this.players = players;

        Color[] palette = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
        for (int i = 0; i < players.size(); i++) {
            playerColor.put(players.get(i), palette[i % palette.length]);
        }

        initWaitingArea();
        redraw();
    }

    private void initWaitingArea() {
        waitingAreas.clear();
        waitingSlots.clear();
        if (players == null) return;

        double w      = getWidth();
        double h      = getHeight();
        double margin = 20;
        double areaH  = 50;              // 대기 공간 높이
        double areaY  = h - areaH - margin;
        double colW   = (w - 2*margin) / players.size();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            double x = margin + i*colW;

            // ── 테두리 박스
            Rectangle rect = new Rectangle(x, areaY, colW - margin, areaH);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.GRAY);
            waitingAreas.put(p, rect);

            // ── 슬롯 좌표
            long count = currentPieces.stream()
                    .filter(pc -> pc.getOwner().equals(p)
                    && pc.getPosition() == null
                    && !pc.isFinished())
                    .count();   // 플레이어당 전체 말 개수
            double gap = (rect.getWidth()) / (count + 1);
            List<Point2D> slots = new ArrayList<>();
            for (int j = 0; j < count; j++) {
                double sx = x + gap*(j+1);
                double sy = areaY + areaH/2;
                slots.add(new Point2D(sx, sy));
            }
            waitingSlots.put(p, slots);
        }
    }


    private void redraw() {
        getChildren().clear();
        if (currentBoard == null) return;

        initWaitingArea();

        // ── 1) 대기 공간 박스
        waitingAreas.forEach((p, r) -> getChildren().add(r));

        // ── 2) 출발 전 말
        for (Player p : players) {
            List<Point2D> slots = waitingSlots.get(p);
            List<Piece> waiting = currentPieces.stream()
                    .filter(pc -> pc.getOwner().equals(p) && pc.getPosition() == null && !pc.isFinished())
                    .toList();
            for (int k = 0; k < waiting.size() && k < slots.size(); k++) {
                Piece piece = waiting.get(k);
                Point2D pt  = slots.get(k);

                // ↓ ImageView 사용
                ImageView iv = new ImageView(getPawnImage(playerColor.get(p)));
                iv.setPreserveRatio(true);
                iv.setFitWidth(30);
                iv.setFitHeight(30);
                iv.setX(pt.getX() - iv.getFitWidth()/2);
                iv.setY(pt.getY() - iv.getFitHeight()/2);
                getChildren().add(iv);
                iv.setOnMouseClicked(e -> {
                    if (pieceClickHandler != null) pieceClickHandler.accept(piece);
                });
            }
        }

        double w = getWidth(), h = getHeight();
        double scale = Math.min(w, h) / 25.0;      // Swing의 scale 값(예: 25)을 기반으로 조정
        double offsetX = (w - scale*25)/2 + 70, offsetY = (h - scale*25)/2 + 30;

        // 1) 연결선 그리기
        String type = currentBoard.getClass().getSimpleName();
        List<int[]> routes = boardConnections.get(type);
        if (routes != null) {
            for (int[] route : routes) {
                for (int i = 0; i < route.length - 1; i++) {
                    Point2D p1 = nodePositions.get(route[i]);
                    Point2D p2 = nodePositions.get(route[i+1]);
                    if (p1==null||p2==null) continue;
                    Line line = new Line(
                            offsetX + p1.getX()*scale, offsetY + p1.getY()*scale,
                            offsetX + p2.getX()*scale, offsetY + p2.getY()*scale
                    );
                    line.setStrokeWidth(2);
                    getChildren().add(line);
                }
            }
        }

        String boardType = currentBoard.getClass().getSimpleName();
        Set<Integer> specials = SPECIAL_NODES.getOrDefault(boardType, Set.of());


        // 2) 노드 그리기
        for (BoardNode node : currentBoard.getAllNodes()) {
            Point2D pos = nodePositions.get(node.getId());
            if (pos == null) continue;
            double x = offsetX + pos.getX()*scale;
            double y = offsetY + pos.getY()*scale;
            if (specials.contains(node.getId())) {
                // ── 이중 원 (특별 노드) ─────────────────
                double outerR = scale * 0.7;   // 바깥 원 지름
                double innerR = scale * 0.5;   // 안쪽 원 지름
                Circle outer = new Circle(x, y, outerR);
                outer.setFill(Color.WHITE);
                outer.setStroke(Color.BLACK);   // 테두리 색상
                outer.setStrokeWidth(2);

                Circle inner = new Circle(x, y, innerR);
                inner.setFill(Color.DARKGRAY);
                getChildren().addAll(outer, inner);
            } else {
                // ── 일반 노드 ────────────────────────
                double r = scale * 0.6;
                Circle c = new Circle(x, y, r);
                c.setFill(Color.LIGHTGRAY);
                c.setStroke(Color.BLACK);
                getChildren().add(c);
            }
        }

        // 3) 말 그리기
        // 0) 노드별 그룹핑 (완주·위치 null 제외)
        Map<BoardNode, List<Piece>> grouped =
                currentPieces.stream()
                        .filter(p -> !p.isFinished() && p.getPosition()!=null)
                        .collect(Collectors.groupingBy(Piece::getPosition));

// 1) 노드마다 한 번씩
        for (Map.Entry<BoardNode, List<Piece>> entry : grouped.entrySet()) {
            BoardNode node = entry.getKey();
            List<Piece>  list = entry.getValue();
            Point2D pos  = nodePositions.get(node.getId());
            if (pos == null) continue;

            // 중앙 좌표
            double cx = offsetX + pos.getX()*scale;
            double cy = offsetY + pos.getY()*scale;

            // 2) 같은 노드 안에서만 offset 계산
            int n = list.size();
            for (int i = 0; i < n; i++) {
                Piece piece = list.get(i);

                double offset = (i - (n-1)/2.0) * 12;  // 좌·우 대칭으로 +-12px 간격
                double x = cx + offset;
                double y = cy;

                Color col = getColorForPlayer(piece.getOwner());
                ImageView iv = new ImageView(getPawnImage(col));
                iv.setPreserveRatio(true);
                iv.setFitWidth(30);
                iv.setFitHeight(30);
                iv.setX(x - iv.getFitWidth()/2);
                iv.setY(y - iv.getFitHeight()/2);
                getChildren().add(iv);
                iv.setOnMouseClicked(e -> {
                    if (pieceClickHandler != null) pieceClickHandler.accept(piece);
                });
            }
        }
    }

    private Color getColorForPlayer(Player p) {
        return playerColor.getOrDefault(p, Color.GRAY);
    }

    /**
     * 플레이어 색상 문자열(RGB) → Image 캐시
     */
    private Image getPawnImage(Color color) {
        return pawnCache.computeIfAbsent(color.toString(), key -> {
            // 색상 문자열로 파일명 매핑
            String file = switch (key) {
                case "0xff0000ff" -> "pawn_red.png";   // Color.RED
                case "0x0000ffff" -> "pawn_blue.png";  // Color.BLUE
                case "0x008000ff" -> "pawn_green.png"; // Color.GREEN
                default           -> "pawn_yellow.png";
            };
            URL url = Objects.requireNonNull(
                    getClass().getResource("/images/" + file),
                    "이미지를 찾을 수 없습니다: " + file);
            return new Image(url.toString());
        });
    }


    private static final Map<String, Set<Integer>> SPECIAL_NODES = Map.of(
            "SquareBoard",   Set.of(0, 5, 10, 15, 28),
            "PentagonBoard", Set.of(0, 5, 10, 15, 20, 35),
            "HexagonBoard",  Set.of(0, 5, 10, 15, 20, 25, 42)
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

    private Map<Integer, Point2D> defineSquarePositions() {
        Map<Integer, Point2D> map = new HashMap<>();
        map.put(10, new Point2D(0.0, 0.0)); map.put(9, new Point2D(4, 0));
        map.put(8, new Point2D(8, 0)); map.put(7, new Point2D(12, 0));
        map.put(6, new Point2D(16, 0)); map.put(5, new Point2D(20, 0));
        map.put(22, new Point2D(3.5, 3.5)); map.put(20, new Point2D(16.5, 3.5));
        map.put(11, new Point2D(0, 4)); map.put(4, new Point2D(20, 4));
        map.put(23, new Point2D(6.5, 6.5)); map.put(21, new Point2D(13.5, 6.5));
        map.put(12, new Point2D(0, 8)); map.put(3, new Point2D(20, 8));
        map.put(28, new Point2D(10, 10));
        map.put(13, new Point2D(0, 12)); map.put(2, new Point2D(20, 12));
        map.put(24, new Point2D(6.5, 13.5)); map.put(26, new Point2D(13.5, 13.5));
        map.put(14, new Point2D(0, 16)); map.put(1, new Point2D(20, 16));
        map.put(25, new Point2D(3.5, 16.5)); map.put(27, new Point2D(16.5, 16.5));
        map.put(15, new Point2D(0, 20)); map.put(16, new Point2D(4, 20));
        map.put(17, new Point2D(8, 20)); map.put(18, new Point2D(12, 20));
        map.put(19, new Point2D(16, 20)); map.put(0, new Point2D(20, 20));
        return map;
    }

    private Map<Integer, Point2D> definePentagonPositions() {
        Map<Integer, Point2D> map = new HashMap<>();
        // 외곽 노드 (반시계 방향)
        map.put(10, new Point2D(10.0, 0.0));
        map.put(9,  new Point2D(12.0, 1.5));
        map.put(8,  new Point2D(14.0, 3.0));
        map.put(7,  new Point2D(16.0, 5.0));
        map.put(6,  new Point2D(18.0, 7.0));
        map.put(5,  new Point2D(20.0, 9.0));
        map.put(4,  new Point2D(19.0, 11.0));
        map.put(3,  new Point2D(18.0, 13.5));
        map.put(2,  new Point2D(17.0, 15.5));
        map.put(1,  new Point2D(16.0, 17.5));
        map.put(0,  new Point2D(15.0, 19.5));
        map.put(24, new Point2D(13.0, 19.5));
        map.put(23, new Point2D(11.0, 19.5));
        map.put(22, new Point2D(9.0, 19.5));
        map.put(21, new Point2D(7.0, 19.5));
        map.put(20, new Point2D(5.0, 19.5));
        map.put(19, new Point2D(4.0, 17.5));
        map.put(18, new Point2D(3.0, 15.5));
        map.put(17, new Point2D(2.0, 13.0));
        map.put(16, new Point2D(1.0, 11.0));
        map.put(15, new Point2D(0.0, 9.0));
        map.put(14, new Point2D(2.0, 7.0));
        map.put(13, new Point2D(4.0, 5.0));
        map.put(12, new Point2D(6.0, 3.0));
        map.put(11, new Point2D(8.0, 1.5));

        // 중앙 교차점 및 내부 지름길
        map.put(25, new Point2D(16.5, 9.5));
        map.put(26, new Point2D(13.3, 10.0));
        map.put(27, new Point2D(10.0, 3.5));
        map.put(28, new Point2D(10.0, 7.0));
        map.put(29, new Point2D(3.5, 9.5));
        map.put(30, new Point2D(6.7, 10.0));
        map.put(31, new Point2D(8.5, 13.0));
        map.put(32, new Point2D(7.0, 16.0));
        map.put(33, new Point2D(11.5, 13.0));
        map.put(34, new Point2D(13.0, 16.0));
        map.put(35, new Point2D(10.0, 10.5)); // 중심
        return map;
    }

    private Map<Integer, Point2D> defineHexagonPositions() {
        Map<Integer, Point2D> map = new HashMap<>();
        map.put(0, new Point2D(15, 20)); map.put(1, new Point2D(16, 18));
        map.put(2, new Point2D(17, 16)); map.put(3, new Point2D(18, 14));
        map.put(4, new Point2D(19, 12)); map.put(5, new Point2D(20, 10));
        map.put(6, new Point2D(19, 8)); map.put(7, new Point2D(18, 6));
        map.put(8, new Point2D(17, 4)); map.put(9, new Point2D(16, 2));
        map.put(10, new Point2D(15, 0)); map.put(11, new Point2D(13, 0));
        map.put(12, new Point2D(11, 0)); map.put(13, new Point2D(9, 0));
        map.put(14, new Point2D(7, 0)); map.put(15, new Point2D(5, 0));
        map.put(16, new Point2D(4, 2)); map.put(17, new Point2D(3, 4));
        map.put(18, new Point2D(2, 6)); map.put(19, new Point2D(1, 8));
        map.put(20, new Point2D(0, 10)); map.put(21, new Point2D(1, 12));
        map.put(22, new Point2D(2, 14)); map.put(23, new Point2D(3, 16));
        map.put(24, new Point2D(4, 18)); map.put(25, new Point2D(5, 20));
        map.put(26, new Point2D(7, 20)); map.put(27, new Point2D(9, 20));
        map.put(28, new Point2D(11, 20)); map.put(29, new Point2D(13, 20));
        map.put(30, new Point2D(17, 10)); map.put(31, new Point2D(13.5, 10));
        map.put(32, new Point2D(13.3, 3.5)); map.put(33, new Point2D(11.7, 7));
        map.put(34, new Point2D(6.7, 3.5)); map.put(35, new Point2D(8.3, 7));
        map.put(36, new Point2D(3, 10)); map.put(37, new Point2D(6.5, 10));
        map.put(38, new Point2D(8.3, 13)); map.put(39, new Point2D(6.7, 16.5));
        map.put(40, new Point2D(11.7, 13)); map.put(41, new Point2D(13.3, 16.5));
        map.put(42, new Point2D(10, 10));
        return map;
    }
}