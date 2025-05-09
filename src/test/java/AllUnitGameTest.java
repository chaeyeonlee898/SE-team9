import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.util.*;
import model.*;
/*
순수 로직에 대한 단위 테스트들 통합한 class 입니다.
임시 디버깅 용으로 만들어서 test case 들이 굉장히 적습니다. (계속해서 추가해야합니다..)
기능을 구현하신 분이 해당 기능의 test case 까지 추가하면 될 것 같습니다.

지금은 통합 class 에서 단위 테스트를 진행하지만 기능별로 test class 를 분리하는게 좋다? 고 합니다.
추후, test class 를 분리한다면 기능 branch 이름에 맞추어서 +Test 붙여서 class 만들면 될 것 같습니다.
*/

@DisplayName("윷놀이 게임 테스트 (임시)") // 순수 로직에 대한 단위 테스트들 통합, 추후 기능 별 테스트로 확장
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // BeforeAll 사용할 때 static 선언을 회피하기 위해
public class AllUnitGameTest {


    HexagonBoard board3;
    Player player;
    Piece piece;
    Scanner scanner;


    @BeforeAll
    void setUp() {

        board3 = new HexagonBoard();
        player = new Player("Player1", 2, board3);
        piece = player.getPieces().get(0);
        scanner = new Scanner(System.in);
    }

    @Test
    void testInitialPositionIsNull() {
        assertTrue(piece.isFinished());
    }

    @Test
    void testMoveOneStepFromStart() {
        board3.movePiece(piece, 1, scanner);
        assertNotNull(piece.getPosition());
    }

    @Test
    void testBackdoWithoutStartFails() {
        boolean result = board3.movePiece(piece, -1, scanner);
        assertFalse(result);
        assertNull(piece.getPosition());
    }

    @Test
    void testBackdoThenForwardCompletesLap() {
        board3.movePiece(piece, 1, scanner);   // 0 → 1
        board3.movePiece(piece, -1, scanner);  // 1 → 0 (출발점)
        board3.movePiece(piece, 1, scanner);   // 0 → 1 (한 칸 더 이동)
        assertTrue(piece.isFinished());           // 완주 처리됨
    }

    @Test
    void testCaptureEnemyPiece() {
        Player enemy = new Player("Enemy", 1, board3);
        Piece enemyPiece = enemy.getPieces().get(0);
        board3.movePiece(enemyPiece, 2, scanner);
        board3.movePiece(piece, 2, scanner);

    }

    @Test
    void testStackMoveTogether() {
        player = new Player("Player1", 2, board3);
        Piece p1 = player.getPieces().get(0);
        Piece p2 = player.getPieces().get(1);

        board3.movePiece(p1, 1, scanner);  // 0 → 1
        board3.movePiece(p2, 1, scanner);  // p2도 1번으로 이동 (업)
        board3.movePiece(p1, 2, scanner);  // 1 → 2 → 3

        assertNotNull(p1.getPosition(), "p1.position이 null입니다.");
        assertNotNull(p2.getPosition(), "p2.position이 null입니다.");
        assertEquals(p1.getPosition(), p2.getPosition(), "p1과 p2는 같은 위치에 있어야 합니다.");
        assertEquals("3", p1.getPosition().getName(), "말의 위치는 3번 노드여야 합니다.");
    }

