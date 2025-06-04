import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import model.*;

@DisplayName("SquareBoard 이동 로직 전체 테스트")
public class SquareUnitTest extends AbstractBoardTest {
    @Override
    protected Board createBoard() {
        return new SquareBoard();
    }

    @Override
    protected int numberOfPlayers() {
        return 1;
    }

    @Override
    protected int piecesPerPlayer() {
        return 1;
    }

    @Nested
    @DisplayName("Single Game Scenario")
    class SingleGame {
        /**
         * 0(모)->5(걸)->28(개)->27(빽도)->26(걸)->0(완주) + 빽도 수행
         */
        @Test
        @DisplayName("교차점 5 & 교차점 28 (1번째 path)")
        void singleSquareA() {
            apply(
                    YutResult.MO.getStepCount(),
                    YutResult.GEOL.getStepCount(),
                    YutResult.GAE.getStepCount(),
                    YutResult.BACKDO.getStepCount(),
                    YutResult.GEOL.getStepCount(),
                    YutResult.GAE.getStepCount()
            );
            assertTrue(players.get(0).getPieces().get(0).isFinished(), "모→걸→개→빽도→걸→개 순서로 던지면 완주되어야 합니다.");
        }

        /**
         * 0(모)->5(모)->25(걸)->17(걸)->0(빽도)->19(개)->0(완주) + 0 에서 빽도 수행
         */
        @Test
        @DisplayName("교차점 5 (2번째 path)")
        void singleSquareB() {
            apply(
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.GEOL.getStepCount(),
                    YutResult.GEOL.getStepCount(),
                    YutResult.BACKDO.getStepCount(),
                    YutResult.GAE.getStepCount(),
                    YutResult.YUT.getStepCount()
            );
            assertTrue(players.get(0).getPieces().get(0).isFinished(), "모→모→걸→걸→빽도→개→윷 순서로 던지면 완주되어야 합니다.");
        }

        /**
         * 0(윷)→4(모)→9(도)→10(걸)→28(걸)→0(미완주) + 0 지점에 도착시 미완주
         */
        @Test
        @DisplayName("교차점 10 & 교차점 28 (1번째 path)")
        void singleSquareC() {
            apply(
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount(),
                    YutResult.GEOL.getStepCount(),
                    YutResult.GEOL.getStepCount()
            );
            assertFalse(players.get(0).getPieces().get(0).isFinished(), "윷→모→도→걸→걸 순서로 던지면 아직 완주되지 않아야 합니다.");
            assertEquals(0, players.get(0).getPieces().get(0).getPosition().getId(), "0(출발점)에 멈춰도 미완주 상태여야 합니다.");
        }

        /**
         * 0(윷)->4(모)->9(도)->10(모)->25(모)->19(개)->0(완주)
         */
        @Test
        @DisplayName("교차점 10 (2번째 path)")
        void singleSquareD() {
            apply(
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.GAE.getStepCount(),
                    YutResult.YUT.getStepCount()
            );
            assertTrue(players.get(0).getPieces().get(0).isFinished(), "윷→모→도→모→모→개→윷 순서로 던지면 완주되어야 합니다.");
        }

        /**
         * 0(윷)->4(모)->9(모)->14(도)->15(모)->0(개)->0(완주) + 0 지점 도착 후 positive step (완주)
         */
        @Test
        @DisplayName("교차점 15 (외곽 path)")
        void singleSquareE() {
            apply(
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.GAE.getStepCount()
            );
            assertTrue(players.get(0).getPieces().get(0).isFinished(), "윷→모→모→도→모→개 순서로 던지면 완주되어야 합니다.");
        }

        /**
         * 0(윷)->4(모)->9(모)->14(윷)->18(걸)->0(완주)
         */
        @Test
        @DisplayName("교차점 X (외곽 path)")
        void singleSquareF() {
            apply(
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.YUT.getStepCount(),
                    YutResult.GEOL.getStepCount()
            );
            assertTrue(players.get(0).getPieces().get(0).isFinished(), "윷→모→모→윷→걸 순서로 던지면 완주되어야 합니다.");
        }

