package foundation.icon.btp.lib.blockupdate;

import foundation.icon.btp.lib.ErrorCode;
import foundation.icon.btp.lib.exception.RelayMessageRLPException;

import score.Context;
import score.ObjectReader;

import java.math.BigInteger;
import java.util.List;

public class BlockUpdate {
    private final BlockHeader blockHeader;
    private final Votes votes;

    public BlockUpdate(byte[] serialized) throws RelayMessageRLPException {
        ObjectReader rlpReader = Context.newByteArrayObjectReader("RLPn", serialized);
        rlpReader.beginList();
        byte[] encodedHeader = rlpReader.readByteArray();
        if (encodedHeader != null && encodedHeader.length > 0) {
            this.blockHeader = new BlockHeader(encodedHeader);
        } else {
            this.blockHeader = null;
        }
        byte[] encodedVotes = rlpReader.readByteArray();
        if (encodedVotes != null && encodedVotes.length > 0) {
            this.votes = Votes.fromBytes(encodedVotes);
        } else {
            this.votes = null;
        }
        rlpReader.end();
    }

    public BlockHeader getBlockHeader() {
        return this.blockHeader;
    }

    public Votes getVotes() {
        return this.votes;
    }

    public void verify(List<byte[]> validators, BigInteger currentSetId) {
        if (this.votes == null) {
            Context.revert(ErrorCode.INVALID_BLOCK_UPDATE, "not exists votes");
        }

        this.votes.verify(this.blockHeader.getNumber(), this.blockHeader.getHash(), validators, currentSetId);
    }

    public static BlockUpdate fromBytes(byte[] serialized) throws RelayMessageRLPException {
        try {
            return new BlockUpdate(serialized);
        } catch (IllegalStateException | UnsupportedOperationException | IllegalArgumentException e) {
            throw new RelayMessageRLPException("BlockUpdate ", e.toString());
        }
    }
}