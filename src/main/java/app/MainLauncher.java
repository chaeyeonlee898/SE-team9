package app;

import javax.swing.*; // 사용자 선택용 GUI
import java.util.Objects;

public class MainLauncher {
    public static void main(String[] args) {
        // JOptionPane으로 선택지 제공
        Object[] options = { "JavaFX", "Swing" };
        int choice = JOptionPane.showOptionDialog(
                null,
                "UI 모드를 선택하세요:",
                "SE-Team9 - UI 선택",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        // 선택에 따라 MainFX 또는 MainSwing 실행
        if (choice == 0) {
            System.out.println("👉 JavaFX 실행 중...");
            MainFX.main(args); // JavaFX 진입점
        } else if (choice == 1) {
            System.out.println("👉 Swing 실행 중...");
            MainSwing.main(args); // Swing 진입점
        } else {
            System.out.println("❌ 선택되지 않아 프로그램을 종료합니다.");
        }
    }
}
