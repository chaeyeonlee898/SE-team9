import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HexagonBoard 교차점별 이동 로직 테스트")
public class HexagonBoardTest extends AbstractBoardTest {

    @Override
    protected Board createBoard() {
        return new HexagonBoard();
    }

    /** 0(모)->5(걸)->42(윷)->0(완주)  >> 교차점 5 & 교차점 42 (1번째 path) */
    @Test @DisplayName("교차점 5 & 교차점 42 (1번째 path)")
    void hexagonA() {
        apply(
                YutResult.MO .getStepCount(),  // 5
                YutResult.GEOL.getStepCount(), // 3
                YutResult.YUT.getStepCount()   // 4
        );
        assertTrue(piece.finished, "모→걸→윷 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(모)->5(모)->39(윷)->28(걸)->0(완주)  >> 교차점 5 (2번째 path) */
    @Test @DisplayName("교차점 5 (2번째 path)")
    void hexagonB() {
        apply(
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.YUT.getStepCount(), // 4
                YutResult.GEOL.getStepCount() // 3
        );
        assertTrue(piece.finished, "모→모→윷→걸 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(도)->10(걸)->42(윷)->0(완주)  >> 교차점 10 & 교차점 42 (1번째 path) */
    @Test @DisplayName("교차점 10 & 교차점 42 (1번째 path)")
    void hexagonC() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.DO .getStepCount(), // 1
                YutResult.GEOL.getStepCount(),// 3
                YutResult.YUT.getStepCount()  // 4
        );
        assertTrue(piece.finished, "윷→모→도→걸→윷 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(도)->10(모)->39(모)->29(개)->0(완주)  >> 교차점 10 (2번째 path) */
    @Test @DisplayName("교차점 10 (2번째 path)")
    void hexagonD() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.DO .getStepCount(), // 1
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.GAE.getStepCount()  // 2
        );
        assertTrue(piece.finished, "윷→모→도→모→모→개 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(도)->15(걸)->42(윷)->0(완주)  >> 교차점 15 & 교차점 42 (1번째 path) */
    @Test @DisplayName("교차점 15 & 교차점 42 (1번째 path)")
    void hexagonE() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.DO .getStepCount(), // 1
                YutResult.GEOL.getStepCount(),// 3
                YutResult.YUT.getStepCount()  // 4
        );
        assertTrue(piece.finished, "윷→모→모→도→걸→윷 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(도)->15(모)->39(모)->29(개)->0(완주)  >> 교차점 15 (2번째 path) */
    @Test @DisplayName("교차점 15 (2번째 path)")
    void hexagonF() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.DO .getStepCount(), // 1
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.GAE.getStepCount()  // 2
        );
        assertTrue(piece.finished, "윷→모→모→도→모→모→개 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(개)->20(걸)->42(윷)->0(완주)  >> 교차점 20 & 교차점 42 (1번째 path) */
    @Test @DisplayName("교차점 20 & 교차점 42 (1번째 path)")
    void hexagonG() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.YUT.getStepCount(), // 4
                YutResult.GAE.getStepCount(), // 2
                YutResult.GEOL.getStepCount(),// 3
                YutResult.YUT.getStepCount()  // 4
        );
        assertTrue(piece.finished, "윷→모→모→윷→개→걸→윷 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(개)->20(모)->39(모)->29(개)->0(완주)  >> 교차점 20 (2번째 path) */
    @Test @DisplayName("교차점 20 (2번째 path)")
    void hexagonH() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.YUT.getStepCount(), // 4
                YutResult.GAE.getStepCount(), // 2
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.GAE.getStepCount()  // 2
        );
        assertTrue(piece.finished, "윷→모→모→윷→개→모→모→개 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(모)->23(개)->25(모)->0(도)->0(완주)  >> 교차점 25 (외곽 path) */
    @Test @DisplayName("교차점 25 (외곽 path)")
    void hexagonI() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.GAE.getStepCount(), // 2
                YutResult.MO .getStepCount(), // 5
                YutResult.DO .getStepCount()  // 1
        );
        assertTrue(piece.finished, "윷→모→모→윷→모→개→모→도 순서로 던지면 완주되어야 합니다.");
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(모)->23(모)->28(걸)->0(완주)  >> 교차점 X (외곽 path) */
    @Test @DisplayName("교차점 X (외곽 path)")
    void hexagonJ() {
        apply(
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.YUT.getStepCount(), // 4
                YutResult.MO .getStepCount(), // 5
                YutResult.MO .getStepCount(), // 5
                YutResult.GEOL.getStepCount() // 3
        );
        assertTrue(piece.finished, "윷→모→모→윷→모→모→걸 순서로 던지면 완주되어야 합니다.");
    }
}
