package blockchain.blockchain;

import blockchain.mining.TransactionPack;

public class Block {
    TransactionPack transactionPack;
    private long timeStamp;
    private long blockId;
    //    TODO: redo on byte[] and separate formatting
    private String prevHash;
    private BlockChain blockChain;
    //
    private long magic;

    public Block(long blockId, String prevHash) {
//        this.timeStamp = new Date().getTime();
        this.timeStamp = System.currentTimeMillis();
        this.transactionPack = new TransactionPack();
        this.blockId = blockId;
        this.prevHash = prevHash;
    }

    public BlockChain getBlockChain() {
        return blockChain;
    }

    public void setBlockChain(BlockChain blockChain) {
        this.blockChain = blockChain;
    }

    public long getMagic() {
        return magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }

    public void incMagic() {
        this.magic++;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public String getBlockHash() {
        return CryptoHelper.applySha256(this.getBlockData(this.magic));
    }

    public String getBlockHash(long withMagic) {
        return CryptoHelper.applySha256(this.getBlockData(withMagic));
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public String getPayload() {
        return transactionPack.toString();
    }

    protected String getBlockData(long magic) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getTimeStamp());
        sb.append(this.getBlockId());
        sb.append(this.getPrevHash());
        sb.append(magic);
        sb.append(this.getPayload());
        return sb.toString();
    }

    public TransactionPack getTransactionPack() {
        return transactionPack;
    }

    public void setTransactionPack(TransactionPack transactionPack) {
        this.transactionPack = transactionPack;
    }

}
