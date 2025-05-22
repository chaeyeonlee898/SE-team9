package view.javafx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import model.Piece;
import model.YutResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * JavaFX용 다이얼로그 유틸리티 클래스
 * Swing용 DialogUtils와 유사한 API를 제공합니다.
 */
public final class FXDialog {
    private FXDialog() { /* 생성 방지 */ }

    /**
     * 던지기 방식을 묻고 랜덤 모드 여부를 반환합니다.
     * @return 랜덤 윷 던지기 선택 시 true, 지정 윷 던지기 선택 시 false
     */
    public static boolean askRandomMode() {
        final String[] options = {"지정 윷 던지기", "랜덤 윷 던지기"};
        if (Platform.isFxApplicationThread()) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(options[1], options);
            dialog.setTitle("던지기 방식 선택");
            dialog.setHeaderText(null);
            dialog.setContentText("윷 던지기 방식을 선택하세요:");
            Optional<String> result = dialog.showAndWait();
            return result.map(opt -> opt.equals(options[1])).orElse(false);
        } else {
            final boolean[] choice = new boolean[1];
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                choice[0] = askRandomMode();
                latch.countDown();
            });
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return choice[0];
        }
    }

    /**
     * 수동 윷 던지기 결과를 요청합니다.
     * @return 선택된 YutResult 또는 취소 시 null
     */
    public static YutResult askManualThrow() {
        if (Platform.isFxApplicationThread()) {
            ChoiceDialog<YutResult> dialog = new ChoiceDialog<>(YutResult.DO, YutResult.values());
            dialog.setTitle("수동 윷 던지기");
            dialog.setHeaderText(null);
            dialog.setContentText("지정할 윷 결과를 선택하세요:");
            Optional<YutResult> result = dialog.showAndWait();
            return result.orElse(null);
        } else {
            final YutResult[] selection = new YutResult[1];
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                selection[0] = askManualThrow();
                latch.countDown();
            });
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return selection[0];
        }
    }

    /**
     * 던지기 결과를 팝업으로 보여줍니다.
     * @param result 보여줄 YutResult
     */
    public static void showThrowResult(YutResult result) {
        runOnFxThread(() -> {
            if (result != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("던지기 결과");
                alert.setHeaderText(null);
                alert.setContentText("윷 결과: " + result);
                alert.showAndWait();
            }
        });
    }

    /**
     * 적용할 윷 결과를 선택하도록 다이얼로그를 띄웁니다.
     * @param results 수집된 YutResult 리스트
     * @return 선택된 YutResult 또는 취소 시 null
     */
    public static YutResult selectYutResult(List<YutResult> results) {
        if (Platform.isFxApplicationThread()) {
            ChoiceDialog<YutResult> dialog = new ChoiceDialog<>(
                    results.get(0), results);
            dialog.setTitle("윷 결과 선택");
            dialog.setHeaderText(null);
            dialog.setContentText("적용할 윷 결과를 선택하세요:");
            Optional<YutResult> result = dialog.showAndWait();
            return result.orElse(null);
        } else {
            final YutResult[] choice = new YutResult[1];
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                choice[0] = selectYutResult(results);
                latch.countDown();
            });
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return choice[0];
        }
    }

    /**
     * 이동할 말을 선택하도록 다이얼로그를 띄웁니다.
     * @param pieces 이동 가능한 Piece 리스트
     * @return 선택된 Piece 객체 또는 취소 시 null
     */
    public static Piece askPieceSelection(List<Piece> pieces) {
        if (Platform.isFxApplicationThread()) {
            if (pieces == null || pieces.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("말 선택");
                alert.setHeaderText(null);
                alert.setContentText("이동 가능한 말이 없습니다.");
                alert.showAndWait();
                return null;
            }
            ChoiceDialog<Piece> dialog = new ChoiceDialog<>(pieces.get(0), pieces);
            dialog.setTitle("말 선택");
            dialog.setHeaderText(null);
            dialog.setContentText("이동할 말을 선택하세요:");
            Optional<Piece> result = dialog.showAndWait();
            return result.orElse(null);
        } else {
            final Piece[] choice = new Piece[1];
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                choice[0] = askPieceSelection(pieces);
                latch.countDown();
            });
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return choice[0];
        }
    }

    /**
     * 게임 승리 후 재시작 여부를 묻습니다.
     * @param winnerName 승리한 플레이어 이름
     * @return 재시작 선택 시 true, 아니면 false
     */
//    public static boolean confirmRestart(String winnerName) {
//        if (Platform.isFxApplicationThread()) {
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            alert.setTitle("게임 종료");
//            alert.setHeaderText(null);
//            alert.setContentText(winnerName + " 승리! 게임을 다시 시작할까요?");
//            Optional<ButtonType> result = alert.showAndWait();
//            return result.filter(bt -> bt == ButtonType.YES).isPresent();
//        } else {
//            final boolean[] answer = new boolean[1];
//            final CountDownLatch latch = new CountDownLatch(1);
//            Platform.runLater(() -> {
//                answer[0] = confirmRestart(winnerName);
//                latch.countDown();
//            });
//            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
//            return answer[0];
//        }
//    }

    public static boolean confirmRestart(String winnerName) {
        // ① 버튼을 YES/NO 로 직접 지정
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                winnerName + " 승리! 게임을 다시 시작할까요?",
                ButtonType.YES, ButtonType.NO
        );
        alert.setTitle("게임 종료");
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(bt -> bt == ButtonType.YES).isPresent();
    }


    /**
     * JavaFX Application Thread에서 안전하게 실행하도록 보장합니다.
     */
    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try { action.run(); } finally { latch.countDown(); }
            });
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
