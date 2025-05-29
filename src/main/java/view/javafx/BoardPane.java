package view.javafx;

import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.effect.Glow;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import model.Board;
import model.BoardNode;
import model.PentagonBoard;
import model.Piece;
import model.Player;
import model.SquareBoard;
import model.HexagonBoard;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BoardPane extends Pane {

    private final Map<String, Image> pawnCache = new HashMap<>();
    private Map<Integer, Point2D> nodePositions = new HashMap<>();
    private Map<String, List<int[]>> boardConnections = defineBoardConnections();
    private final Map<Player, Color> playerColor = new HashMap<>();
    private Consumer<Piece> pieceClickHandler;

    private Board currentBoard;
    private List<Piece> currentPieces;
    private List<Player> players;

    private final Map<Player, Rectangle> waitingAreas = new HashMap<>();
    private final Map<Player, List<Point2D>> waitingSlots = new HashMap<>();
    private final Map<Piece, ImageView> pieceViewMap = new HashMap<>();

    // 각 Piece에 대응하는 ImageView 보관용
    private final Map<Piece, ImageView> pieceNodes = new HashMap<>();
    private Player highlightedPlayer = null;

    public BoardPane() {
        nodePositions = new HashMap<>();
        boardConnections = defineBoardConnections();
        widthProperty().addListener(o -> redraw());
        heightProperty().addListener(o -> redraw());
    }

    public void setOnPieceClick(Consumer<Piece> handler) {
        this.pieceClickHandler = handler;
    }

    public void drawBoard(Board board, List<Piece> pieces, List<Player> players) {
        this.currentBoard = board;
        this.currentPieces = pieces;
        this.players = players;

        // 플레이어 색상 설정
        Color[] palette = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
        for (int i = 0; i < players.size(); i++) {
            playerColor.put(players.get(i), palette[i % palette.length]);
        }

        // 노드 위치 초기화
        if (board instanceof SquareBoard) {
            nodePositions = defineSquarePositions();
        } else if (board instanceof PentagonBoard) {
            nodePositions = definePentagonPositions();
        } else if (board instanceof HexagonBoard) {
            nodePositions = defineHexagonPositions();
        }

        // 말 ImageView 생성 (한 번만)
        for (Piece piece : pieces) {
            pieceNodes.computeIfAbsent(piece, p -> {
                ImageView iv = new ImageView(getPawnImage(getColorForPlayer(p.getOwner())));
                iv.setPreserveRatio(true);
                iv.setFitWidth(30);
                iv.setFitHeight(30);
                iv.setOnMouseClicked(e -> {
                    if (pieceClickHandler != null) pieceClickHandler.accept(p);
                });
                // 초기 위치는 대기 영역에서 redraw로 설정
                getChildren().add(iv);
                return iv;
            });
        }

        initWaitingArea();
        redraw();
    }
    
    public void highlightCurrentPlayer(Player p) {
        this.highlightedPlayer = p;
        redraw();
    }

    private void initWaitingArea() {
        waitingAreas.clear();
        waitingSlots.clear();
        if (players == null) return;

        double w = getWidth();
        double h = getHeight();
        double margin = 20;
        double areaH = 50;
        double areaY = h - areaH - margin;
        double colW = (w - 2 * margin) / players.size();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            double x = margin + i * colW;

            Rectangle rect = new Rectangle(x, areaY, colW - margin, areaH);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.GRAY);
            waitingAreas.put(p, rect);

            long count = currentPieces.stream()
                .filter(pc -> pc.getOwner().equals(p) && pc.getPosition() == null && !pc.isFinished())
                .count();
            double gap = (rect.getWidth()) / (count + 1);
            List<Point2D> slots = new ArrayList<>();
            for (int j = 0; j < count; j++) {
                double sx = x + gap * (j + 1);
                double sy = areaY + areaH / 2;
                slots.add(new Point2D(sx, sy));
            }
            waitingSlots.put(p, slots);
        }
    }

    public void redraw() {
        getChildren().clear();
        if (currentBoard == null) return;
        initWaitingArea();

        // ── 1) 대기 공간 박스 (하이라이트 포함)
        waitingAreas.forEach((p, rect) -> {
            if (p.equals(highlightedPlayer)) {
                rect.setStroke(Color.GOLD);
                rect.setStrokeWidth(4);
                rect.setEffect(new Glow(0.8));
            } else {
                rect.setStroke(Color.GRAY);
                rect.setStrokeWidth(1);
                rect.setEffect(null);
            }
            getChildren().add(rect);
        });

        double w = getWidth(), h = getHeight();
        double scale = Math.min(w, h) / 25.0;
        double ox = (w - scale * 25) / 2 + 70;
        double oy = (h - scale * 25) / 2 + 30;
        String type = currentBoard.getClass().getSimpleName();

        // ── 2) 대기 중인 말
        for (Player p : players) {
            List<Piece> waiting = currentPieces.stream()
                .filter(pc -> pc.getOwner().equals(p)
                           && pc.getPosition() == null
                           && !pc.isFinished())
                .toList();
            List<Point2D> slots = waitingSlots.get(p);

            for (int k = 0; k < waiting.size() && k < slots.size(); k++) {
                Piece piece = waiting.get(k);
                Point2D pt = slots.get(k);

                ImageView iv = pieceNodes.computeIfAbsent(piece, pc -> {
                    ImageView newIv = new ImageView(getPawnImage(playerColor.get(p)));
                    newIv.setPreserveRatio(true);
                    newIv.setFitWidth(30);
                    newIv.setFitHeight(30);
                    newIv.setOnMouseClicked(e -> {
                        if (pieceClickHandler != null) pieceClickHandler.accept(pc);
                    });
                    return newIv;
                });

                if (!getChildren().contains(iv)) getChildren().add(iv);
                animateNode(iv,
                    pt.getX() - iv.getFitWidth()/2,
                    pt.getY() - iv.getFitHeight()/2
                );
            }
        }

        // ── 3) 보드 연결선
        boardConnections.getOrDefault(type, Collections.emptyList())
            .forEach(route -> {
                for (int i = 0; i < route.length - 1; i++) {
                    Point2D a = nodePositions.get(route[i]);
                    Point2D b = nodePositions.get(route[i+1]);
                    if (a == null || b == null) continue;
                    Line line = new Line(
                        ox + a.getX()*scale, oy + a.getY()*scale,
                        ox + b.getX()*scale, oy + b.getY()*scale
                    );
                    line.setStrokeWidth(2);
                    getChildren().add(line);
                }
            });

        // ── 4) 노드
        Set<Integer> specials = SPECIAL_NODES.getOrDefault(type, Set.of());
        for (BoardNode node : currentBoard.getAllNodes()) {
            Point2D pos = nodePositions.get(node.getId());
            if (pos == null) continue;
            double x = ox + pos.getX()*scale, y = oy + pos.getY()*scale;
            if (specials.contains(node.getId())) {
                Circle outer = new Circle(x, y, scale * 0.7);
                outer.setFill(Color.WHITE);
                outer.setStroke(Color.BLACK);
                outer.setStrokeWidth(2);
                Circle inner = new Circle(x, y, scale * 0.5);
                inner.setFill(Color.DARKGRAY);
                getChildren().addAll(outer, inner);
            } else {
                Circle c = new Circle(x, y, scale * 0.6);
                c.setFill(Color.LIGHTGRAY);
                c.setStroke(Color.BLACK);
                getChildren().add(c);
            }
        }

        // ── 5) 보드 위의 말
        Map<BoardNode, List<Piece>> grouped = currentPieces.stream()
            .filter(pc -> !pc.isFinished() && pc.getPosition() != null)
            .collect(Collectors.groupingBy(Piece::getPosition));

        grouped.forEach((node, list) -> {
            Point2D pos = nodePositions.get(node.getId());
            if (pos == null) return;
            double cx = ox + pos.getX()*scale;
            double cy = oy + pos.getY()*scale;

            for (int i = 0; i < list.size(); i++) {
                Piece piece = list.get(i);
                double offset = (i - (list.size()-1)/2.0) * 12;
                double x = cx + offset - /* fitWidth/2 */ 15;
                double y = cy - /* fitHeight/2 */ 15;

                ImageView iv = pieceNodes.computeIfAbsent(piece, pc -> {
                    ImageView newIv = new ImageView(getPawnImage(getColorForPlayer(pc.getOwner())));
                    newIv.setPreserveRatio(true);
                    newIv.setFitWidth(30);
                    newIv.setFitHeight(30);
                    newIv.setOnMouseClicked(e -> {
                        if (pieceClickHandler != null) pieceClickHandler.accept(pc);
                    });
                    return newIv;
                });

                if (!getChildren().contains(iv)) getChildren().add(iv);
                animateNode(iv, x, y);
            }
        });

        // ── 6) 말이 위로 오도록
        pieceNodes.values().forEach(iv -> iv.toFront());
    }

    
    private void animateNode(ImageView iv, double x, double y) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.3), iv);
        tt.setToX(x); tt.setToY(y);
        tt.play();
    }

    private void animate(ImageView iv, double x, double y) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.3), iv);
        tt.setToX(x);
        tt.setToY(y);
        tt.play();
    }
    
    /** 
     * 말(p)가 pathNodes 에 적힌 순서대로 움직이는 애니메이션.
     * 끝나면 onFinished.run() 호출.
     */
    public void animateAlongPath(Piece piece, int steps, Runnable onFinished) {
        // 1) ImageView 꺼내기
        ImageView iv = pieceNodes.get(piece);
        if (iv == null || steps <= 0) {
            onFinished.run();
            return;
        }

        // 2) 모델 src 노드와 경로 리스트 구하기
        BoardNode src = piece.getPosition() == null
            ? currentBoard.getStart()
            : piece.getPosition();
        List<BoardNode> pathNodes = currentBoard.calculatePath(src, steps);

        // 3) Path 만들기
        Path path = new Path();
        // 3-a) 시작 좌표: 현재 iv 의 센터
        Bounds b = iv.localToParent(iv.getBoundsInLocal());
        double startX = b.getMinX() + b.getWidth()/2;
        double startY = b.getMinY() + b.getHeight()/2;
        path.getElements().add(new MoveTo(startX, startY));

        // 3-b) 각 BoardNode 가리키는 화면 좌표
        double w = getWidth(), h = getHeight();
        double scale = Math.min(w, h) / 25.0;
        double ox = (w - scale*25)/2 + 70;
        double oy = (h - scale*25)/2 + 30;

        for (BoardNode bn : pathNodes) {
            Point2D pos = nodePositions.get(bn.getId());
            double x = ox + pos.getX()*scale;
            double y = oy + pos.getY()*scale;
            path.getElements().add(new LineTo(x, y));
        }

        // 4) PathTransition 실행
        PathTransition pt = new PathTransition(
            Duration.seconds(pathNodes.size() * 0.2),
            path,
            iv
        );
        pt.setOnFinished(evt -> {
            // 잠깐 정지 후 콜백
            PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
            pause.setOnFinished(e2 -> onFinished.run());
            pause.play();
        });
        pt.play();
    }
    /** 대기 슬롯 좌표 얻기 (이전 코드 재사용) */
    private Point2D findWaitingSlotFor(Piece piece) {
        Player owner = piece.getOwner();
        List<Piece> waiting = currentPieces.stream()
            .filter(pc -> pc.getOwner().equals(owner)
                       && pc.getPosition()==null
                       && !pc.isFinished())
            .toList();
        int idx = waiting.indexOf(piece);
        List<Point2D> slots = waitingSlots.get(owner);
        idx = Math.max(0, Math.min(idx, slots.size()-1));
        return slots.get(idx);
    }
    
    public void animateFromWaitingTo(int steps,Piece piece,boolean captured,Runnable onFinished) {
    	 // 1) 시작점: 대기 슬롯 좌표
        Point2D startPt = findWaitingSlotFor(piece);

        // 2) 경로용 BoardNode 리스트 얻기
        //    모델 이동은 이미 controller 쪽에서 해 두셨으니,
        //    다시 calculatePath를 써서 동일 경로를 구합니다.
        BoardNode srcNode = null; // 대기였다면 START 노드
        // (대기장이었다면 start 노드, 아니면 piece.getPosition() 직전)
        srcNode = currentBoard.getStart();
        List<BoardNode> pathNodes = currentBoard.calculatePath(srcNode, steps);

        // 3) 화면 좌표용 Path 생성
        double w = getWidth(), h = getHeight();
        double scale = Math.min(w, h) / 25.0;
        double offsetX = (w - scale*25)/2 + 70, offsetY = (h - scale*25)/2 + 30;

        Path path = new Path();
        path.getElements().add(new MoveTo(startPt.getX(), startPt.getY()));
        for (BoardNode bn : pathNodes) {
            Point2D pos = nodePositions.get(bn.getId());
            double x = offsetX + pos.getX()*scale;
            double y = offsetY + pos.getY()*scale;
            path.getElements().add(new LineTo(x, y));
        }

        // 4) ImageView 찾기
        ImageView iv = pieceViewMap.get(piece);
        if (iv == null) {
            // 안전 장치: redraw되지 않았다면 그냥 콜백
            onFinished.run();
            return;
        }

        // 5) PathTransition 생성 & 실행
        PathTransition pt = new PathTransition(Duration.seconds(steps * 0.2), path, iv);
        pt.setOnFinished(e -> {
            // 잠깐 멈춘 뒤에 콜백
            PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
            pause.setOnFinished(ev -> onFinished.run());
            pause.play();
        });
        pt.play();
    }
    
    private Color getColorForPlayer(Player p) {
        return playerColor.getOrDefault(p, Color.GRAY);
    }

    private Image getPawnImage(Color color) {
        return pawnCache.computeIfAbsent(color.toString(), key -> {
            String file = switch (key) {
                case "0xff0000ff" -> "pawn_red.png";
                case "0x0000ffff" -> "pawn_blue.png";
                case "0x008000ff" -> "pawn_green.png";
                default -> "pawn_yellow.png";
            };
            URL url = Objects.requireNonNull(getClass().getResource("/images/" + file));
            return new Image(url.toString());
        });
    }

    private static final Map<String, Set<Integer>> SPECIAL_NODES = Map.of(
        "SquareBoard", Set.of(0, 5, 10, 15, 28),
        "PentagonBoard", Set.of(0, 5, 10, 15, 20, 35),
        "HexagonBoard", Set.of(0, 5, 10, 15, 20, 25, 42)
    );

    private Map<String, List<int[]>> defineBoardConnections() {
        Map<String, List<int[]>> connections = new HashMap<>();
        connections.put("SquareBoard", List.of(
            new int[]{5, 20, 21, 28},new int[]{10, 22, 23, 28},new int[]{15, 25, 24, 28},
            new int[]{0, 27, 26, 28},new int[]{0,1,2,3,4,5},new int[]{5,6,7,8,9,10},
            new int[]{10,11,12,13,14,15},new int[]{15,16,17,18,19,0}
        ));
        connections.put("PentagonBoard", List.of(
            new int[]{15,29,30,35},new int[]{5,25,26,35},new int[]{20,32,31,35},
            new int[]{0,34,33,35},new int[]{10,27,28,35},new int[]{0,1,2,3,4,5},
            new int[]{5,6,7,8,9,10},new int[]{10,11,12,13,14,15},
            new int[]{15,16,17,18,19,20},new int[]{20,21,22,23,24,0}
        ));
        connections.put("HexagonBoard", List.of(
            new int[]{5,30,31,42},new int[]{10,32,33,42},new int[]{15,34,35,42},
            new int[]{20,36,37,42},new int[]{25,39,38,42},new int[]{0,41,40,42},
            new int[]{0,1,2,3,4,5},new int[]{5,6,7,8,9,10},
            new int[]{10,11,12,13,14,15},new int[]{15,16,17,18,19,20},
            new int[]{20,21,22,23,24,25},new int[]{25,26,27,28,29,0}
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
        map.put(10, new Point2D(10.0, 0.0));
        map.put(9, new Point2D(12.0, 1.5));
        map.put(8, new Point2D(14.0, 3.0));
        map.put(7, new Point2D(16.0, 5.0));
        map.put(6, new Point2D(18.0, 7.0));
        map.put(5, new Point2D(20.0, 9.0));
        map.put(4, new Point2D(19.0, 11.0));
        map.put(3, new Point2D(18.0, 13.5));
        map.put(2, new Point2D(17.0, 15.5));
        map.put(1, new Point2D(16.0, 17.5));
        map.put(0, new Point2D(15.0, 19.5));
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
        map.put(35, new Point2D(10.0, 10.5));
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
