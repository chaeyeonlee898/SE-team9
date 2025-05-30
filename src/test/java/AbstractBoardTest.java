import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Scanner;
import model.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // BeforeAll 사용할 때 static 선언을 회피하기 위해
public abstract class AbstractBoardTest {

    protected Board board;
    protected Game game;
    protected List<Player> players;
    protected Player player;
    protected List<Piece> pieces;
    protected Scanner scanner;

    /** 서브클래스에서 보드 인스턴스를 리턴하세요 */
    protected abstract Board createBoard();

    protected abstract int numberOfPlayers();

    /** 서브클래스에서 말 개수를 리턴하세요 */
    protected abstract int piecesPerPlayer();

    @BeforeEach
    void setUp() {
        board   = createBoard();
        game     = new Game(numberOfPlayers(), piecesPerPlayer(), board);
        players  = game.getPlayers();          // Game 내부의 players 리스트
        player  = new Player("P", 1, board);
        scanner = new Scanner("");
    }

    protected Player player(int idx) {
        return game.getPlayers().get(idx);
    }

    protected Piece piece(int playerIdx, int pieceIdx) {
        return player(playerIdx).getPieces().get(pieceIdx);
    }

    /** 여러 스텝을 연속으로 적용할 때 편하게 쓰는 헬퍼 */
    protected void apply(int... steps) {
        applyOne(0, 0, steps);
    }


    protected void applyOne(int playerIdx, int pieceIdx, int... steps) {
        Piece p = players.get(playerIdx).getPieces().get(pieceIdx);
        for (int s : steps) {
            board.movePiece(p, s, scanner);
        }
    }
}