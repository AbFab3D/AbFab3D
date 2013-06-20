package abfab3d.grid;

/**
 * Store the state information in the low order bit.
 *
 * @author Alan Hudson
 */
public class StoredInsideOutsideFuncFactory {
    public static InsideOutsideFunc create(int stateBits, int attributeBits) {
        int total_bits = stateBits + attributeBits;

        if (total_bits < 9) {
            return new ByteStoredInsideOutsideFunc(stateBits, attributeBits);
        } else if (total_bits < 17) {
            return new ShortStoredInsideOutsideFunc(stateBits, attributeBits);
        }

        return null;
    }

    static class ByteStoredInsideOutsideFunc implements InsideOutsideFunc {
        private int attributeBits;
        private byte signMask;
        private byte matMask;

        public ByteStoredInsideOutsideFunc(int stateBits, int attributeBits) {

            if (stateBits + attributeBits > 8) {
                throw new IllegalArgumentException("Too many bits");
            }
            this.attributeBits = attributeBits;
            signMask = (byte) ((1 << (stateBits + attributeBits)) - 1);  // (stateBits + attributeBits) 1's
            matMask = (byte) ((1 << attributeBits) - 1);   // attributeBites 1's
        }

        @Override
        public final byte getState(long encoded) {
            return (byte) ((encoded & signMask) >>> attributeBits);
        }

        @Override
        public final long getAttribute(long store) {
            return (matMask & (byte)store);
        }

        @Override
        public long combineStateAndAttribute(byte state, long attribute) {
            return (signMask & (state << attributeBits | (byte)attribute));
        }

        @Override
        public long updateAttribute(long encoded, long attribute) {
            byte state = (byte) ((encoded & signMask) >>> attributeBits);

            return (signMask & (state << attributeBits | (byte)attribute));
        }

    }

    static class ShortStoredInsideOutsideFunc implements InsideOutsideFunc {
        private int attributeBits;
        private short signMask;
        private short matMask;

        public ShortStoredInsideOutsideFunc(int stateBits, int attributeBits) {
            if (stateBits + attributeBits > 16) {
                throw new IllegalArgumentException("Too many bits");
            }

            this.attributeBits = attributeBits;
            signMask = (short) ((1 << (stateBits + attributeBits)) - 1);  // (stateBits + attributeBits) 1's
            matMask = (short) ((1 << attributeBits) - 1);   // attributeBites 1's
        }

        @Override
        public final byte getState(long encoded) {
            return (byte) ((encoded & signMask) >>> attributeBits);
        }

        @Override
        public final long getAttribute(long store) {
            return (matMask & (short)store);
        }

        @Override
        public long combineStateAndAttribute(byte state, long attribute) {
            return (signMask & (state << attributeBits | (short)attribute));
        }

        @Override
        public long updateAttribute(long encoded, long attribute) {
            short state = (short) ((encoded & signMask) >>> attributeBits);

            return (signMask & (state << attributeBits | (short)attribute));
        }

    }

    static class IntStoredInsideOutsideFunc implements InsideOutsideFunc {
        private int attributeBits;
        private int signMask;
        private int matMask;

        public IntStoredInsideOutsideFunc(int stateBits, int attributeBits) {
            if (stateBits + attributeBits > 32) {
                throw new IllegalArgumentException("Too many bits");
            }

            this.attributeBits = attributeBits;
            signMask = (int) ((1 << (stateBits + attributeBits)) - 1);  // (stateBits + attributeBits) 1's
            matMask = (int) ((1 << attributeBits) - 1);   // attributeBites 1's
        }

        @Override
        public final byte getState(long encoded) {
            return (byte) ((encoded & signMask) >>> attributeBits);
        }

        @Override
        public final long getAttribute(long store) {
            return (matMask & (int)store);
        }

        @Override
        public long combineStateAndAttribute(byte state, long attribute) {
            return (signMask & (state << attributeBits | (int)attribute));
        }

        @Override
        public long updateAttribute(long encoded, long attribute) {
            int state = (int) ((encoded & signMask) >>> attributeBits);

            return (signMask & (state << attributeBits | (int)attribute));
        }

    }

    static class LongStoredInsideOutsideFunc implements InsideOutsideFunc {
        private int attributeBits;
        private long signMask;
        private long matMask;

        public LongStoredInsideOutsideFunc(int stateBits, int attributeBits) {
            if (stateBits + attributeBits > 64) {
                throw new IllegalArgumentException("Too many bits");
            }

            this.attributeBits = attributeBits;
            signMask = (int) ((1 << (stateBits + attributeBits)) - 1);  // (stateBits + attributeBits) 1's
            matMask = (int) ((1 << attributeBits) - 1);   // attributeBites 1's
        }

        @Override
        public final byte getState(long encoded) {
            return (byte) ((encoded & signMask) >>> attributeBits);
        }

        @Override
        public final long getAttribute(long store) {
            return (matMask & (long)store);
        }

        @Override
        public long combineStateAndAttribute(byte state, long attribute) {
            return (signMask & (state << attributeBits | (long)attribute));
        }

        @Override
        public long updateAttribute(long encoded, long attribute) {
            long state = (long) ((encoded & signMask) >>> attributeBits);

            return (signMask & (state << attributeBits | attribute));
        }
    }
}

