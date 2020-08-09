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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Робот, играющий по принципу минимакса.
 * Решение робота принимается с целья максимизации своих очков.
 * Решение оппонента принимается с целью минимизации выигрыша робота.
 * Добавлен механизм альфа-бета отсечений.
 */
public class MinMaxWithABBot extends BaseBot {
    public static class MaxMaxBotFactory extends BaseBotFactory {
        @Override
        public MinMaxWithABBot createBot(final CellType botCellType) {
            final String name = "MinMaxWithABBot-" + botIdx++;
            return new MinMaxWithABBot(name, botCellType);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MinMaxWithABBot.class);

    public static final IBotFactory factory = new MinMaxWithABBot.MaxMaxBotFactory();

    private final EndOfGameDetector eogDetector;
    private final MonteCarloEvaluator mcEvaluator;
    private final Random rnd = new Random();
    private final int maxRecLevel = 5;
    private final int numSimulatedNodes = 10;

    private long genTime = 0L;
    private int terminatedNodes = 0;

    public MinMaxWithABBot(final String name,
                           final CellType myType) {
        super(name, myType);
        eogDetector = new EndOfGameDetector();
        mcEvaluator = new MonteCarloEvaluator(eogDetector);
    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        terminalNodes = 0;
        totalNodes = 0;
        terminatedNodes = 0;
        final long startTime = System.currentTimeMillis();
        final List<Coord> coordToAct = board.getAllPossibleCoordToAct();
        final int recLevel = 0;
        WinCollector alpha = new WinCollector(
                0.0D,
                0.0D,
                Double.NEGATIVE_INFINITY,
                1.0D,
                0.0D,
                0.0D);

        final WinCollector beta = new WinCollector(
                Double.POSITIVE_INFINITY,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                0.0D);

        AIAnswer bestAnswer = null;
        for (final Coord coord : coordToAct) {
            totalNodes++;
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, botCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy, alpha, beta, recLevel);
            logger.info("--{}: {} -> {}", name, aiAnswer, winCollector);
            if (winCollector.getTotalWin() > alpha.getTotalWin()) {
                alpha = winCollector;
                bestAnswer = aiAnswer;
            }
        }
        genTime = System.currentTimeMillis() - startTime;
        logger.info("--{}: total num nodes = {}", name, totalNodes);
        logger.info("--{}: terminal num nodes = {}", name, terminalNodes);
        logger.info("--{}: terminated num nodes = {}", name, terminatedNodes);
        logger.info("--{}: genTime = {} millis", name, genTime);
        logger.info("");
        return bestAnswer;
    }

    private WinCollector getWinByGameTree(final ImplBoard board,
                                          final WinCollector alpha,
                                          final WinCollector beta,
                                          final int recLevel) {
        WinCollector alphaCopy = alpha.getCopy();
        WinCollector betaCopy = beta.getCopy();
        final int nextPlayerId = board.getNextPlayerId();
        final CellType simCellType = nextPlayerId == id ? botCellType : getOppCellType();
        final WinnerType winnerType = eogDetector.detectWinner(board);
        if (winnerType != WinnerType.NONE) {
            return distributeWin(winnerType).catchBrokenProbabilities();
        }
        if (recLevel == maxRecLevel) {
            final WinCollector termNodeWinCollector = distributeMaxRecNode(board, simCellType);
            return termNodeWinCollector.catchBrokenProbabilities();
        }
        final List<Coord> coordToAct = randomDropout(board.getAllPossibleCoordToAct());
        if (nextPlayerId == id) {
            for (final Coord coord : coordToAct) {
                totalNodes++;
                final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, simCellType);
                final ImplBoard copy = board.getCopy(aiAnswer);
                final WinCollector winCollector = getWinByGameTree(copy, alphaCopy, betaCopy, recLevel + 1);
                if (winCollector.getTotalWin() > alphaCopy.getTotalWin()) {
                    alphaCopy = winCollector;
                }
                if (alphaCopy.getTotalWin() >= betaCopy.getTotalWin()) {
                    break;
                }
            }
            return alphaCopy;
        } else {
            for (final Coord coord : coordToAct) {
                totalNodes++;
                final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, simCellType);
                final ImplBoard copy = board.getCopy(aiAnswer);
                final WinCollector winCollector = getWinByGameTree(copy, alphaCopy, betaCopy, recLevel + 1);
                if (winCollector.getTotalWin() < betaCopy.getTotalWin()) {
                    betaCopy = winCollector;
                }
                if (alphaCopy.getTotalWin() >= betaCopy.getTotalWin()) {
                    break;
                }

            }
        }
        return betaCopy;
    }

    private List<Coord> randomDropout(final List<Coord> coordToAct) {
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
        terminatedNodes++;
        final int iterations = 30;
        return mcEvaluator.evaluateWin(board, simCellType, botWinnerType, iterations);
    }
}
