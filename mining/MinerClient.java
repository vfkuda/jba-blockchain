package blockchain.mining;

import blockchain.Logger;
import blockchain.blockchain.Block;
import blockchain.media.BasicChainClient;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;

import java.util.*;

public class MinerClient extends BasicChainClient implements BlockFound {
    final static int MIN_TRANSACTIONS_IN_BLOCK = 4;
    final Random rand = new Random();
    final List<Transaction> unconfirmedTransactions = new LinkedList<>();
    final List<Transaction> sentTransactions = new LinkedList<>();
    final Queue<BasicMinerClient.MiningTask> tasks = new LinkedList<>();
    BasicMiner miner;
    //cachced addr
    MediaAddress blockChainClient;
    volatile int lastKnownNetworkComplexity;
    volatile Block lastKnownBlock;
    private Block sentBlock;
//    private boolean flushFlag = false;

    public MinerClient() {
        super();
        sendToBlockChainClient(new DataPack(DataPack.DataPackKind.MINING_NETWORK_UPDATE_REQUEST));

    }

    protected TransactionPack createTransactionPackToMine() {
        List<Transaction> transactions = new ArrayList<>(unconfirmedTransactions);
        transactions.removeAll(sentTransactions);
        Collections.shuffle(transactions);
        TransactionPack tp = new TransactionPack();
        int n = Math.min(transactions.size(), MIN_TRANSACTIONS_IN_BLOCK);
        for (int i = 0; i < n; i++) {
            tp.addTransasction(transactions.get(i));
        }
        return tp;
    }

    protected void terminateMining() {
        if (miner != null) {
            miner.terminate();
            miner = null;
        }
    }

    protected void resetMining() {
        if (sentBlock == null) {
            terminateMining();
            TransactionPack tp = createTransactionPackToMine();
            if (!tp.isEmpty() && lastKnownBlock != null) {
                miner = new BasicMiner(this, lastKnownBlock, lastKnownNetworkComplexity, tp);
                new Thread(miner).start();
            }
        }

    }

    public void sendToBlockChainClient(DataPack pack) {
        if (blockChainClient != null) {
            send(blockChainClient, pack);
        } else {
            broadcast(pack);
        }
    }


    @Override
    public synchronized void onBlockFound(BasicMiner miner, Block block) {
        if (this.miner == miner) {
//            System.out.println(address.toString() + " onfound: blockid " + block.getBlockId());
            sentBlock = block;
            sendToBlockChainClient(new DataPack(DataPack.DataPackKind.BLOCK_SUGGESTION).setBlock(block));
        }
    }

    @Override
    protected void onShutdown() {
        terminateMining();
    }

    @Override
    protected void onIdle() {
        if (miner == null) {
            resetMining();
        }
    }

    @Override
    protected void onPackReceived(DataPack pack) {
        Logger.info("package received by miner %s", this);

        switch (pack.kind) {
            case MINING_REQUEST:
                unconfirmedTransactions.addAll(pack.transactions.getTransactions());
                break;

            case BLOCK_DECISION:
                if (pack.status) {
                    unconfirmedTransactions.removeAll(pack.block.getTransactionPack().getTransactions());
                    sentTransactions.removeAll(pack.block.getTransactionPack().getTransactions());
                    resetMining();
                } else {
                    sentTransactions.removeAll(pack.block.getTransactionPack().getTransactions());
                }
                if (sentBlock == pack.block) {
                    sentBlock = null;
                }

            case MINING_NETWORK_UPDATE:
                blockChainClient = pack.sender;
                lastKnownBlock = pack.block;
                lastKnownNetworkComplexity = pack.intParam;

                resetMining();
        }
    }

    @Override
    public String toString() {
        return "MinerClient{" +
                "unconfirmedTransactions=" + unconfirmedTransactions.size() +
                '}';
    }
}
