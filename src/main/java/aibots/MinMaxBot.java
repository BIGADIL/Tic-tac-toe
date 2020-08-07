package aibots;

import aibots.common.WinCollector;
import aibots.evaluators.MonteCarloEvaluator;
import answer.AIAnswer;
import board.Coord;
import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;
import listeners.EndOfGameDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * Робот, играющий по принципу минимакса.
 * Решение робота принимается с целья максимизации своих очков.
 * Решение оппонента принимается с целью минимизации выигрыша робота.
 * Фактически цель обоих оппонентов - максимизировать свой выигрыш (поэтому до этого он назывался MaxMax,
 * только не предполагалось, что игра идет с нулевой суммой).
 */
public class MinMaxBot extends BaseBot {
    public static class MaxMaxBotFactory extends BaseBotFactory {
        @Override
        public MinMaxBot createBot(final CellType botCellType) {
            final String name = "MinMaxBot-" + botIdx++;
            return new MinMaxBot(name, botCellType);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MinMaxBot.class);

    public static final IBotFactory factory = new MinMaxBot.MaxMaxBotFactory();

    private final EndOfGameDetector eogDetector;
    private final MonteCarloEvaluator mcEvaluator;
    private final boolean isPrintLogs = true;
    private final Random rnd = new Random();

    private long genTime = 0L;

    private final int maxRecLevel = 4;

    private final int numSimulatedNodes = 7;

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

    public MinMaxBot(final String name,
                     final CellType myType) {
        super(name, myType);
        eogDetector = new EndOfGameDetector();
        mcEvaluator = new MonteCarloEvaluator(eogDetector);
    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        terminalNodes = 0;
        totalNodes = 0;
        final long startTime = System.currentTimeMillis();
        final List<Coord> coordToAct = board.getAllPossibleCoordToAct();
        final List<AnswerAndWin> awList = new ArrayList<>();
        final int recLevel = 0;
        for (final Coord coord : coordToAct) {
            totalNodes++;
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, botCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy, recLevel);
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

    private WinCollector getWinByGameTree(final ImplBoard board, final int recLevel) {
        final ToDoubleFunction<WinCollector> winCalculator;
        final CellType simCellType;
        final int nextPlayerId = board.getNextPlayerId();
        if (nextPlayerId == id) {
            winCalculator = WinCollector::getTotalWin;
            simCellType = botCellType;
        } else {
            winCalculator = w -> -w.getTotalWin();
            simCellType = getOppCellType();
        }
        final WinnerType winnerType = eogDetector.detectWinner(board);
        if (winnerType != WinnerType.NONE) {
            return distributeWin(winnerType).catchBrokenProbabilities();
        }
        if (recLevel == maxRecLevel) {
            final WinCollector termNodeWinCollector = distributeMaxRecNode(board, simCellType);
            return termNodeWinCollector.catchBrokenProbabilities();
        }
        final List<Coord> coordToAct = getCutCoordToAct(board.getAllPossibleCoordToAct());
        final List<AnswerAndWin> awList = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            totalNodes++;
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, simCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy, recLevel + 1);
            awList.add(new AnswerAndWin(aiAnswer, winCollector));
        }
        final AnswerAndWin greedyDecision = getGreedyDecision(awList, winCalculator);
        return greedyDecision.winCollector.catchBrokenProbabilities();
    }

    private List<Coord> getCutCoordToAct(final List<Coord> coordToAct) {
        if (coordToAct.size() <= numSimulatedNodes) {
            return coordToAct;
        }
        final List<Coord> result = new LinkedList<>();
        for (int i = 0; i < numSimulatedNodes; i++) {
            final int idx = rnd.nextInt(coordToAct.size());
            result.add(coordToAct.remove(idx));
        }
        return result;
    }

    private CellType getOppCellType() {
        return botCellType == CellType.CROSSES ? CellType.NOUGHTS : CellType.CROSSES;
    }

    private WinCollector distributeMaxRecNode(final ImplBoard board, final CellType simCellType) {
        final int iterations = 30;
        return mcEvaluator.evaluateWin(board, simCellType, botWinnerType, iterations);
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