        /**
         * 0(도)->1(빽도)->0(빽도)->1(빽도)->0(개)->0(완주)
         */
        @Test
        @DisplayName("squareG: BACKDO 이동")
        void singleSquareG() {
            // 도(1) → 1
            apply(YutResult.DO.getStepCount());
            assertEquals(1, players.get(0).getPieces().get(0).getPosition().getId(), "도 후 위치는 1이어야 합니다.");
            // 빽도 → 0
            apply(YutResult.BACKDO.getStepCount());
            assertEquals(0, players.get(0).getPieces().get(0).getPosition().getId(), "빽도 후 위치는 0이어야 합니다.");
            // 빽도 → 최종 이력 따라 1
            apply(YutResult.BACKDO.getStepCount());
            assertEquals(1, players.get(0).getPieces().get(0).getPosition().getId(), "연속 빽도 후 위치는 1이어야 합니다.");
            // 빽도 → 0
            apply(YutResult.BACKDO.getStepCount());
            assertEquals(0, players.get(0).getPieces().get(0).getPosition().getId(), "다시 빽도 후 위치는 0이어야 합니다.");
            // 개(2) → 마지막 완주
            apply(YutResult.GAE.getStepCount());
            assertTrue(players.get(0).getPieces().get(0).isFinished(), "개 후 완주되어야 합니다.");
        }
    }

    @Nested
    @DisplayName("Multi Game(2명, 말 5개) Scenario")
    class MultiGame {
        @BeforeEach
        void overrideGameSetup() {
            game = new Game(2,5,board);
            players = game.getPlayers();
        }

        @Test
        void multiSquareA() {
            applyOne(0,1,
                    YutResult.MO.getStepCount(),
                    YutResult.GEOL.getStepCount()  // P0.M1 : 28
            );
            applyOne(1,1,
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.GEOL.getStepCount()  // P1.M1 : 17
            );

            applyOne(0,2,
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount()  // P0.M2 : 10
            );
            applyOne(1,2,
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount()  // P1.M2 : 10 (P1.M2 잡기)
            );
            assertNull(piece(0,2).getPosition(), "Player1 의 말2 은 잡혔습니다.");

            applyOne(1,2,
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.GAE.getStepCount()  // P1.M2 : 완주
            );
            assertTrue(piece(1,2).isFinished(),"Player2 의 말3 는 완주 되어야 합니다.");

            applyOne(0,3,
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount()  // P0.M3 : 15
            );

            applyOne(1,3,
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.YUT.getStepCount(),
                    YutResult.GEOL.getStepCount()  // P1.M3 : 완주
            );
            assertTrue(piece(1,3).isFinished(),"Player2 의 말3 는 완주 되어야 합니다.");

            applyOne(0,4,
                    YutResult.BACKDO.getStepCount()
            );
            assertNull(piece(0,4).getPosition(), "Player1의 말1 은 진입하지 못했습니다.");

            applyOne(1,4,
                    YutResult.YUT.getStepCount(),
                    YutResult.BACKDO.getStepCount()  // P1.M4 : 3
            );
            applyOne(0,0,
                    YutResult.MO.getStepCount(),
                    YutResult.GAE.getStepCount()  // P0.M0 : 21
            );
            applyOne(1,0,
                    YutResult.GEOL.getStepCount()  // P1.M0 : 3 (P1.M4 업기)
            );
            assertSame(piece(1,0).getPosition(), piece(1,4).getPosition());

            /** turn1 근황
             * Player0 : 말1 - 28, 말2 - 집, 말3 - 15, 말4 - 집, 말0 - 21
             * Player1 : 말1 - 17, 말2 - 완주, 말3 - 완주, 말4+말0 - 3
             */

            applyOne(0,1,
                    YutResult.GAE.getStepCount()  // P0.M1 : 27
            );
            applyOne(1,1,
                    YutResult.GEOL.getStepCount()  // P1.M1 : 0
            );
            applyOne(0,2,
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount()  // P0.M2 : 10
            );

            applyOne(1,4,
                    YutResult.BACKDO.getStepCount()  // P1.M4 & P1.M0 : 2
            );
            applyOne(0,3,
                    YutResult.MO.getStepCount(),
                    YutResult.GAE.getStepCount()  // P0.M3 : 완주
            );
            applyOne(1,0,
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount()  // P1.M0 & P1.M4 : 8
            );
            applyOne(0,4,
                    YutResult.DO.getStepCount()  // P0.M4 : 1
            );

            /** turn2 근황
             * Player0 : 말1 - 27, 말2 - 10, 말3 - 완주, 말4 - 1, 말0 - 21
             * Player1 : 말1 - 0, 말2 - 완주, 말3 - 완주, 말4+말0 - 8
             */

            applyOne(1,1,
                    YutResult.BACKDO.getStepCount()  // P1.M1 : 19
            );
            applyOne(0,0,
                    YutResult.YUT.getStepCount(),
                    YutResult.GEOL.getStepCount()  // P0.M0 : 18
            );
            applyOne(1,4,
                    YutResult.GAE.getStepCount()  // P1.M4 & P1.M0 : 10 (P0.M2 잡기)
            );
            assertNull(piece(0,2).getPosition(), "말이 잡혔습니다.");
            applyOne(1,4,
                    YutResult.GEOL.getStepCount()  // P1.M4 & P1.M0 : 28
            );

            applyOne(0,1,
                    YutResult.BACKDO.getStepCount()  // P0.M1 : 26
            );

            applyOne(1,0,
                    YutResult.GEOL.getStepCount()  // P1.M0 & P1.M4 : 0
            );

            applyOne(0,2,
                    YutResult.YUT.getStepCount(),
                    YutResult.MO.getStepCount(),
                    YutResult.DO.getStepCount()  // P0.M2 : 10
            );

            /** turn3 근황
             * Player0 : 말1 - 26, 말2 - 10, 말3 - 완주, 말4 - 1, 말0 - 18
             * Player1 : 말1 - 19, 말2 - 완주, 말3 - 완주, 말4+말0 - 0
             */
        }

