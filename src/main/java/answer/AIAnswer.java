package answer;


import enums.CellType;

public class AIAnswer {

    public final int x;
    public final int y;

    public final CellType cellType;

    public AIAnswer(final int x, final int y, final CellType cellType) {
        this.x = x;
        this.y = y;
        this.cellType = cellType;
    }

    @Override
    public String toString() {
        return String.format("[(%d, %d) - %s]", x, y, cellType);
    }
}
