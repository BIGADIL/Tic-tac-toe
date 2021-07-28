package aiagents;

import answer.AIAnswer;
import board.Coord;
import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;
import listeners.EndOfGameDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class SimpleMinMaxAgent extends BaseAgent {
    public static class SimpleMinMaxAgentFactory extends BaseAgentFactory {
        @Override
        public SimpleMinMaxAgent createAgent(final CellType agentCellType) {
            final String name = "SimpleMinMaxAgent-" + botIdx++;
            return new SimpleMinMaxAgent(name, agentCellType);
        }
    }

    private static final class AnswerAndWin {
        final AIAnswer answer;
        final double win;

        private AnswerAndWin(final AIAnswer answer, final double win) {
            this.answer = answer;
            this.win = win;
        }
    }

    private final EndOfGameDetector eogDetector = new EndOfGameDetector();
    private final CellType oppCellType;

    protected SimpleMinMaxAgent(final String name, final CellType agentCellType) {
        super(name, agentCellType);
        oppCellType = agentCellType == CellType.CROSSES ? CellType.NOUGHTS : CellType.CROSSES;
    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        final List<Coord> coordToAct = board.getAllPossibleCoordToAct();
        final List<AnswerAndWin> awList = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, agentCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final double win = getWinByGameTree(copy);
            awList.add(new AnswerAndWin(aiAnswer, win));
        }
        return getGreedyDecision(awList, aw -> aw.win).answer;
    }

    private double getWinByGameTree(final ImplBoard board) {
       final ToDoubleFunction<AnswerAndWin> winCalculator;
       final CellType simCellType;
        final int nextPlayerId = board.getNextPlayerId();
        if (nextPlayerId == id) {
            winCalculator = aw -> aw.win;
            simCellType = agentCellType;
        } else {
            winCalculator = aw -> -aw.win;
            simCellType = oppCellType;
        }
        final WinnerType winnerType = eogDetector.detectWinner(board);
        if (winnerType != WinnerType.NONE) {
            return computeWin(winnerType);
        }
        final List<Coord> coordToAct = board.getAllPossibleCoordToAct();
        final List<AnswerAndWin> awList = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, simCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final double win = getWinByGameTree(copy);
            awList.add(new AnswerAndWin(aiAnswer, win));
        }
        return getGreedyDecision(awList, winCalculator).win;
    }

    private AnswerAndWin getGreedyDecision(final List<AnswerAndWin> awList,
                                           final ToDoubleFunction<AnswerAndWin> winCalculator) {
        AnswerAndWin bestAW = awList.get(0);
        double bestWin = winCalculator.applyAsDouble(bestAW);
        for (int i = 1; i < awList.size(); i++) {
            final AnswerAndWin currentAW = awList.get(i);
            final double currentWin = winCalculator.applyAsDouble(currentAW);
            if (currentWin > bestWin) {
                bestAW = currentAW;
                bestWin = currentWin;
            }
        }
        return bestAW;
    }

    private double computeWin(final WinnerType winnerType) {
        if (!winnerType.isTerminalWinnerType()) {
            throw new IllegalStateException(winnerType + " is not terminal WinnerType");
        }
        if (winnerType == WinnerType.DRAW) {
            return 0;
        } else if (winnerType == agentWinnerType) {
            return 1;
        } else {
            return -1;
        }
    }
}
