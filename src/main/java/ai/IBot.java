package ai;

import answer.AIAnswer;
import board.Board;

@FunctionalInterface
interface IBot {
    AIAnswer getAnswer(final Board board);
}