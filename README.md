# SE-team9: Yutnori Game (JavaFX + Swing UI)

> 전통 율로리를 JavaFX와 Swing으로 구현한 데스크톱 애플리케이션입니다.
> 사용자가 실행 시 **JavaFX 또는 Swing UI를 지정 선택**할 수 있는 통합 실행 환경을 제공합니다.

---

## 📌 프로젝트 특징

* Java 17 기반 크로스 플랫폼 게임
* JavaFX / Swing UI 중 선택 가능
* ShadowJar로 빌드된 fat JAR → **JavaFX SDK 설치 없이 실행 가능**
* 다양한 보드 모양(정사각형, 오갑형 등) 확장 가능 구조
* GUI 애니메이션 + CLI 구조 분리 개발

---

## 🎮 실행 방법

### 1. 요구 상태

* Java 17 이상 설치
  ➔ 설치 확인:

  ```bash
  java -version
  ```

* **JavaFX SDK는 필요하지 않음**

  > JavaFX가 포함된 fat JAR 파일 바로 실행 가능

---

### 2. 실행 옵션

#### ✅ A. 더블클릭 실행 (Windows/macOS)

* `SE-team9-1.0.jar` 파일을 더블클릭하면 게임이 실행됩니다.

#### ✅ B. 터미널에서 실행

```bash
java -jar SE-team9-1.0.jar
```

실행 후 JavaFX / Swing 중 원하는 UI를 선택하면 해당 인터페이스로 게임이 시작됩니다.

---

## 📁 프로젝트 구조

```
SE-team9/
├─ src/
│   ├─ main/java/
│   │   ├─ app
│   │   │   ├─ MainLauncher.java      // 공통 진입점 (UI 선택)
│   │   │   ├─ MainFX.java            // JavaFX 시작 지점
│   │   │   └─ MainSwing.java         // Swing 시작 지점
│   │   ├─ controller
│   │   ├─ model
│   │   └─ view
│   └─ test/   
├─ build.gradle                   // Gradle 설정 파일
├─ settings.gradle
├─ gradlew, gradlew.bat           // Gradle Wrapper
├─ README.md                      // 프로젝트 설명 파일
└─ SE-team9-1.0.jar   // ✅ 실행 가능한 fat JAR
```

---

## ⚙️ 개발자용: 직접 빌드하기

> 아래 명령은 프로젝트 루트에서 실행하세요.

```bash
./gradlew shadowJar
```

빌드가 완료되면 `build/libs/SE-team9-1.0.jar`가 생성됩니다.

---

## 📜 사용 기술

* Java 17
* JavaFX 21.0.1
* Swing
* Gradle 8.x + Shadow plugin 8.1.1
* JUnit 5 (테스트용)