    @Test
    void testStartPointBaekDo() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 1, scanner);  // 도 -> 1
        board.movePiece(piece, -1, scanner);  // 빽도 -> 0
        assertEquals(0, piece.getPosition().getId(), "현재 위치는 2이어야 합니다.");
        board.movePiece(piece, -1, scanner);  // 빽도 → 1
        assertEquals(1, piece.getPosition().getId(), "현재 위치는 2이어야 합니다.");
        board.movePiece(piece, 2, scanner);  // 개 → 3
        board.movePiece(piece, -1, scanner);  // 빽도 → 2

        assertFalse(piece.isFinished(), "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(2, piece.getPosition().getId(), "최종 위치는 2이어야 합니다.");
    }

    @Test
    void testStartPointBaekDo2() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 1, scanner);  // 도 -> 1
        board.movePiece(piece, -1, scanner);  // 빽도 -> 0
        assertEquals(0, piece.getPosition().getId(), "현재 위치는 0이어야 합니다.");
        board.movePiece(piece, -1, scanner);  // 빽도 → 1
        assertEquals(1, piece.getPosition().getId(), "현재 위치는 1이어야 합니다.");
        board.movePiece(piece, -1, scanner);  // 빽도 → 0
        assertEquals(0, piece.getPosition().getId(), "현재 위치는 0이어야 합니다.");
        board.movePiece(piece, 2, scanner);  // 개 → 완주

        assertTrue(piece.isFinished(), "이 시나리오에서는 완주되어야 합니다.");
    }

    // ────────────────────────────────────────────────────────────
    // 사각형 판 시나리오
    // ────────────────────────────────────────────────────────────

    /** a. 0(윷)→4(개)→6(윷)→10(윷)→26(걸)→완주 */
    @Test
    void testSquareScenarioA() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 4, scanner);  // 윷
        board.movePiece(piece, 2, scanner);  // 개
        board.movePiece(piece, 4, scanner);  // 윷
        board.movePiece(piece, 4, scanner);  // 윷
        board.movePiece(piece, 3, scanner);  // 걸

        assertTrue(piece.isFinished(), "말이 완주되어야 합니다.");
    }

    /** b. 0(윷)→4(윷)→8(개)→10(윷)→26 (미완주) */
    @Test
    void testSquareScenarioB() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 4, scanner);  // 윷 → 4
        board.movePiece(piece, 4, scanner);  // 윷 → 8
        board.movePiece(piece, 2, scanner);  // 개 → 10
        board.movePiece(piece, 4, scanner);  // 윷 → 26

        assertFalse(piece.isFinished(), "완주되지 않아야 합니다.");
        assertEquals(26, piece.getPosition().getId(), "최종 위치는 26이어야 합니다.");
    }

    /** c. 0(윷)→4(윷)→8(윷)→12(걸)→15(걸)→18 */
    @Test
    void testSquareScenarioC() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 4, scanner);  // 윷 → 4
        board.movePiece(piece, 4, scanner);  // 윷 → 8
        board.movePiece(piece, 4, scanner);  // 윷 → 12
        board.movePiece(piece, 3, scanner);  // 걸 → 15
        board.movePiece(piece, 3, scanner);  // 걸 → 18

        assertFalse(piece.isFinished(), "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(18, piece.getPosition().getId(), "최종 위치는 18이어야 합니다.");
    }

    /** d. 0(모) -> 5(걸) -> 28(걸) -> 0(개) -> 완주 */
    @Test
    void testSquareScenarioD() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        // 모(5)
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);
        assertEquals("5", p.getPosition().getName());

        // 걸(3) → 28
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("28", p.getPosition().getName());

        // 걸(3) → 0 (완주 직전)
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("0", p.getPosition().getName());

        // 개(2) → 완주
        board.movePiece(p, YutResult.GAE.getStepCount(), scanner);
        assertTrue(p.isFinished(), "개를 던진 후 완주되어야 합니다.");
    }

    /** e. 0(모) -> 5(윷) -> 24(걸) -> 16 */
    @Test
    void testSquareScenarioE() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        // 모(5) → 5
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);
        assertEquals("5", p.getPosition().getName());

        // 윷(4) → 24 (5번에서 숏컷)
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);
        assertEquals("24", p.getPosition().getName());

        // 걸(3) → 16
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("16", p.getPosition().getName());
    }

    // ────────────────────────────────────────────────────────────
    // 오각형 판 시나리오
    // ────────────────────────────────────────────────────────────

    /** a. 0(모)→5(개)→26(걸)→32 */
    @Test
    void testPentagonScenarioA() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 5, scanner);  // 모 → 5
        board.movePiece(piece, 2, scanner);  // 개 → 26
        board.movePiece(piece, 3, scanner);  // 걸 → 32

        assertFalse(piece.isFinished(), "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(32, piece.getPosition().getId(), "최종 위치는 32이어야 합니다.");
    }

    /** b. 0(모)→5(걸)→35(걸)→0 */
    @Test
    void testPentagonScenarioB() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 5, scanner);  // 모 → 5
        board.movePiece(piece, 3, scanner);  // 걸 → 35
        board.movePiece(piece, 3, scanner);  // 걸 → 0

        assertFalse(piece.isFinished(), "완주 플래그는 지나치기 전까지는 켜지지 않습니다.");
        assertEquals(board.getStart(), piece.getPosition(), "최종 위치는 출발점이어야 합니다.");
    }

    /** c. 0(모)→5(윷)→31(걸)→21 */
    @Test
    void testPentagonScenarioC() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.getPieces().get(0);

        board.movePiece(piece, 5, scanner);  // 모 → 5
        board.movePiece(piece, 4, scanner);  // 윷 → 31
        board.movePiece(piece, 3, scanner);  // 걸 → 21

        assertFalse(piece.isFinished(), "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(21, piece.getPosition().getId(), "최종 위치는 21이어야 합니다.");
    }

    /** d. 0(윷) -> 4(걸) -> 7(걸) -> 10(개) -> 28(걸) -> 32 */
    @Test
    void testPentagonScenarioD() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        // 윷(4) → 4
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);
        assertEquals("4", p.getPosition().getName());

        // 걸(3) → 7
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("7", p.getPosition().getName());

        // 걸(3) → 10
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("10", p.getPosition().getName());

        // 개(2) → 28 (10번에서 숏컷으로 중앙 진입 → 28)
        board.movePiece(p, YutResult.GAE.getStepCount(), scanner);
        assertEquals("28", p.getPosition().getName());

        // 걸(3) → 32
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("32", p.getPosition().getName());
    }

    /** e. 0(윷) -> 4(걸) -> 7(걸) -> 10(걸) -> 35(걸) -> 0 */
    @Test
    void testPentagonScenarioE() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        // 윷(4) → 4
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);
        // 걸(3) → 7
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        // 걸(3) → 10
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        // 걸(3) → 35
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("35", p.getPosition().getName());
        // 걸(3) → 0
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        // 걸(3) → 완주
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertTrue(p.isFinished());
    }

    /** f. 0(윷) -> 4(걸) -> 7(걸) -> 10(윷) -> 31 */
    @Test
    void testPentagonScenarioF() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // →4
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);  // →7
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);  // →10
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // →31 (10번 숏컷 중앙 진입 후)
        assertEquals("31", p.getPosition().getName());
    }

    /** g. 0(윷) -> 4(모) -> 9(모) -> 14(도) -> 15(윷) -> 31 */
    @Test
    void testPentagonScenarioG() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // 윷 →4
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →9
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →14
        board.movePiece(p, YutResult.DO.getStepCount(), scanner);    // 도 →15
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // 윷 →31
        assertEquals("31", p.getPosition().getName());
    }

    /** h. 0(윷) -> 4(모) -> 9(모) -> 14(도) -> 15(걸) -> 35(개) -> 34 */
    @Test
    void testPentagonScenarioH() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.getPieces().get(0);

        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // 윷 →4
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →9
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →14
        board.movePiece(p, YutResult.DO.getStepCount(), scanner);    // 도 →15
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);  // 걸 → central to 35
        board.movePiece(p, YutResult.GAE.getStepCount(), scanner);   // 개 → 34
        assertEquals("34", p.getPosition().getName());
    }

}
