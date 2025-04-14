import java.util.ArrayList;
import java.util.List;

class BoardNode {
    int id;               // 노드 식별 번호
    String name;          // 출력용 이름
    BoardNode next;       // 기본 다음 노드 (외곽 진행)
    BoardNode shortcut;   // 첫 스텝에서 적용되는 내부 경로 (단축)
    boolean isIntersection = false; // 교차점 여부
    boolean isCenter = false; //중앙점 여부
    List<Piece> pieces;  // 이 칸 위의 말들 (스택)

    public BoardNode(int id, String name) {
        this.id = id;
        this.name = name;
        this.pieces = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name + "(" + id + ")[" + pieces.size() + "]";
    }
}

