package view.javafx;

import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.effect.Glow;
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

        // draw waiting areas with highlight
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

        // draw waiting pieces
        for (Player p : players) {
            List<Piece> waiting = currentPieces.stream()
                .filter(pc->pc.getOwner().equals(p)&&pc.getPosition()==null&&!pc.isFinished())
                .toList();
            List<Point2D> slots = waitingSlots.get(p);
            for (int k = 0; k < waiting.size() && k < slots.size(); k++) {
                Piece piece = waiting.get(k);
                ImageView iv = pieceNodes.get(piece);
                Point2D pt = slots.get(k);
                if (!getChildren().contains(iv)) getChildren().add(iv);
                animateNode(iv, pt.getX()-iv.getFitWidth()/2, pt.getY()-iv.getFitHeight()/2);
            }
        }

        double w = getWidth(), h = getHeight();
        double scale = Math.min(w,h)/25.0;
        double ox = (w-scale*25)/2+70, oy = (h-scale*25)/2+30;
        String type = currentBoard.getClass().getSimpleName();

        // draw connections
        boardConnections.getOrDefault(type, Collections.emptyList())
            .forEach(route-> {
                for (int i=0;i<route.length-1;i++){
                    Point2D a=nodePositions.get(route[i]);
                    Point2D b=nodePositions.get(route[i+1]);
                    if (a==null||b==null) continue;
                    Line line = new Line(ox+a.getX()*scale, oy+a.getY()*scale,
                                          ox+b.getX()*scale, oy+b.getY()*scale);
                    line.setStrokeWidth(2);
                    getChildren().add(line);
                }
            });

        // draw nodes
        Set<Integer> specials = SPECIAL_NODES.getOrDefault(type, Set.of());
        for (BoardNode node : currentBoard.getAllNodes()) {
            Point2D pos = nodePositions.get(node.getId());
            if (pos==null) continue;
            double x = ox+pos.getX()*scale, y = oy+pos.getY()*scale;
            if (specials.contains(node.getId())) {
                Circle outer = new Circle(x,y,scale*0.7);
                outer.setFill(Color.WHITE); outer.setStroke(Color.BLACK); outer.setStrokeWidth(2);
                Circle inner = new Circle(x,y,scale*0.5);
                inner.setFill(Color.DARKGRAY);
                getChildren().addAll(outer, inner);
            } else {
                Circle c = new Circle(x,y,scale*0.6);
                c.setFill(Color.LIGHTGRAY); c.setStroke(Color.BLACK);
                getChildren().add(c);
            }
        }

        // draw moved pieces
        Map<BoardNode, List<Piece>> grouped = currentPieces.stream()
            .filter(pc->!pc.isFinished()&&pc.getPosition()!=null)
            .collect(Collectors.groupingBy(Piece::getPosition));
        grouped.forEach((node,list)->{
            Point2D pos = nodePositions.get(node.getId());
            if (pos==null) return;
            double cx=ox+pos.getX()*scale, cy=oy+pos.getY()*scale;
            for(int i=0;i<list.size();i++){
                Piece piece = list.get(i);
                ImageView iv = pieceNodes.get(piece);
                double off = (i-(list.size()-1)/2.0)*12;
                if (!getChildren().contains(iv)) getChildren().add(iv);
                animateNode(iv, cx+off-iv.getFitWidth()/2, cy-iv.getFitHeight()/2);
            }
        });

        // ensure pieces on top
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
    
    public void animateFromWaitingTo(int destId,Piece piece,boolean captured,Runnable onFinished) {
    	 ImageView iv = pieceNodes.get(piece);
         if (iv == null) return;

         // 1) 시작 노드(0번) 좌표 계산
         Point2D startNode = nodePositions.get(0);
         double w       = getWidth(), h = getHeight();
         double scale   = Math.min(w, h) / 25.0;
         double offsetX = (w - scale*25)/2 + 70;
         double offsetY = (h - scale*25)/2 + 30;
         double midX    = offsetX + startNode.getX()*scale - iv.getFitWidth()/2;
         double midY    = offsetY + startNode.getY()*scale - iv.getFitHeight()/2;

         // 2) 목적지(destId) 좌표 계산
         Point2D destNode = nodePositions.get(destId);
         double endX = offsetX + destNode.getX()*scale - iv.getFitWidth()/2;
         double endY = offsetY + destNode.getY()*scale - iv.getFitHeight()/2;

         // 3) 두 단계 TranslateTransition
         TranslateTransition toStart = new TranslateTransition(Duration.seconds(0.3), iv);
         toStart.setToX(midX); toStart.setToY(midY);

         TranslateTransition toDest = new TranslateTransition(Duration.seconds(0.3), iv);
         toDest.setToX(endX);  toDest.setToY(endY);

         // 4) 순차 실행 + 완료 시 redraw() → onFinished.run()
         SequentialTransition st = new SequentialTransition(iv, toStart, toDest);
         st.setOnFinished(evt -> {
             redraw();
             if (onFinished != null) onFinished.run();
         });
         st.play();
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
