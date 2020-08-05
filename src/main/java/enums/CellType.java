package enums;

public enum CellType {
    EMPTY(' '),
    NOUGHTS('0'),
    CROSSES('x'),
    ;

    public final char code;

    CellType(final char code) {
        this.code = code;
    }

    public boolean isPlayingCellType() {
        return this == NOUGHTS || this == CROSSES;
    }
}