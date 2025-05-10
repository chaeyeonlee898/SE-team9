import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.util.Scanner;
<<<<<<< HEAD
=======
import model.*;
>>>>>>> b131ef782f8106be45e3c30349bf9793b2b618ed

@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // BeforeAll 사용할 때 static 선언을 회피하기 위해
public abstract class AbstractBoardTest {

    protected Board board;
    protected Player player;
    protected Piece piece;
    protected Scanner scanner;

    /** 서브클래스에서 보드 인스턴스를 리턴하세요 */
    protected abstract Board createBoard();

    @BeforeEach
    void setUp() {
        board   = createBoard();
        player  = new Player("P", 1, board);
<<<<<<< HEAD
        piece   = player.pieces.get(0);
=======
        piece   = player.getPieces().get(0);
>>>>>>> b131ef782f8106be45e3c30349bf9793b2b618ed
        scanner = new Scanner("");
    }

    /** 여러 스텝을 연속으로 적용할 때 편하게 쓰는 헬퍼 */
    protected void apply(int... steps) {
        for (int s : steps) {
            board.movePiece(piece, s, scanner);
        }
    }
}
