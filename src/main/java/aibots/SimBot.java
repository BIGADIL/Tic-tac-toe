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
import java.util.function.Function;

/**
 * Симуляционный робот.
 * Использует жадную стратегию для своих решений.
 * Использует смешанную стратегию для противников.
 */
public class SimBot extends BaseBot {
    public static class SimBotFactory extends BaseBotFactory {
        @Override
        public SimBot createBot(final CellType botCellType) {
            final String name = "SimBot-" + botIdx++;
            return new SimBot(name, botCellType);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SimBot.class);

    public static final IBotFactory factory = new SimBot.SimBotFactory();

    private final EndOfGameDetector eogDetector = new EndOfGameDetector();
    private final boolean isPrintLogs = true;

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

    public SimBot(final String name,
                  final CellType botCellType) {
        super(name, botCellType);

    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        totalNodes = 0;
        terminalNodes = 0;
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
        return getGreedyDecision(awList).answer;
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
        final CellType simCellType;
        final Function<List<AnswerAndWin>, AnswerAndWin> winCalculator;

        if (nextPlayerId == id) {
            simCellType = botCellType;
            winCalculator = this::getGreedyDecision;
        } else {
            simCellType = getOppCellType();
            winCalculator = this::getMixedDecision;
        }
        final List<AnswerAndWin> aw = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            totalNodes++;
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, simCellType);
            final ImplBoard copy = board.getCopy(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy);
            aw.add(new AnswerAndWin(aiAnswer, winCollector));
        }
        return winCalculator.apply(aw).winCollector.catchBrokenProbabilities();
    }

    private CellType getOppCellType() {
        return botCellType == CellType.CROSSES ? CellType.NOUGHTS : CellType.CROSSES;
    }

    private AnswerAndWin getMixedDecision(final List<AnswerAndWin> awList) {
        final double sequenceLength = (double) awList.size();
        final double p = 1.0D / sequenceLength;
        final WinCollector mixedWin = new WinCollector(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        for (final AnswerAndWin aw : awList) {
            mixedWin.add(aw.winCollector, p);
        }
        return new AnswerAndWin(null, mixedWin);
    }

    private AnswerAndWin getGreedyDecision(final List<AnswerAndWin> awList) {
        if (awList.size() == 1) {
            return awList.get(0);
        }
        AnswerAndWin bestDecision = awList.get(0);
        for (final AnswerAndWin tmpDecision : awList.subList(1, awList.size())) {
            final double bestTotalWin = bestDecision.winCollector.getTotalWin();
            final double tmpTotalWin = tmpDecision.winCollector.getTotalWin();
            final double bestWin = bestDecision.winCollector.win;
            final double tmpWin = tmpDecision.winCollector.win;
            if (tmpTotalWin > bestTotalWin || (Math.abs(tmpTotalWin - bestTotalWin) < EPS && tmpWin > bestWin)) {
                bestDecision = tmpDecision;
            }
        }
        return bestDecision;
    }
}
