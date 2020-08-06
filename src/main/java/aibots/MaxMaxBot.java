package aibots;

import answer.AIAnswer;
import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;
import listeners.EndOfGameDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * Робот, играющий по принципу max-max.
 * Принятие решения противником идет по принципу максимизации его выигрыша.
 */
public class MaxMaxBot extends BaseBot {
    public static class MaxMaxBotFactory extends BaseBotFactory {
        @Override
        public MaxMaxBot createBot(final CellType botCellType) {
            final String name = "MaxMaxBot-" + botIdx++;
            return new MaxMaxBot(name, botCellType);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MaxMaxBot.class);

    public static final IBotFactory factory = new MaxMaxBot.MaxMaxBotFactory();

    private final EndOfGameDetector eogDetector = new EndOfGameDetector();
    private final boolean isPrintLogs = true;
    private final Random rnd = new Random();

    private long genTime = 0L;

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

    public MaxMaxBot(final String name,
                     final CellType myType) {
        super(name, myType);
    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        terminalNodes = 0;
        totalNodes = 0;
        final long startTime = System.currentTimeMillis();
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        final List<AnswerAndWin> awList = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            totalNodes++;
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, botCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy);
            awList.add(new AnswerAndWin(aiAnswer, winCollector));
        }
        genTime = System.currentTimeMillis() - startTime;
        logDecisions(awList);
        return getGreedyDecision(awList, WinCollector::getTotalWin).answer;
    }

    private void logDecisions(final List<AnswerAndWin> awList) {
        if (!isPrintLogs) {
            return;
        }
        logger.info("--{}: print decisions", name);
        awList.forEach(aw -> logger.info("--{}: {}", name, aw));
        logger.info("--{}: total num nodes = {}", name, totalNodes);
        logger.info("--{}: terminal num nodes = {}", name, terminalNodes);
        logger.info("--{}: genTime = {} millis", name, genTime);
        logger.info("");
    }

    private WinCollector getWinByGameTree(final ImplBoard board) {
        final WinnerType winnerType = eogDetector.detectWinner(board);
        if (winnerType != WinnerType.NONE) {
            return distributeWin(winnerType);
        }
        final int nextPlayerId = board.getNextPlayerId();
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        final ToDoubleFunction<WinCollector> winCalculator;
        final CellType simCellType;
        if (nextPlayerId == id) {
            winCalculator = WinCollector::getTotalWin;
            simCellType = botCellType;
        } else {
            winCalculator = w -> 1.0D - w.getTotalWin();
            simCellType = getOppCellType();
        }
        final List<AnswerAndWin> awList = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            totalNodes++;
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, simCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy);
            awList.add(new AnswerAndWin(aiAnswer, winCollector));
        }
        final AnswerAndWin greedyDecision = getGreedyDecision(awList, winCalculator);
        return greedyDecision.winCollector.catchBrokenProbabilities();
    }

    private CellType getOppCellType() {
        return botCellType == CellType.CROSSES ? CellType.NOUGHTS : CellType.CROSSES;
    }


    private AnswerAndWin getGreedyDecision(final List<AnswerAndWin> awList,
                                           final ToDoubleFunction<WinCollector> winCalculator) {
        if (awList.size() == 1) {
            return awList.get(0);
        }
        final List<AnswerAndWin> potentialWin = new ArrayList<>();
        potentialWin.add(awList.get(0));
        for (final AnswerAndWin tmpDecision : awList.subList(1, awList.size())) {
            final double bestWin = winCalculator.applyAsDouble(potentialWin.get(0).winCollector);
            final double tmpWin = winCalculator.applyAsDouble(tmpDecision.winCollector);
            if (bestWin < tmpWin) {
                potentialWin.clear();
                potentialWin.add(tmpDecision);
            } else if (Math.abs(bestWin - tmpWin) < EPS) {
                potentialWin.add(tmpDecision);
            }
        }
        final int idx = rnd.nextInt(potentialWin.size());
        return potentialWin.get(idx);
    }
}
