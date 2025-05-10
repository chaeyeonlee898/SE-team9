import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import model.*;
@DisplayName("SquareBoard 교차점별 이동 로직 테스트")
public class SquareBoardTest extends AbstractBoardTest {

    @Override
    protected Board createBoard() {
        return new SquareBoard();
    }

    /** 0(모)->5(걸)->28(개)->27(빽도)->26(걸)->0(완주) + 빽도 수행 */
    @Test @DisplayName("교차점 5 & 교차점 28 (1번째 path)")
    void squareA() {
        apply(
                YutResult.MO .getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GAE .getStepCount(),
                YutResult.BACKDO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GAE .getStepCount()
        );
        assertTrue(piece.isFinished(), "모→걸→개→빽도→걸→개 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(모)->5(모)->25(걸)->17(걸)->0(빽도)->19(개)->0(완주) + 0 에서 빽도 수행 */
    @Test @DisplayName("교차점 5 (2번째 path)")
    void squareB() {
        apply(
                YutResult.MO .getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.BACKDO.getStepCount(),
                YutResult.GAE .getStepCount(),
                YutResult.YUT .getStepCount()
        );
        assertTrue(piece.isFinished(), "모→모→걸→걸→빽도→개→윷 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)→4(모)→9(도)→10(걸)→28(걸)→0(미완주) + 0 지점에 도착시 미완주 */
    @Test @DisplayName("교차점 10 & 교차점 28 (1번째 path)")
    void squareC() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.DO .getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount()
        );
        assertFalse(piece.isFinished(), "윷→모→도→걸→걸 순서로 던지면 아직 완주되지 않아야 합니다.");
        assertEquals(0, piece.getPosition().getId(), "0(출발점)에 멈춰도 미완주 상태여야 합니다.");
    }

    /** 0(윷)->4(모)->9(도)->10(모)->25(모)->19(개)->0(완주) */
    @Test @DisplayName("교차점 10 (2번째 path)")
    void squareD() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.DO .getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.GAE.getStepCount(),
                YutResult.YUT.getStepCount()
        );
        assertTrue(piece.isFinished(), "윷→모→도→모→모→개→윷 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(도)->15(모)->0(개)->0(완주) + 0 지점 도착 후 positive step (완주) */
    @Test @DisplayName("교차점 15 (외곽 path)")
    void squareE() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.DO .getStepCount(),
                YutResult.MO .getStepCount(),
                YutResult.GAE.getStepCount()
        );
        assertTrue(piece.isFinished(), "윷→모→모→도→모→개 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(걸)->0(완주) */
    @Test @DisplayName("교차점 X (외곽 path)")
    void squareF() {
        apply(
                YutResult.YUT.getStepCount(),   // 4
                YutResult.MO .getStepCount(),   // 5
                YutResult.MO .getStepCount(),   // 5
                YutResult.YUT.getStepCount(),   // 4
                YutResult.GEOL.getStepCount()   // 3 -> 0 (완주)
        );
        assertTrue(piece.isFinished(), "윷→모→모→윷→걸 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(도)->1(빽도)->0(빽도)->1(빽도)->0(개)->0(완주) */
    @Test @DisplayName("squareG: BACKDO 이동")
    void squareG() {
        // 도(1) → 1
        apply(YutResult.DO.getStepCount());
        assertEquals(1, piece.getPosition().getId(), "도 후 위치는 1이어야 합니다.");
        // 빽도 → 0
        apply(YutResult.BACKDO.getStepCount());
        assertEquals(0, piece.getPosition().getId(), "빽도 후 위치는 0이어야 합니다.");
        // 빽도 → 최종 이력 따라 1
        apply(YutResult.BACKDO.getStepCount());
        assertEquals(1, piece.getPosition().getId(), "연속 빽도 후 위치는 1이어야 합니다.");
        // 빽도 → 0
        apply(YutResult.BACKDO.getStepCount());
        assertEquals(0, piece.getPosition().getId(), "다시 빽도 후 위치는 0이어야 합니다.");
        // 개(2) → 마지막 완주
        apply(YutResult.GAE.getStepCount());
        assertTrue(piece.isFinished(), "개 후 완주되어야 합니다.");
    }
}
