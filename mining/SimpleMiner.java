package blockchain.mining;

import blockchain.blockchain.Block;
import blockchain.blockchain.CryptoHelper;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

enum MinerStatus {
    ON_TASK_WAITING_FOR_PARAMS,
    MINING,
    BLOCK_FOUND_WAIT_CONFIRMATION,
    IDLE,
    TERMINATE
}

//    public void miningCompleted() {
//        suspensionFlag = true;
//        transactions = null;
//    }

interface InternalCommand {
    public void execute();
}

public class SimpleMiner implements Runnable {
    final private MinerClient master;
    final private Queue<InternalCommand> commands = new LinkedList<>();
    //    volatile boolean terminationFlag = false;
//    volatile boolean suspensionFlag = false;
    volatile MinerStatus state;
    //    volatile lastlong lastKnownBlockId;
//    volatile long lastKnownTransactionId;
    TransactionPack transactions;
    volatile int lastKnownNetworkComplexity;
    volatile Block lastKnownBlock;

    Block block;

    public SimpleMiner(MinerClient master) {
        this.master = master;
        this.state = MinerStatus.IDLE;
    }

    @Override
    public String toString() {
        return "SimpleMiner{" +
//                "terminationFlag=" + terminationFlag +
//                ", suspensionFlag=" + suspensionFlag +
                ", state=" + state +
                ", transactions=" + transactions +
                ", block=" + block +
                ", networkComplexity=" + lastKnownNetworkComplexity +
                '}';
    }

    private synchronized void resetTransactionsIds() {
        long lastTrId = lastKnownBlock.getTransactionPack().getLastTransactionId() + 1;
        for (Transaction tr : transactions.transactions) {
            tr.transactionId = lastTrId++;
        }
    }

    public boolean isReadyToMine() {
        if ((block != null) &&
                (block.getBlockId() == lastKnownBlock.getBlockId() + 1) &&
                (block.getTransactionPack().getTransactions().size() > 0)) {
            if (transactions != null) {
                if (lastKnownBlock.getTransactionPack().getLastTransactionId() <= transactions.getFirstTransactionId()) {
                    resetTransactionsIds();
                    return true;
                }
            }
        }
        return false;
    }

    public void tryTransitToMining() {
        if (state == MinerStatus.ON_TASK_WAITING_FOR_PARAMS) {
            if (isReadyToMine()) {
                state = MinerStatus.MINING;
            }
        }
    }

    private synchronized void queueCommand(InternalCommand cmd) {
        commands.offer(cmd);
    }

    public boolean isBusy() {
//        return transactions != null;
        return state != MinerStatus.IDLE;
    }

    public void terminate() {
        state = MinerStatus.TERMINATE;
//        terminationFlag = true;
    }

    @Override
    public void run() {
//        System.out.println("stared " + toString());
        master.updateOnMiningNetwork();

        while (state != MinerStatus.TERMINATE) {
//          command loop

            while (!commands.isEmpty()) {
                commands.poll().execute();
            }

            if (state == MinerStatus.ON_TASK_WAITING_FOR_PARAMS) {
                tryTransitToMining();
            }
            if (state == MinerStatus.MINING) {
                while (!CryptoHelper.isHashComplexEnough(block.getBlockHash(), lastKnownNetworkComplexity)) {
                    block.incMagic();
                    if (state != MinerStatus.MINING) {
                        break;
                    }
                    Thread.yield();
                }
                if (state == MinerStatus.MINING) {
                    state = MinerStatus.BLOCK_FOUND_WAIT_CONFIRMATION;
                    master.onBlockFound(block);
                }
            }
        }
        Thread.yield();
    }
//        System.out.println("closed" + toString());

//}

//    public void suspendMining() {
//        suspensionFlag = true;
////        System.out.println("suspend mining: " + toString());
//    }
//
//    public synchronized void stopMining() {
//        suspensionFlag = true;
//        transactions = null;
////        System.out.println("stop mining: " + toString());
//    }
//
//    public void resumeMining() {
//        suspensionFlag = false;
//        Logger.debug("resume mining: " + toString());
//    }

    public void queueResetMiningOnBlockChainChange(Block block, int intParam) {
        queueCommand(new SimpleMiner.ResetMiningOnBlockChainChange(block, intParam));
    }

    public void queueResetMiningOnTransactionPackChanged(TransactionPack tp) {
        queueCommand(new SimpleMiner.ResetMiningOnTransactionPackChanged(tp));
    }

    class ResetMiningOnBlockChainChange implements InternalCommand {
        Block lastKnownBlock;
        int networkComplexity;

        public ResetMiningOnBlockChainChange(Block block, int networkComplexity) {
            this.lastKnownBlock = block;
            this.networkComplexity = networkComplexity;
        }

        public void execute() {
            state = MinerStatus.ON_TASK_WAITING_FOR_PARAMS;
            SimpleMiner.this.lastKnownNetworkComplexity = networkComplexity;
            SimpleMiner.this.lastKnownBlock = lastKnownBlock;

            if ((block == null) || (block.getBlockId() != lastKnownBlock.getBlockId() - 1)) {
                block = new Block(lastKnownBlock.getBlockId() + 1, lastKnownBlock.getBlockHash());
                if (SimpleMiner.this.transactions != null) {
                    block.setTransactionPack(SimpleMiner.this.transactions);
                }
                Random rand = new Random(new Date().getTime());
                block.setMagic(rand.nextInt(Integer.MAX_VALUE) / 2);
            }
            tryTransitToMining();
        }

    }

    public class ResetMiningOnTransactionPackChanged implements InternalCommand {
        private TransactionPack transactions;

        public ResetMiningOnTransactionPackChanged(TransactionPack tpack) {
            this.transactions = tpack;
        }

        public void execute() {
            state = MinerStatus.ON_TASK_WAITING_FOR_PARAMS;
            SimpleMiner.this.transactions = this.transactions;
            SimpleMiner.this.block.setTransactionPack(transactions);
            tryTransitToMining();
        }

    }

}


