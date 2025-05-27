package view;

import model.Piece;
import model.YutResult;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * DialogUtils는 게임 진행 중 사용자와 상호작용하는
 * 모든 JOptionPane 다이얼로그를 모아둔 유틸리티 클래스입니다.
 */
public class DialogUtils {

    /**
     * 던지기 방식을 묻고 랜덤 모드 여부를 반환합니다.
     * @return 랜덤 윷 던지기 선택 시 true, 지정 윷 던지기 선택 시 false
     */
    public static boolean askRandomMode() {
        String[] options = {"지정 윷 던지기", "랜덤 윷 던지기"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "윷 던지기 방식을 선택하세요:",
                "던지기 방식 선택",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]
        );
        return choice == 1;
    }

    /**
     * 수동 윷 던지기 결과를 요청합니다.
     * @return 선택된 YutResult 또는 취소 시 null
     */
    public static YutResult askManualThrow() {
        return (YutResult) JOptionPane.showInputDialog(
                null,
                "지정할 윷 결과를 선택하세요:",
                "수동 윷 던지기",
                JOptionPane.PLAIN_MESSAGE,
                null,
                YutResult.values(),
                YutResult.DO
        );
    }

    /**
     * 던지기 결과를 팝업으로 보여줍니다.
     * @param result 보여줄 YutResult
     */
    public static void showThrowResult(YutResult result) {
        if (result != null) {
            JOptionPane.showMessageDialog(
                    null,
                    "윷 결과: " + result,
                    "던지기 결과",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * 적용할 윷 결과를 선택하도록 다이얼로그를 띄웁니다.
     * @param results 수집된 YutResult 리스트
     * @return 선택된 YutResult 또는 취소 시 null
     */
    public static YutResult selectYutResult(List<YutResult> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }
        return (YutResult) JOptionPane.showInputDialog(
                null,
                "적용할 윷 결과를 선택하세요:",
                "윷 결과 선택",
                JOptionPane.PLAIN_MESSAGE,
                null,
                results.toArray(),
                results.get(0)
        );
    }

    /**
     * 이동할 말을 선택하도록 다이얼로그를 띄웁니다.
     * @param pieces 이동 가능한 Piece 리스트
     * @return 선택된 Piece 객체 또는 취소 시 null
     */
    public static Piece askPieceSelection(List<Piece> pieces) {
        if (pieces == null || pieces.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "이동 가능한 말이 없습니다.",
                    "말 선택",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
        return (Piece) JOptionPane.showInputDialog(
                null,
                "이동할 말을 선택하세요:",
                "말 선택",
                JOptionPane.PLAIN_MESSAGE,
                null,
                pieces.toArray(),
                pieces.get(0)
        );
    }

    /**
     * 게임 승리 후 재시작 여부를 묻습니다.
     * @param winnerName 승리한 플레이어 이름
     * @return 재시작 선택 시 true, 아니면 false
     */
    public static boolean confirmRestart(String winnerName) {
        int opt = JOptionPane.showConfirmDialog(
                null,
                winnerName + " 승리!\n게임을 다시 시작할까요?",
                "게임 종료",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return opt == JOptionPane.YES_OPTION;
    }
}
