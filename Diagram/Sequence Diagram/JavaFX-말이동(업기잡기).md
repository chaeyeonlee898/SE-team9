```mermaid
sequenceDiagram
    actor User as user
    participant FXGameController
    participant Game
    participant FXDialog
    participant BoardPane
    participant Board
    participant Piece

    User->>FXGameController: onRandomThrowButtonClicked()
    activate FXGameController

    FXGameController->>Game: getRandomYutResults()
    activate Game
    note over Game: 랜덤으로 YutResult 리스트 생성
    Game-->>FXGameController: List<YutResult> pendingResults 반환
    deactivate Game

    loop pendingResults에 결과가 남아있는 동안
        FXGameController->>FXDialog: selectYutResult(pendingResults)
        activate FXDialog
        FXDialog-->>FXGameController: selResult 또는 null 반환
        deactivate FXDialog

        alt selResult != null
            FXGameController->>FXDialog: askPieceSelection(currentPlayer.getUnfinishedPieces())
            activate FXDialog
            FXDialog-->>FXGameController: selPiece 또는 null 반환
            deactivate FXDialog

            alt selPiece != null
                FXGameController->>Game: applyYutResult(selResult, selPiece)
                activate Game

                note over Game: movePiece(selPiece, selResult) → 위치 계산
                Game->>Board: getCurrentNode(selPiece.position)
                activate Board
                Board-->>Game: 현재 노드 반환
                deactivate Board

                Game->>Board: computeTargetNode(currentNode, selResult)
                activate Board
                Board-->>Game: 목표 노드 반환
                deactivate Board

                Game->>Piece: updatePosition(targetNode)
                activate Piece
                Piece-->>Game: 위치 갱신 완료
                deactivate Piece

                note over Game: getPiecesOnNode(targetNode) → 업기/잡기 판별
                Game->>Board: getPiecesOnNode(targetNode)
                activate Board
                Board-->>Game: samePositionPieces 반환
                deactivate Board

                alt 상대말 있으면 (잡기)
                    Game->>Piece: capture(opponentPiece)
                    activate Piece
                    Piece-->>Game: 잡혔으니 출발지 복귀 처리
                    deactivate Piece
                else 내말만 있으면 (업기)
                    Game->>Piece: group(ownPiece, selPiece)
                    activate Piece
                    Piece-->>Game: 묶어서 이동
                    deactivate Piece
                else 아예 비어있으면
                    note over Game: 단순 이동
                end

                Game-->>FXGameController: 이동 결과(capturedFlag) 반환
                deactivate Game

                FXGameController->>BoardPane: refresh()
                activate BoardPane
                BoardPane-->>FXGameController: 렌더링 완료
                deactivate BoardPane

                FXGameController->>FXGameController: updateTurnLabel(), updateStatusLabel()
            end
            FXGameController->>FXGameController: pendingResults.remove(selResult)
        else selResult == null
            FXGameController-->>User: 취소 → 윷 던지기 모드 재선택
            break
        end
    end

    FXGameController-->>User: 턴 처리 완료
    deactivate FXGameController
