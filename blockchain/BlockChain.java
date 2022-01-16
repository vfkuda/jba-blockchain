package blockchain.blockchain;

import blockchain.mining.Transaction;
import blockchain.mining.TransactionPack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class BlockChain {
    private final List<Block> blocks;
    HashSet<Transaction> transactionIndex = new HashSet<>();

    public BlockChain() {
        this.blocks = new ArrayList<>();
        blocks.add(new Block(0, "0"));
    }

    public int checkBlockValidity(Block block) {
//        block timestamp can't be older than timestamp of the last block in the chain
        if ((block.getTimeStamp() < getLastBlock().getTimeStamp()) ||
                (block.getTimeStamp() > System.currentTimeMillis())) {
            return 1;
        }
//  block hash should match
        if (!Objects.equals(block.getPrevHash(), getLastBlock().getBlockHash())) {
            return 2;
        }
//  there should be no block with same Id
        if (findById(block.getBlockId())) {
            return 3;
        }

//        long lastTransactionId = getLastTransactionPack().getLastTransactionId();
        for (Transaction tr : block.transactionPack.getTransactions()) {
//            if (tr.getTimeStamp() <= getLastBlock().getTimeStamp()) {
//                return 4;
//            }
            if (transactionIndex.contains(tr)) {
                return 5;
            }
            try {
                if (!tr.verify()) {
                    return 6;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 6;
            }
        }
        return 0;
    }

    public void addBlock(Block block) {
        //add Block
        blocks.add(block);
        // index transactions
        for (Transaction tr : block.getTransactionPack().getTransactions()) {
            transactionIndex.add(tr);
        }
    }

    public boolean registerBlock(Block block) {
        if (0 == checkBlockValidity(block)) {
            addBlock(block);
            return true;
        }
        return false;
    }

    // TODO: extend in future to accommodate block's payload
    public Block frameNextBlock() {
        return new Block(getLastBlock().getBlockId() + 1, getLastBlock().getBlockHash());
    }

    public boolean isValid() {
        Block b;
        for (int i = 1; i < blocks.size(); i++) {
            b = blocks.get(i);
            if (checkBlockValidity(b) != 0) {
                return false;
            }
        }
        return true;
    }

    public Block getLastBlock() {
        return blocks.get(getChainSize() - 1);
    }

    public TransactionPack getLastTransactionPack() {
        return getLastBlock().getTransactionPack();
    }

    public int getChainSize() {
        return blocks.size();
    }

//    public void iterateOverNoResult(Consumer<Block> c) {
//        for (Block block : blocks) {
//            c.accept(block);
//        }
//    }

    public boolean findById(long blockId) {
//        TODO: do storage and indexing better
        for (Block b : blocks) {
            if (b.getBlockId() == blockId) {
                return true;
            }
        }
        return false;
    }
}
