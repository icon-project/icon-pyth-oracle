package icon.oracle.score.pyth;

import java.util.Arrays;

import icon.oracle.score.pyth.structs.ParsedVAA;
import icon.oracle.score.pyth.utils.Errors;
import score.ArrayDB;
import score.Context;
import score.VarDB;

public class Wormhole {
    static ArrayDB<byte[]> guardians = Context.newArrayDB("GUARDIANS", byte[].class);
    static VarDB<byte[]> emitterAddress = Context.newVarDB("EMITTER", byte[].class);

    public static ParsedVAA parseAndVerifyVaa(byte[] data) {
        ParsedVAA vaa = ParsedVAA.deserialize(data);
        Context.require(vaa.version == 1, Errors.InvalidVersion);
        Context.require(vaa.emitterChain == 26, "TODO");
        Context.require(Arrays.equals(vaa.emitterAddress, emitterAddress.get()),"TODO");

        // TODO: do we need to prevent duplicates?
        // if vaa_archive_check(storage, vaa.hash.as_slice()) {
        //     return Err(PythContractError::InvalidMerkleProof)?;
        // }

        // TODO verify guardian expiration date?

        Context.require(vaa.lenSigners >= (((guardians.size()*10/3)*2)/10) +1, Errors.NoQuorum);

        int lastIndex = -1;
        int pos = ParsedVAA.HEADER_LEN;
        for (int i = 0; i < vaa.lenSigners; i++) {
            Context.require(pos + ParsedVAA.SIGNATURE_LEN <= data.length, Errors.InvalidVAA);
            int index = data[pos];
            Context.require(index > lastIndex, Errors.WrongGuardianIndexOrder);
            lastIndex = index;

            byte[] signData = Arrays.copyOfRange(data, pos+ParsedVAA.SIG_DATA_POS, pos+ParsedVAA.SIG_DATA_POS+ParsedVAA.SIG_DATA_LEN+1);
            signData[64] = data[pos + ParsedVAA.SIG_RECOVERY_POS];


            byte[] key = Context.recoverKey("ecdsa-secp256k1", vaa.hash, signData, false);

            Context.require(keyEqual(key, guardians.get(index)), Errors.GuardianSignatureError);
            pos = pos + ParsedVAA.SIGNATURE_LEN;
        }

        return vaa;
    }

    public static boolean keyEqual(byte[] key, byte[] address) {
        byte[] pubKey = Context.hash("keccak-256", Arrays.copyOfRange(key, 1, key.length));
        byte[] recoveredAddress = Arrays.copyOfRange(pubKey, 12, pubKey.length);
        return Arrays.equals(recoveredAddress, address);
    }
}
