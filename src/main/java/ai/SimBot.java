package ai;

import answer.AIAnswer;
import board.Board;
import enums.CellType;
import enums.WinnerType;
import listeners.EndOfGameDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimBot extends BaseBot {

    private static Logger logger = LoggerFactory.getLogger(SimBot.class);

    private static final double EPS = 1.0e-9;

    private final Map<WinnerType, CellType> mapWinnerTypeOnCellType = new EnumMap<>(WinnerType.class);

    private final EndOfGameDetector eogDetector = new EndOfGameDetector();

    private static final class AnswerAndWin {
        final AIAnswer answer;
        final WinCollector winCollector;

        private AnswerAndWin(final AIAnswer answer, final WinCollector winCollector) {
            this.answer = answer;
            this.winCollector = winCollector;
        }

        @Override
        public String toString() {
            return answer + " -> " + winCollector;
        }
    }

    public SimBot(final int id, final String name, final CellType myType) {
        super(id, name, myType);
        mapWinnerTypeOnCellType.put(WinnerType.CROSSES, CellType.CROSSES);
        mapWinnerTypeOnCellType.put(WinnerType.NOUGHTS, CellType.NOUGHTS);
    }

    @Override
    public AIAnswer getAnswer(final Board board) {
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        final List<AnswerAndWin> awList = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, myType);
            final Board copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy);
            awList.add(new AnswerAndWin(aiAnswer, winCollector));
        }
        logDecisions(awList);

        return getGreedyDecision(awList).answer;
    }

    private void logDecisions(final List<AnswerAndWin> awList) {
        logger.info("--{}: print decisions", name);
        awList.forEach(aw -> logger.info("--{}: {}", name, aw));
        logger.info("");
    }

    private WinCollector getWinByGameTree(final Board board) {
        final WinnerType winnerType = eogDetector.detectWinner(board);
        if (winnerType != WinnerType.NONE) {
            return distributeWin(winnerType);
        }
        final int nextPlayerId = board.getNextPlayerId();
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        if (nextPlayerId == id) {
            final List<AnswerAndWin> aw = new ArrayList<>();
            for (final Coord coord : coordToAct) {
                final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, myType);
                final Board copy = board.getCopy(aiAnswer);
                final WinCollector winCollector = getWinByGameTree(copy);
                aw.add(new AnswerAndWin(aiAnswer, winCollector));
            }
            return getGreedyDecision(aw).winCollector.catchBrokenProbabilities();
        } else {
            final List<WinCollector> winCollectors = new ArrayList<>();
            for (final Coord coord : coordToAct) {
                final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, CellType.getRevertType(myType));
                final Board copy = board.getCopy(aiAnswer);
                final WinCollector winCollector = getWinByGameTree(copy);
                winCollectors.add(winCollector);
            }
            return getMixedDecision(winCollectors).catchBrokenProbabilities();
        }
    }

    private WinCollector getMixedDecision(final List<WinCollector> winCollectors) {
        final double sequenceLength = (double) winCollectors.size();
        final double p = 1.0D / sequenceLength;
        final WinCollector mixedWin = new WinCollector(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        for (final WinCollector winCollector : winCollectors) {
            mixedWin.add(winCollector, p);
        }
        return mixedWin.catchBrokenProbabilities();
    }

    private AnswerAndWin getGreedyDecision(final List<AnswerAndWin> awList) {
        AnswerAndWin bestDecision = null;
        for (final AnswerAndWin tmpDecision : awList) {
            if (bestDecision == null) {
                bestDecision = tmpDecision;
                continue;
            }
            final double bestTotalWin = bestDecision.winCollector.getTotalWin();
            final double tmpTotalWin = tmpDecision.winCollector.getTotalWin();
            final double bestWin = bestDecision.winCollector.win;
            final double tmpWin = tmpDecision.winCollector.getTotalWin();
            if (tmpTotalWin > bestTotalWin || (Math.abs(tmpTotalWin - bestTotalWin) < EPS && tmpWin > bestWin)) {
                bestDecision = tmpDecision;
            }
        }
        if (bestDecision == null) {
            throw new IllegalArgumentException("Can't find best decision");
        }
        return bestDecision;
    }

    private WinCollector distributeWin(final WinnerType winnerType) {
        final WinCollector result;
        if (winnerType == WinnerType.DRAW) {
            result = new WinCollector(0.0D, 0.5D, 0.0D, 0.0D, 1.0D);
        } else {
            final CellType winnerCellType = mapWinnerTypeOnCellType.get(winnerType);
            if (winnerCellType == myType) {
                result = new WinCollector(1.0D, 0.0D, 0.0D, 1.0D, 0.0D);
            } else {
                result = new WinCollector(0.0D, 0.0D, 1.0D, 0.0D, 0.0D);
            }
        }
        return result.catchBrokenProbabilities();
    }


    private static class WinCollector {
        double win;
        double drawWin;
        double pLoose;
        double pWin;
        double pDraw;

        WinCollector(final double win,
                     final double drawWin,
                     final double pLoose,
                     final double pWin,
                     final double pDraw) {
            this.win = win;
            this.drawWin = drawWin;
            this.pLoose = pLoose;
            this.pWin = pWin;
            this.pDraw = pDraw;
        }

        public void add(final WinCollector w, final double p) {
            win += p * w.win;
            drawWin += p * w.drawWin;
            pLoose += p * w.pLoose;
            pWin += p * w.pWin;
            pDraw += p * w.pDraw;
        }

        public double getTotalWin() {
            return win + drawWin;
        }

        public WinCollector catchBrokenProbabilities() {
            final double sumP = pDraw + pWin + pLoose;
            if (Math.abs(sumP - 1.0D) > EPS) {
                throw new IllegalStateException("Sum proba != 1: " + sumP);
            }
            return this;
        }

        @Override
        public String toString() {
            return String.format("win=%.2f, drawWin=%.2f, pLoose=%.2f, pWin=%.2f, pDraw=%.2f", win, drawWin, pLoose, pWin, pDraw);
        }
    }
}
