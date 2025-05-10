import controller.YutnoriGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Scanner;
import model.*;
/*
게임 전체 flow 시나리오 에 대한 통합 테스트로 사용할 class 입니다!
이 부분은 단순 단위 테스트가 아니어서 main 폴더의 class 들 입력 방식을 수정해야 하기 때문에
시험 끝난 후.. 진행하겠습니다.
 */
import model.*;
@DisplayName("윷놀이 게임 테스트") // 전체 흐름 통합 테스트로 사용할 예정
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // BeforeAll 사용할 때 static 선언을 회피하기 위해

public class GameFlowTest {
    YutnoriGame game;

    @BeforeAll
    void beforeAll() {
        game = new YutnoriGame();
    }

    @Test
    public void test1() {
    }

    @Test
    public void test2() {

    }


    @Test
    public void test3() {


    }

    @AfterAll
    public void afterAll() {

    }

    @AfterEach
    public void afterEach() {



    }
}
