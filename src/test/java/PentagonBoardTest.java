import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import model.*;
@DisplayName("PentagonBoard 교차점별 이동 로직 테스트")
public class PentagonBoardTest extends AbstractBoardTest {

    @Override
    protected Board createBoard() {
        return new PentagonBoard();
    }

    @Override
    protected int numberOfPlayers() {
        return 1;
    }

    @Override
    protected int piecesPerPlayer() {
        return 1;
    }

    /** 0(모)->5(걸)->35(윷)->0(완주) + 윷 이후 완주일 때 추가 윷 던지기 발생 X 확인 */
    @Test @DisplayName("교차점 5 & 교차점 35 (1번째 path)")
    void pentagonA() {
        apply(
                YutResult.MO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.YUT.getStepCount()
        );
        assertTrue(piece(0,0).isFinished());
    }

    /** 0(모)->5(모)->32(윷)->23(걸)->0(완주) */
    @Test @DisplayName("교차점 5 (2번째 path)")
    void pentagonB() {
        apply(
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.YUT.getStepCount()
        );
        assertEquals(23, piece(0,0).getPosition().getId(), "말의 위치는 23 이어야 합니다.");
        apply(YutResult.GEOL.getStepCount());
        assertTrue(piece(0,0).isFinished());
    }

    /** 0(윷)->4(모)->9(도)->10(걸)->35(걸)->0(빽도)->34(미완주) */
    @Test @DisplayName("교차점 10 & 교차점 35 (1번째 path)")
    void pentagonC() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.BACKDO.getStepCount()
        );
        assertEquals(34, piece(0,0).getPosition().getId(), "말의 위치는 34 이어야 합니다.");
        assertFalse(piece(0,0).isFinished());
    }

    /** 0(윷)->4(모)->9(도)->10(모)->32(모)->24(도)->0(빽도)->24(미완주) */
    @Test @DisplayName("교차점 10 (2번째 path)")
    void pentagonD() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.BACKDO.getStepCount()
        );
        assertEquals(24, piece(0,0).getPosition().getId(), "말의 위치는 24 이어야 합니다.");
        assertFalse(piece(0,0).isFinished());
    }

    /** 0(윷)->4(모)->9(모)->14(도)->15(걸)->35(걸)->0(개)->0(완주) */
    @Test
    @DisplayName("교차점 15 & 교차점 35 (1번째 path)")
    void pentagonE() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.GEOL.getStepCount(),
                YutResult.GEOL.getStepCount()
        );
        assertEquals(0, piece(0,0).getPosition().getId(), "말의 위치는 0 이어야 합니다.");
        apply(YutResult.GAE.getStepCount());
        assertTrue(piece(0,0).isFinished());
    }

    /** 0(윷)->4(모)->9(모)->14(도)->15(모)->32(모)->24(개)->0(완주) */
    @Test @DisplayName("교차점 15 (2번째 path)")
    void pentagonF() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.DO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount()
        );
        assertEquals(24, piece(0,0).getPosition().getId(), "말의 위치는 24 이어야 합니다.");
        apply(YutResult.GAE.getStepCount());

        assertTrue(piece(0,0).isFinished());
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(개)->20(모)->0(미완주) */
    @Test @DisplayName("교차점 20 (외곽 path)")
    void pentagonG() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.YUT.getStepCount(),
                YutResult.GAE.getStepCount(),
                YutResult.MO.getStepCount()
        );
        assertFalse(piece(0,0).isFinished());
    }

    /** 0(윷)->4(모)->9(모)->14(윷)->18(모)->23(걸)->0(완주) */
    @Test @DisplayName("교차점 X (외곽 path)")
    void pentagonH() {
        apply(
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.YUT.getStepCount(),
                YutResult.MO.getStepCount(),
                YutResult.GEOL.getStepCount()
        );
        assertTrue(piece(0,0).isFinished());
    }

    /** e. 0(도)->1(빽도)->0(빽도)->1(빽도)->0(개)->0(완주) */
    @Test
    @DisplayName("BACKDO 이동")
    void pentagonI() {
        apply(
                YutResult.DO.getStepCount(),
                YutResult.BACKDO.getStepCount()
        );
        assertEquals(0, piece(0,0).getPosition().getId(),"위치는 0 이어야 합니다.");
        apply(YutResult.BACKDO.getStepCount());
        assertEquals(1, piece(0,0).getPosition().getId(),"위치는 1 이어야 합니다.");
        apply(YutResult.BACKDO.getStepCount());
        assertEquals(0, piece(0,0).getPosition().getId(),"위치는 0 이어야 합니다.");
        apply(YutResult.GAE.getStepCount());
        assertTrue(piece(0,0).isFinished(), "말이 완주되어야 합니다.");
    }
}
