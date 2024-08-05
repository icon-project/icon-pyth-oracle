package icon.oracle.score.pyth;

import java.util.Arrays;

import icon.oracle.score.pyth.utils.Constants;

public class Pyth {

    private static String NAME = "ICON Pyth verifier";

    public Pyth(byte[][] _guardians, byte[] _emitterAddress) {
        for (byte[] bs : _guardians) {
            Wormhole.guardians.add(bs);
        }
        Wormhole.emitterAddress.set(_emitterAddress);
    }

    public void updatePriceFeed(byte[][] data) {
        for (byte[] datum : data) {
            parseUpdate(datum);
        }
    }

    public void parseUpdate(byte[] data) {
        byte[] header = Arrays.copyOfRange(data, 0, 4);
        if (Arrays.equals(header, Constants.PYTHNET_ACCUMULATOR_UPDATE_MAGIC)) {
            Accumulator.parse(data);
            return;
        }
        parseBatchAttestation(data);
    }

    public void parseBatchAttestation(byte[] data) {
    }





}


