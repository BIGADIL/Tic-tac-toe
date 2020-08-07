package aibots.evaluators;

import aibots.common.WinCollector;
import answer.AIAnswer;
import board.Coord;
import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;
import listeners.EndOfGameDetector;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MonteCarloEvaluator {
    private final EndOfGameDetector eogDetector;

    private final Random rnd = new Random();

    public MonteCarloEvaluator(final EndOfGameDetector eogDetector) {
        this.eogDetector = eogDetector;
    }

    public WinCollector evaluateWin(final ImplBoard board,
                                    final CellType firstActionCellType,
                                    final WinnerType expectedWinnerType,
                                    final int iterations) {
        double win = 0.0D;
        double drawWin = 0.0D;
        double looseWin = 0.0D;
        double pWin = 0.0D;
        double pDraw = 0.0D;
        double pLoose = 0.0D;
        final List<Coord> coordToAct = board.getAllPossibleCoordToAct();
        for (int i = 0; i < iterations; i++) {
            final ImplBoard copy = board.getCopy();
            final List<Coord> copyOfList = new LinkedList<>(coordToAct);
            CellType curCellType = firstActionCellType;
            boolean continueGame = true;
            while (continueGame) {
                final int idx = rnd.nextInt(copyOfList.size());
                final Coord tmpCoord = copyOfList.remove(idx);
                copy.act(new AIAnswer(tmpCoord.x, tmpCoord.y, curCellType));
                final WinnerType winnerType = eogDetector.detectWinner(copy);
                if (winnerType.isTerminalWinnerType()) {
                    continueGame = false;
                    if (winnerType == WinnerType.DRAW) {
                        pDraw++;
                    } else if (winnerType == expectedWinnerType) {
                        pWin++;
                        win++;
                    } else {
                        pLoose++;
                        looseWin--;
                    }
                }
                curCellType = getNextCellType(curCellType);
            }
        }
        return new WinCollector(
                win / iterations,
                drawWin / iterations,
                looseWin / iterations,
                pLoose / iterations,
                pWin / iterations,
                pDraw / iterations);
    }

    private static CellType getNextCellType(final CellType curCellType) {
        return switch (curCellType) {
            case NOUGHTS -> CellType.CROSSES;
            case CROSSES -> CellType.NOUGHTS;
            default -> throw new IllegalArgumentException("Unsupported type = " + curCellType);
        };
    }
}
