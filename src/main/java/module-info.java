module com.example.demo2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // 모델과 로직·컨트롤러를 외부에 공개
    exports app;
    exports model;
    exports controller;
    exports view.swing;
    exports view.javafx;

    // FXML이 리플렉션으로 접근해야 하는 패키지
    opens controller        to javafx.fxml;
    opens view.javafx       to javafx.fxml;
    opens model             to javafx.base;
}
