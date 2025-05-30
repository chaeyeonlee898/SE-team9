package view.javafx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import model.Piece;
import model.YutResult;

import java.util.List;
import java.util.Optional;

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
        ChoiceDialog<String> dialog = new ChoiceDialog<>("랜덤 윷 던지기", "지정 윷 던지기", "랜덤 윷 던지기");
        dialog.setTitle("던지기 방식 선택");
        dialog.setHeaderText(null);
        dialog.setContentText("윷 던지기 방식을 선택하세요:");
        return dialog.showAndWait()
                     .map(opt -> opt.equals("랜덤 윷 던지기"))
                     .orElse(false);
    }

    /**
     * 수동 윷 던지기 결과를 요청합니다.
     * @return 선택된 YutResult 또는 취소 시 null
     */
    public static YutResult askManualThrow() {
        ChoiceDialog<YutResult> dialog = new ChoiceDialog<>(YutResult.DO, YutResult.values());
        dialog.setTitle("수동 윷 던지기");
        dialog.setHeaderText(null);
        dialog.setContentText("지정할 윷 결과를 선택하세요:");
        return dialog.showAndWait().orElse(null);
    }

    /**
     * 던지기 결과를 팝업으로 보여줍니다.
     * @param result 보여줄 YutResult
     */
    public static void showThrowResult(YutResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("던지기 결과");
        alert.setHeaderText(null);
        alert.setContentText("윷 결과: " + result);
        alert.showAndWait();
    }

    /**
     * 적용할 윷 결과를 선택하도록 다이얼로그를 띄웁니다.
     * @param results 수집된 YutResult 리스트
     * @return 선택된 YutResult 또는 취소 시 null
     */
    public static YutResult selectYutResult(List<YutResult> results) {
        ChoiceDialog<YutResult> dialog = new ChoiceDialog<>(results.get(0), results);
        dialog.setTitle("윷 결과 선택");
        dialog.setHeaderText(null);
        dialog.setContentText("적용할 윷 결과를 선택하세요:");
        return dialog.showAndWait().orElse(null);
    }

    /**
     * 이동할 말을 선택하도록 다이얼로그를 띄웁니다.
     * @param pieces 이동 가능한 Piece 리스트
     * @return 선택된 Piece 객체 또는 취소 시 null
     */
    public static Piece askPieceSelection(List<Piece> pieces) {
        if (pieces == null || pieces.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "이동 가능한 말이 없습니다.").showAndWait();
            return null;
        }
        ChoiceDialog<Piece> dialog = new ChoiceDialog<>(pieces.get(0), pieces);
        dialog.setTitle("말 선택");
        dialog.setHeaderText(null);
        dialog.setContentText("이동할 말을 선택하세요:");
        return dialog.showAndWait().orElse(null);
    }

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


}
