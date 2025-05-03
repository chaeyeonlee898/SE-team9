import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.util.*;

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
        piece = player.pieces.get(0);
        scanner = new Scanner(System.in);
    }

    @Test
    void testInitialPositionIsNull() {
        assertTrue(piece.finished);
    }

    @Test
    void testMoveOneStepFromStart() {
        board3.movePiece(piece, 1, scanner);
        assertNotNull(piece.position);
    }

    @Test
    void testBackdoWithoutStartFails() {
        boolean result = board3.movePiece(piece, -1, scanner);
        assertFalse(result);
        assertNull(piece.position);
    }

    @Test
    void testBackdoThenForwardCompletesLap() {
        board3.movePiece(piece, 1, scanner);   // 0 → 1
        board3.movePiece(piece, -1, scanner);  // 1 → 0 (출발점)
        board3.movePiece(piece, 1, scanner);   // 0 → 1 (한 칸 더 이동)
        assertTrue(piece.finished);           // 완주 처리됨
    }

    @Test
    void testCaptureEnemyPiece() {
        Player enemy = new Player("Enemy", 1, board3);
        Piece enemyPiece = enemy.pieces.get(0);
        board3.movePiece(enemyPiece, 2, scanner);
        board3.movePiece(piece, 2, scanner);

    }

    @Test
    void testStackMoveTogether() {
        player = new Player("Player1", 2, board3);
        Piece p1 = player.pieces.get(0);
        Piece p2 = player.pieces.get(1);

        board3.movePiece(p1, 1, scanner);  // 0 → 1
        board3.movePiece(p2, 1, scanner);  // p2도 1번으로 이동 (업)
        board3.movePiece(p1, 2, scanner);  // 1 → 2 → 3

        assertNotNull(p1.position, "p1.position이 null입니다.");
        assertNotNull(p2.position, "p2.position이 null입니다.");
        assertEquals(p1.position, p2.position, "p1과 p2는 같은 위치에 있어야 합니다.");
        assertEquals("3", p1.position.name, "말의 위치는 3번 노드여야 합니다.");
    }

}
