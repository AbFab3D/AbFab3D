package abfab3d.grid;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class DefaultInsideOutsideFunc implements InsideOutsideFunc {
    @Override
    public final byte getState(final long attribute) {
        if (attribute > 0) return Grid.INSIDE;

        return Grid.OUTSIDE;
    }

    @Override
    public final long getAttribute(final long attribute) {
        return attribute;
    }

    @Override
    public final long combineStateAndAttribute(final byte state, long attribute) {
        // preserve inside/outside test
        if (state == Grid.INSIDE && attribute == 0) {
            attribute = 1;
        } else if (state == Grid.OUTSIDE && attribute != 0) {
            attribute = 0;
        }

        return attribute;
    }

    @Override
    public final long updateAttribute(final long encoded, final long attribute) {
        return attribute;
    }
}
