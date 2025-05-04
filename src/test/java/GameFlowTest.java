import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Scanner;

/*
게임 전체 flow 시나리오 에 대한 통합 테스트로 사용할 class 입니다!
이 부분은 단순 단위 테스트가 아니어서 main 폴더의 class 들 입력 방식을 수정해야 하기 때문에
시험 끝난 후.. 진행하겠습니다.
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameFlowTest {

    private Scanner scanner;

    @BeforeEach
    void setUp() {
        // movePiece 메서드 파라미터용 Scanner
        scanner = new Scanner(System.in);
    }

    @AfterEach
    void tearDown() {
        scanner.close();
    }

    // ────────────────────────────────────────────────────────────
    // 사각형 판 시나리오
    // ────────────────────────────────────────────────────────────

    /** a. 0(윷)→4(개)→6(윷)→10(윷)→26(걸)→완주 */
    @Test
    void testSquareScenarioA() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.pieces.get(0);

        board.movePiece(piece, 4, scanner);  // 윷
        board.movePiece(piece, 2, scanner);  // 개
        board.movePiece(piece, 4, scanner);  // 윷
        board.movePiece(piece, 4, scanner);  // 윷
        board.movePiece(piece, 3, scanner);  // 걸

        assertTrue(piece.finished, "말이 완주되어야 합니다.");
    }

    /** b. 0(윷)→4(윷)→8(개)→10(윷)→26 (미완주) */
    @Test
    void testSquareScenarioB() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.pieces.get(0);

        board.movePiece(piece, 4, scanner);  // 윷 → 4
        board.movePiece(piece, 4, scanner);  // 윷 → 8
        board.movePiece(piece, 2, scanner);  // 개 → 10
        board.movePiece(piece, 4, scanner);  // 윷 → 26

        assertFalse(piece.finished, "완주되지 않아야 합니다.");
        assertEquals(26, piece.position.id, "최종 위치는 26이어야 합니다.");
    }

    /** c. 0(윷)→4(윷)→8(윷)→12(걸)→15(걸)→18 */
    @Test
    void testSquareScenarioC() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.pieces.get(0);

        board.movePiece(piece, 4, scanner);  // 윷 → 4
        board.movePiece(piece, 4, scanner);  // 윷 → 8
        board.movePiece(piece, 4, scanner);  // 윷 → 12
        board.movePiece(piece, 3, scanner);  // 걸 → 15
        board.movePiece(piece, 3, scanner);  // 걸 → 18

        assertFalse(piece.finished, "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(18, piece.position.id, "최종 위치는 18이어야 합니다.");
    }

    /** d. 0(모) -> 5(걸) -> 28(걸) -> 0(개) -> 완주 */
    @Test
    void testSquareScenarioD() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        // 모(5)
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);
        assertEquals("5", p.position.name);

        // 걸(3) → 28
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("28", p.position.name);

        // 걸(3) → 0 (완주 직전)
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("0", p.position.name);

        // 개(2) → 완주
        board.movePiece(p, YutResult.GAE.getStepCount(), scanner);
        assertTrue(p.finished, "개를 던진 후 완주되어야 합니다.");
    }

    /** e. 0(모) -> 5(윷) -> 24(걸) -> 16 */
    @Test
    void testSquareScenarioE() {
        SquareBoard board = new SquareBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        // 모(5) → 5
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);
        assertEquals("5", p.position.name);

        // 윷(4) → 24 (5번에서 숏컷)
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);
        assertEquals("24", p.position.name);

        // 걸(3) → 16
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("16", p.position.name);
    }

    // ────────────────────────────────────────────────────────────
    // 오각형 판 시나리오
    // ────────────────────────────────────────────────────────────

    /** a. 0(모)→5(개)→26(걸)→32 */
    @Test
    void testPentagonScenarioA() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.pieces.get(0);

        board.movePiece(piece, 5, scanner);  // 모 → 5
        board.movePiece(piece, 2, scanner);  // 개 → 26
        board.movePiece(piece, 3, scanner);  // 걸 → 32

        assertFalse(piece.finished, "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(32, piece.position.id, "최종 위치는 32이어야 합니다.");
    }

    /** b. 0(모)→5(걸)→35(걸)→0 */
    @Test
    void testPentagonScenarioB() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.pieces.get(0);

        board.movePiece(piece, 5, scanner);  // 모 → 5
        board.movePiece(piece, 3, scanner);  // 걸 → 35
        board.movePiece(piece, 3, scanner);  // 걸 → 0

        assertFalse(piece.finished, "완주 플래그는 지나치기 전까지는 켜지지 않습니다.");
        assertEquals(board.getStart(), piece.position, "최종 위치는 출발점이어야 합니다.");
    }

    /** c. 0(모)→5(윷)→31(걸)→21 */
    @Test
    void testPentagonScenarioC() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece piece = player.pieces.get(0);

        board.movePiece(piece, 5, scanner);  // 모 → 5
        board.movePiece(piece, 4, scanner);  // 윷 → 31
        board.movePiece(piece, 3, scanner);  // 걸 → 21

        assertFalse(piece.finished, "이 시나리오에서는 완주되지 않아야 합니다.");
        assertEquals(21, piece.position.id, "최종 위치는 21이어야 합니다.");
    }

    /** d. 0(윷) -> 4(걸) -> 7(걸) -> 10(개) -> 28(걸) -> 32 */
    @Test
    void testPentagonScenarioD() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        // 윷(4) → 4
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);
        assertEquals("4", p.position.name);

        // 걸(3) → 7
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("7", p.position.name);

        // 걸(3) → 10
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("10", p.position.name);

        // 개(2) → 28 (10번에서 숏컷으로 중앙 진입 → 28)
        board.movePiece(p, YutResult.GAE.getStepCount(), scanner);
        assertEquals("28", p.position.name);

        // 걸(3) → 32
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("32", p.position.name);
    }

    /** e. 0(윷) -> 4(걸) -> 7(걸) -> 10(걸) -> 35(걸) -> 0 */
    @Test
    void testPentagonScenarioE() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        // 윷(4) → 4
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);
        // 걸(3) → 7
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        // 걸(3) → 10
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        // 걸(3) → 35
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertEquals("35", p.position.name);
        // 걸(3) → 0 (완주)
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);
        assertTrue(p.finished);
    }

    /** f. 0(윷) -> 4(걸) -> 7(걸) -> 10(윷) -> 31 */
    @Test
    void testPentagonScenarioF() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // →4
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);  // →7
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);  // →10
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // →31 (10번 숏컷 중앙 진입 후)
        assertEquals("31", p.position.name);
    }

    /** g. 0(윷) -> 4(모) -> 9(모) -> 14(도) -> 15(윷) -> 31 */
    @Test
    void testPentagonScenarioG() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // 윷 →4
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →9
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →14
        board.movePiece(p, YutResult.DO.getStepCount(), scanner);    // 도 →15
        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // 윷 →31
        assertEquals("31", p.position.name);
    }

    /** h. 0(윷) -> 4(모) -> 9(모) -> 14(도) -> 15(걸) -> 35(개) -> 34 */
    @Test
    void testPentagonScenarioH() {
        PentagonBoard board = new PentagonBoard();
        Player player = new Player("P", 1, board);
        Piece p = player.pieces.get(0);

        board.movePiece(p, YutResult.YUT.getStepCount(), scanner);   // 윷 →4
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →9
        board.movePiece(p, YutResult.MO.getStepCount(), scanner);    // 모 →14
        board.movePiece(p, YutResult.DO.getStepCount(), scanner);    // 도 →15
        board.movePiece(p, YutResult.GEOL.getStepCount(), scanner);  // 걸 → central to 35
        board.movePiece(p, YutResult.GAE.getStepCount(), scanner);   // 개 → 34
        assertEquals("34", p.position.name);
    }
}
