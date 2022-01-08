package blockchain.mining;

import blockchain.Logger;
import blockchain.blockchain.Block;
import blockchain.media.DataPack;
import blockchain.media.MediaClient;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MinerClient extends MediaClient {
    final Random rand = new Random();
    Queue<TransactionPack> transactions = new LinkedList<>();
    Queue<Block> sentBlocks = new LinkedList<>();
    SimpleMiner miner;

    public MinerClient() {
        super();
        miner = new SimpleMiner(this);
        new Thread(miner).start();
    }

    public void onBlockFound(Block block) {
        sentBlocks.offer(block);
        broadcast(new DataPack(DataPack.DataPackKind.BLOCK_SUGGESTION).setBlock(block));
    }

    protected void updateOnMiningNetwork() {
        broadcast(new DataPack(DataPack.DataPackKind.MINING_NETWORK_UPDATE_REQUEST));
    }

    @Override
    protected void onShutdown() {
        miner.terminate();
//        super.onShutdown();
    }

    @Override
    protected void onIdle() {
        if (!miner.isBusy() && !transactions.isEmpty()) {
            miner.queueResetMiningOnTransactionPackChanged(transactions.peek());
        }
    }

    @Override
    protected void onPackReceived(DataPack pack) {
        Logger.info("package received by miner %s", this);

        switch (pack.kind) {
            case MINING_REQUEST_AUCTION:
                send(pack.sender, new DataPack(DataPack.DataPackKind.MINING_REQUEST_AUCTION_BID).
                        setTransactions(pack.transactions).
                        setIntParam(rand.nextInt((int) 1000000.0 / (transactions.size() + 1))));
                break;
            case MINING_REQUEST:
                transactions.add(pack.transactions);
                break;

            case MINING_REQUEST_STATUS:
//                if (pack.status) {
//                    transactions.remove(pack.transactions);
//                }

//                if (miner.transactions == pack.transactions) {
////                    miner.state = MinerStatus.IDLE;
////                    miner.stopMining();
//                }
                break;

            case BLOCK_DECISION:
                if (sentBlocks.contains(pack.block)) {
                    sentBlocks.remove(pack.block);
                    if (pack.status) {
                        transactions.remove(pack.block.getTransactionPack());
//                        miner.transitToIdle()
                        miner.state = MinerStatus.IDLE;
                    } else {
                        if (pack.block != miner.block) {
                            miner.state = MinerStatus.MINING;
                        } else {
                            miner.state = MinerStatus.ON_TASK_WAITING_FOR_PARAMS;
                        }
                    }
                }
//                else {
////                    some other block change means block will change shortly
//                    miner.state = MinerStatus.WAITING_TASK_PARAMS;
//                }
                break;

            case MINING_NETWORK_UPDATE:
                miner.queueResetMiningOnBlockChainChange(pack.block, pack.intParam);
                break;
//            default:
//                DUMMY:
        }

    }

    @Override
    public String toString() {
        return "MinerClient{" +
                "transactions=" + transactions.size() +
                ", miner=" + miner.toString() +
                '}';
    }
}
