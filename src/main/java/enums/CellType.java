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

    public static CellType getRevertType(final CellType myType) {
        if (myType == CROSSES) {
            return NOUGHTS;
        } else if (myType == NOUGHTS) {
            return CROSSES;
        }
        throw new IllegalArgumentException("Unsupported for revert = " + myType);
    }
}