        @Test
        void multiSquareB() {
            // 1) P0의 5개 말을 모두 1칸 이동(같은 칸에 업기)
            for (int i = 0; i < 5; i++) {
                applyOne(0, i, YutResult.DO.getStepCount());
                // 이 시점에서 players.get(0).getPieces().get(i).getPosition().getId() == 1
                assertEquals(1,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "P0.M" + i + "는 1에 있어야 합니다."
                );
            }

            // 2) 연속 빽도 → all at 1
            applyOne(0, 0,
                    YutResult.BACKDO.getStepCount(),
                    YutResult.BACKDO.getStepCount(),
                    YutResult.BACKDO.getStepCount()
            );
            applyOne(0, 0, YutResult.BACKDO.getStepCount());
            for (int i = 0; i < 5; i++) {
                assertEquals(1,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "빽도 후 P0.M" + i + "는 1에 있어야 합니다."
                );
            }

            // 3) 도(1) → all at 2
            applyOne(0, 0, YutResult.DO.getStepCount());
            for (int i = 0; i < 5; i++) {
                assertEquals(2,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "도 후 P0.M" + i + "는 2에 있어야 합니다."
                );
            }

            // 4) 개(2) → all at 4
            applyOne(0, 0, YutResult.GAE.getStepCount());
            for (int i = 0; i < 5; i++) {
                assertEquals(4,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "개 후 P0.M" + i + "는 4에 있어야 합니다."
                );
            }

            // 5) 걸(3) → all at 7
            applyOne(0, 0, YutResult.GEOL.getStepCount());
            for (int i = 0; i < 5; i++) {
                assertEquals(7,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "걸 후 P0.M" + i + "는 7에 있어야 합니다."
                );
            }

            // 6) 윷(4) → all at 10
            applyOne(0, 0, YutResult.YUT.getStepCount());
            for (int i = 0; i < 5; i++) {
                assertEquals(11,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "윷 후 P0.M" + i + "는 11에 있어야 합니다."
                );
            }

            // 7) 모(5) → all at 16
            applyOne(0, 0, YutResult.MO.getStepCount());
            for (int i = 0; i < 5; i++) {
                assertEquals(16,
                        players.get(0).getPieces().get(i).getPosition().getId(),
                        "모 후 P0.M" + i + "는 16에 있어야 합니다."
                );
            }

            // 8) P1.M0을 같은 칸(16)로 이동 → P0의 5개 말 모두 집으로 돌아가야 함
            applyOne(1, 0,
                    YutResult.MO.getStepCount(),  // 5
                    YutResult.MO.getStepCount(), // 25
                    YutResult.GAE.getStepCount()); // 16

            // 검증: P0.M0~M4 모두 position == null
            for (int i = 0; i < 5; i++) {
                assertNull(
                        players.get(0).getPieces().get(i).getPosition(),
                        "P0.M" + i + "가 잡혀서 집(null)이어야 합니다."
                );
            }
        }


    }
}