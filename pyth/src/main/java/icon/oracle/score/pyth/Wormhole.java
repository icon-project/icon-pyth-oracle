package icon.oracle.score.pyth;

import java.util.Arrays;

import icon.oracle.score.pyth.structs.ParsedVAA;
import icon.oracle.score.pyth.utils.Checks;
import icon.oracle.score.pyth.utils.Errors;
import score.ArrayDB;
import score.Context;
import score.VarDB;

public class Wormhole {
    public static final ArrayDB<byte[]> guardians = Context.newArrayDB("GUARDIANS", byte[].class);
    public static final VarDB<byte[]> emitterAddress = Context.newVarDB("EMITTER", byte[].class);

    public static void setGuardians(byte[][] _guardians) {
        // TODO: Verify replacement amount/rules
        int nrGuardians = guardians.size();
        for (int i = 0; i < nrGuardians; i++) {
            guardians.pop();
        }

        for (byte[] bs : _guardians) {
            guardians.add(bs);
        }
    }

    public static byte[][] getGuardians() {
        int nrGuardians = guardians.size();
        byte[][] gs = new byte[20][nrGuardians];
        for (int i = 0; i < nrGuardians; i++) {
            gs[i] = guardians.get(i);
        }

        return gs;
    }

    public static void setEmitter(byte[] _emitter) {
        Checks.onlyOwner();
        emitterAddress.set(_emitter);
    }

    public static byte[] getEmitter() {
        return emitterAddress.get();
    }

    public ParsedVAA parseAndVerifyVaa(byte[] data) {
        ParsedVAA vaa = ParsedVAA.deserialize(data);
        Context.require(vaa.version == 1, Errors.InvalidVersion);
        Context.require(vaa.emitterChain == 26, Errors.InvalidEmitter);
        Context.require(Arrays.equals(vaa.emitterAddress, emitterAddress.get()), Errors.InvalidEmitter);

        // TODO: do we need to prevent duplicates?
        // if vaa_archive_check(storage, vaa.hash.as_slice()) {
        //     return Err(PythContractError::InvalidMerkleProof)?;
        // }

        // TODO verify guardian expiration date?
        Context.require(vaa.lenSigners >= (((guardians.size()*10/3)*2)/10) +1, Errors.NoQuorum);

        int lastIndex = -1;
        int pos = ParsedVAA.HEADER_LEN;
        byte[] signData = new byte[65];
        byte[] key = new byte[64];
        for (int i = 0; i < vaa.lenSigners; i++) {
            Context.require(pos + ParsedVAA.SIGNATURE_LEN <= data.length, Errors.InvalidVAA);
            int index = data[pos];
            Context.require(index > lastIndex, Errors.WrongGuardianIndexOrder);
            lastIndex = index;

            System.arraycopy(data, pos+ParsedVAA.SIG_DATA_POS, signData, 0, ParsedVAA.SIG_DATA_LEN+1);
            System.arraycopy(Context.recoverKey("ecdsa-secp256k1", vaa.hash, signData, false), 1, key, 0, 64);

            Context.require(keyEqual(key, guardians.get(index)), Errors.GuardianSignatureError);
            pos = pos + ParsedVAA.SIGNATURE_LEN;
        }

        return vaa;
    }

    public boolean keyEqual(byte[] key, byte[] address) {
        byte[] pubKey = Context.hash("keccak-256", key);
        byte[] recoveredAddress = Arrays.copyOfRange(pubKey, 12, pubKey.length);
        return Arrays.equals(recoveredAddress, address);
    }


}
