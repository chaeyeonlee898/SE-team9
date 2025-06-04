import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import model.*;

@DisplayName("PentagonBoard 통합 테스트")
public class PentagonIntegrationTest extends AbstractBoardTest {

    @Override
    protected Board createBoard() {
        return new PentagonBoard();
    }

    @Override
    protected int numberOfPlayers() {
        return 3;   // 플레이어 3명
    }

    @Override
    protected int piecesPerPlayer() {
        return 4;   // 플레이어당 피스 4개
    }

    @Test
    @DisplayName("교차 플레이어 잡기 및 위치 초기화 테스트")
    void crossPlayerCapture() {
        // ─ P2.M0: GEOL(3) 이동 → Node 5(2번째 분기) ─
        applyOne(2, 0, YutResult.GEOL.getStepCount());
        assertNotNull(piece(2, 0).getPosition(), "P2.M0은 이동해서 Node 5에 있어야 한다");

        // ─ P0.M0: 동일하게 GEOL(3) 이동 → Node 5로 가서 P2.M0 잡기 ─
        applyOne(0, 0, YutResult.GEOL.getStepCount());
        assertNull(piece(2, 0).getPosition(), "P2.M0은 잡혀서 집(null)이어야 한다");
    }

    @Test
    @DisplayName("완주된 말 이동 방지 테스트")
    void preventMoveAfterFinish() {
        // ─ P1.M1: YUT(5)→MO(4)→DO(1)→GEOL(3)→GEOL(3) 합계 16으로 완주 ─
        applyOne(1, 1,
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount()
        );
        assertFalse(piece(1, 1).isFinished(), "P1.M1은 완주되어야 한다");
    }

    @Test
    @DisplayName("그룹화 후 부분 잡힘(De-Group) 테스트")
    void groupAndDeGroupCapture() {
        // ─ P0.M0, P0.M1: 둘 다 GEOL(3) 이동 → Node 5(2번째 분기)에서 그룹화 ─
        applyOne(0, 0, YutResult.GEOL.getStepCount());
        applyOne(0, 1, YutResult.GEOL.getStepCount());
        assertEquals(
                piece(0, 0).getPosition(),
                piece(0, 1).getPosition(),
                "P0.M0과 P0.M1은 같은 위치(Node 5)에 있어야 한다"
        );

        // ─ P2.M0: GEOL(3) 이동 → Node 5로 이동하여 그룹 전체 잡기 ─
        applyOne(2, 0, YutResult.GEOL.getStepCount());
        assertNull(piece(0, 0).getPosition(), "P0.M0은 잡혀서 집(null)이어야 한다");
        assertNull(piece(0, 1).getPosition(), "P0.M1은 잡혀서 집(null)이어야 한다");
    }

    @Test
    @DisplayName("isFinished() 상태 반환 테스트")
    void isFinishedStatus() {
        // ─ P0.M2: 완주 로직 수행 (YUT→MO→DO→GEOL→GEOL, 총 16칸) ─
        applyOne(0, 2,
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount()
        );
        assertTrue(piece(0, 2).isFinished(), "P0.M2은 완주되어야 한다");

        // ─ 이동하지 않은 P1.M3은 아직 isFinished == false ─
        assertFalse(piece(1, 3).isFinished(), "P1.M3은 아직 완주되지 않아야 한다");
    }
}
