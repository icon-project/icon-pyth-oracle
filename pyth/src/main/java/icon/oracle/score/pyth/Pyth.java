package icon.oracle.score.pyth;

import icon.oracle.score.pyth.structs.Price;
import icon.oracle.score.pyth.utils.Checks;
import score.Context;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

public class Pyth {

    private static String NAME = "ICON Pyth verifier";

    public Pyth(@Optional byte[][] _guardians, @Optional byte[] _emitterAddress) {
        if (_guardians != null) {
            Wormhole.setGuardians(_guardians);
        }

        if (_emitterAddress != null) {
            Wormhole.setEmitter(_emitterAddress);
        }
    }

    @External(readonly = true)
    public String name() {
        return NAME;
    }

    @External
    public void updatePriceFeed(byte[][] data) {
        for (byte[] datum : data) {
            parseUpdate(datum);
        }
    }

    @External
    public void parseUpdate(byte[] data) {
        new Accumulator().parse(data);
    }

    @External(readonly = true)
    public Price getPrice(byte[] id) {
        return Accumulator.getPrice(id);
    }

    @External
    public void setGuardians(byte[][] _guardians) {
        Checks.onlyOwner();
        Wormhole.setGuardians(_guardians);
    }

    @External(readonly = true)
    public byte[][] getGuardians() {
        return Wormhole.getGuardians();
    }

    @External
    public void setEmitter(byte[] _emitter) {
        Checks.onlyOwner();
        Wormhole.setEmitter(_emitter);
    }

    @External(readonly = true)
    public byte[] getEmitter() {
        return Wormhole.getEmitter();
    }

    @Payable
    public void fallback() {
    }


}


