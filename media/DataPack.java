package blockchain.media;

import blockchain.blockchain.Block;
import blockchain.mining.TransactionPack;

public class DataPack {
    public MediaAddress sender;
    public DataPackKind kind;
    //    public Transaction transaction;
    public TransactionPack transactions;
    public boolean status;
    public Block block;
    public int intParam;

    public DataPack(DataPackKind kind) {
        this.kind = kind;
    }

    public static DataPack create(DataPackKind kind) {
        return new DataPack(kind);
    }

    public static DataPack DUMMY() {
        return new DataPack(DataPackKind.DUMMY);
    }

    @Override
    public String toString() {
        return "DataPack{" +
                "sender=" + (sender != null ? sender.toString() : "?") +
                ", kind=" + kind +
                ", transactions=" + transactions +
                ", status=" + status +
                ", blockId=" + (block != null ? block.getBlockId() : -1) +
                ", intParam=" + intParam +
                '}';
    }

    public DataPack setTransactions(TransactionPack transactions) {
        this.transactions = transactions;
        return this;
    }

    public DataPack setSender(MediaAddress sender) {
        this.sender = sender;
        return this;
    }

    public DataPack setKind(DataPackKind kind) {
        this.kind = kind;
        return this;
    }

    public DataPack setStatus(boolean status) {

        this.status = status;
        return this;
    }

    public DataPack setBlock(Block block) {
        this.block = block;
        return this;
    }

    public DataPack setIntParam(int intParam) {
        this.intParam = intParam;
        return this;
    }

    public enum DataPackKind {
        DUMMY,
        MEDIA_OFF,
        MINING_REQUEST_AUCTION,
        MINING_REQUEST_AUCTION_BID,
        MINING_REQUEST_NEED_SIGNUP,
        MINING_REQUEST_SIGN_UPDATED,
        MINING_REQUEST,
        MINING_REQUEST_STATUS,
        BLOCK_SUGGESTION,
        BLOCK_DECISION,
        MINING_NETWORK_UPDATE,
        MINING_NETWORK_UPDATE_REQUEST
    }
}
