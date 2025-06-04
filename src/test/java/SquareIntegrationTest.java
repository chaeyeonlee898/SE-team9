import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import model.*;

@DisplayName("SquareBoard 통합 테스트")
public class SquareIntegrationTest extends AbstractBoardTest {

    @Override
    protected Board createBoard() {
        return new SquareBoard();
    }

    @Override
    protected int numberOfPlayers() {
        return 2;   // 플레이어 2명
    }

    @Override
    protected int piecesPerPlayer() {
        return 5;   // 플레이어당 피스 5개
    }

    @Test
    @DisplayName("교차 플레이어 잡기 및 위치 초기화 테스트")
    void crossPlayerCapture() {
        // ─ P1.M0: YUT(5) 이동 → Node 5에 위치 ─
        applyOne(1, 0, YutResult.YUT.getStepCount());
        assertNotNull(piece(1, 0).getPosition(), "P1.M0은 이동해서 Node 5에 있어야 한다");

        // ─ P0.M0: YUT(5) 이동 → 같은 Node 5에 가서 P1.M0 잡기 ─
        applyOne(0, 0, YutResult.YUT.getStepCount());
        assertNull(piece(1, 0).getPosition(), "P1.M0은 잡혀서 집(null)이어야 한다");
    }

    @Test
    @DisplayName("완주된 말 이동 방지 테스트")
    void preventMoveAfterFinish() {
        // ─ P0.M1: YUT(5) → MO(4) → DO(1) → GEOL(3) → GEOL(3) → YUT(5) 순서로 완주 ─
        applyOne(0, 1,
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.YUT.getStepCount()
        );
        assertTrue(piece(0, 1).isFinished(), "P0.M1은 완주되어야 한다");
    }

    @Test
    @DisplayName("그룹화 후 부분 잡힘(De-Group) 테스트")
    void groupAndDeGroupCapture() {
        // ─ P0.M0, P0.M1: 둘 다 YUT(5) 이동 → Node 5에서 그룹화 ─
        applyOne(0, 0, YutResult.YUT.getStepCount());
        applyOne(0, 1, YutResult.YUT.getStepCount());
        assertEquals(
                piece(0, 0).getPosition(),
                piece(0, 1).getPosition(),
                "P0.M0과 P0.M1은 같은 위치(Node 5)에 있어야 한다"
        );

        // ─ P1.M0: YUT(5) 이동 → Node 5로 이동, P0의 그룹(두 말) 전체 잡기 ─
        applyOne(1, 0, YutResult.YUT.getStepCount());
        assertNull(piece(0, 0).getPosition(), "P0.M0은 잡혀서 집(null)이어야 한다");
        assertNull(piece(0, 1).getPosition(), "P0.M1은 잡혀서 집(null)이어야 한다");
    }

    @Test
    @DisplayName("isFinished() 상태 반환 테스트")
    void isFinishedStatus() {
        // ─ P0.M2: 완주 로직 수행 (YUT→MO→DO→GEOL→GEOL→YUT) ─
        applyOne(0, 2,
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.YUT.getStepCount()
        );
        assertTrue(piece(0, 2).isFinished(), "P0.M2은 완주되어야 한다");

        // ─ 아직 이동하지 않은 P0.M3은 isFinished == false ─
        assertFalse(piece(0, 3).isFinished(), "P0.M3은 아직 완주되지 않아야 한다");
    }
}
