package blockchain.mining;

import blockchain.Logger;
import blockchain.blockchain.Block;
import blockchain.blockchain.CryptoHelper;

import java.util.Date;
import java.util.Random;

public class BasicMiner implements Runnable {
    final private BlockFound callback;
    volatile int lastKnownNetworkComplexity;

    Block block;
    private volatile boolean terminationFlag;

    public BasicMiner(BlockFound callback, Block lastKnownBlock, int lastKnownNetworkComplexity, TransactionPack transactions) {
        this.callback = callback;
        this.terminationFlag = false;
        this.lastKnownNetworkComplexity = lastKnownNetworkComplexity;
        block = new Block(lastKnownBlock.getBlockId() + 1, lastKnownBlock.getBlockHash());
        block.setTransactionPack(transactions);
        Random rand = new Random(new Date().getTime());
        block.setMagic(rand.nextInt(Integer.MAX_VALUE) / 2);

    }

    @Override
    public String toString() {
        return "BasicMiner{" +
                ", transactions=" + block.getTransactionPack() +
                ", block=" + block +
                ", networkComplexity=" + lastKnownNetworkComplexity +
                '}';
    }


    public void terminate() {
        terminationFlag = true;
    }

    @Override
    public void run() {
        Logger.info("miner started %s", this);
        while (!CryptoHelper.isHashComplexEnough(block.getBlockHash(), lastKnownNetworkComplexity)) {
            if (terminationFlag) {
                break;
            }
            block.incMagic();
            Thread.yield();
        }
        if (CryptoHelper.isHashComplexEnough(block.getBlockHash(), lastKnownNetworkComplexity)) {
            callback.onBlockFound(this, block);
        }
        Thread.yield();
        Logger.info("miner out %s", this);
    }
}
