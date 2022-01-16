package blockchain.mining;

import blockchain.Logger;
import blockchain.blockchain.Block;
import blockchain.media.BasicChainClient;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;

import java.util.*;
import java.util.function.Predicate;

public class BasicMinerClient extends BasicChainClient {
    final static int MIN_TRANSACTIONS_IN_BLOCK = 4;
    final Random rand = new Random();
    final List<Transaction> unconfirmedTransactions = new LinkedList<>();
    final Queue<MiningTask> tasks = new LinkedList<>();
    //cachced addr
    MediaAddress blockChainClient;
    volatile int lastKnownNetworkComplexity;
    volatile Block lastKnownBlock;
    private boolean flushFlag;

    public BasicMinerClient() {
        super();
        requestUpdateOnNetworkParameters();
    }

    public void sendToBlockChainClient(DataPack pack) {
        if (blockChainClient != null) {
            send(blockChainClient, pack);
        } else {
            broadcast(pack);
        }
    }

    protected void requestUpdateOnNetworkParameters() {
        sendToBlockChainClient(new DataPack(DataPack.DataPackKind.MINING_NETWORK_UPDATE_REQUEST));
    }

    @Override
    protected void onShutdown() {
    }


    protected TransactionPack createTransaactionPackToMine() {
        TransactionPack tp = new TransactionPack();
        int n = MIN_TRANSACTIONS_IN_BLOCK;
        if (flushFlag) {
            n = unconfirmedTransactions.size();
            flushFlag = false;
        }
        if (n <= unconfirmedTransactions.size()) {
            for (int i = 0; i < n; i++) {
                tp.addTransasction(unconfirmedTransactions.get(i));
            }
            return tp;
        }
        return null;

    }

    @Override
    protected void onIdle() {
//        clean up competed tasks
        while (!tasks.isEmpty() && tasks.peek().state == MiningTaskState.COMPLETED) {
            tasks.poll();
        }

        if (!tasks.isEmpty()) {
            tasks.peek().proceed();
        } else {
            TransactionPack tp = createTransaactionPackToMine();
            if (tp != null) {
                MiningTask task = new MiningTask(tp);
                tasks.add(task);
                task.proceed();
            }
        }
    }

//    private void sendCandidates() {
////        for (Block block : candidateBlocks) {
////            broadcast(new DataPack(DataPack.DataPackKind.BLOCK_SUGGESTION).setBlock(block));
////            sentBlocks.add(block);
////        }
//    }

    public boolean isValidTransactionPack(TransactionPack transactionPack) {
//        all transactions are not confirmed yet
        if (lastKnownBlock != null) {
            for (Transaction tr : transactionPack.getTransactions()) {
                if (!unconfirmedTransactions.contains(tr)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isValidBlock(Block block) {
        if ((block != null) &&
                (block.getBlockId() == lastKnownBlock.getBlockId() + 1) &&
                (block.getTransactionPack().getTransactions().size() > 0) &&
                isValidTransactionPack(block.getTransactionPack())) {
            return true;
        }
        return false;
    }

    public MiningTask getTaskFor(Predicate<MiningTask> test) {
        return tasks.stream().filter(test).findFirst().orElse(null);
    }

    public boolean hasTaskFor(Predicate<MiningTask> test) {
        return getTaskFor(test) != null;
    }

    @Override
    protected void onPackReceived(DataPack pack) {
        Logger.info("package received by miner %s", this);

        MiningTask tsk;
        switch (pack.kind) {
            case MINING_NETWORK_UPDATE:
                blockChainClient = pack.sender;
                lastKnownBlock = pack.block;
                lastKnownNetworkComplexity = pack.intParam;
                if (!tasks.isEmpty()) {
                    tasks.peek().onEventNetworkUpdated();
                }
                break;

//            case MINING_REQUEST_AUCTION:
//                send(pack.sender, new DataPack(DataPack.DataPackKind.MINING_REQUEST_AUCTION_BID).
//                        setTransactions(pack.transactions).
//                        setIntParam(rand.nextInt(1024) + (tasks.size() * 1024)));
//                break;

            case MINING_REQUEST:
                unconfirmedTransactions.addAll(pack.transactions.getTransactions());
                break;

            case BLOCK_DECISION:
                tsk = getTaskFor(x -> x.block == pack.block);
                if (tsk != null) {
                    if (pack.status) {
                        tsk.onEventBlockConfirmed(pack.block);
//                        tasks.remove(tsk);
                    } else {
                        tsk.onWrongBlock(pack.block);
                    }
                } else {
                    if (pack.status) {
                        //убрать все таски из анконфермд
                        unconfirmedTransactions.removeAll(pack.transactions.getTransactions());
                        tasks.peek().checkIfAbleToMine();
                    }
                }

                break;

        }

    }

    @Override
    public String toString() {
        return "MinerClient{" +
                "tasks=" + tasks.size() +
                '}';
    }

    enum MiningTaskState {
        STANDBY,
        READY_TO_MINE,
        MINING,
        //        WAIT_FOR_RESIGN,
//        WAIT_FOR_TRANSACTIONPACK_UPDATE,
        WAIT_FOR_NETWORK_PARAMETERS_UPDATE,
        BLOCK_FOUND,
        BLOCK_FOUND_WAIT_CONFIRMATION,
//        IDLE,
//        INVALID_TRANSACTIONS_LIST,
        COMPLETED,
//        TERMINATED
    }

    class MiningTask implements BlockFound {
        TransactionPack transactions;
        Block block;
        BasicMiner miner;
        volatile MiningTaskState state;
        volatile int lastKnownNetworkComplexity;
        volatile Block lastKnownBlock;

        public MiningTask(TransactionPack transactions) {
            this.transactions = transactions;
            this.lastKnownBlock = BasicMinerClient.this.lastKnownBlock;
            this.lastKnownNetworkComplexity = BasicMinerClient.this.lastKnownNetworkComplexity;
            state = MiningTaskState.STANDBY;
        }


//        public void changeState(MiningTaskState targetState) {
//            if (targetState == MiningTaskState.WAIT_FOR_RESIGN) {
//
//            }
//        }

        public void onWrongBlock(Block block) {
//            if (this.block == block) {
//                if (!isValidTransactionPack(transactions)) {
//                    send(requestor, new DataPack(DataPack.DataPackKind.MINING_REQUEST_NEED_SIGNUP).
//                            setTransactions(transactions));
//                }
//            }
        }

        public void onEventBlockConfirmed(Block block) {
            if (state == MiningTaskState.BLOCK_FOUND && this.block == block) {
                state = MiningTaskState.COMPLETED;
            }
        }

        public void onEventNetworkUpdated() {
            stopMining();
//            if (this.lastKnownBlock!=BasicMinerClient.this.lastKnownBlock) ||
//            this.lastKnownNetworkComplexity || BasicMinerClient.this.lastKnownNetworkComplexity);
            this.lastKnownBlock = BasicMinerClient.this.lastKnownBlock;
            this.lastKnownNetworkComplexity = BasicMinerClient.this.lastKnownNetworkComplexity;
            if (state == MiningTaskState.WAIT_FOR_NETWORK_PARAMETERS_UPDATE) {
                state = MiningTaskState.STANDBY;
            }
//            startMiningIfReady();
        }

//        public void onEventTransactionPackUpdated(TransactionPack tp) {
//            stopMining();
//            if (state == MiningTaskState.WAIT_FOR_RESIGN) {
//                state = MiningTaskState.STANDBY;
//            }
//            this.transactions = tp;
////            startMiningIfReady();
//        }

        public void proceed() {
            checkIfAbleToMine();
            transitToMining();
        }

        public void terminate() {
            stopMining();
            state = MiningTaskState.COMPLETED;
        }

        public void stopMining() {
            if (miner != null) {
                miner.terminate();
                miner = null;
            }
        }

//        private void resetTransactionsIds() {
//            long lastTrId = lastKnownBlock.getTransactionPack().getLastTransactionId() + 1;
//            for (Transaction tr : transactions.transactions) {
//                tr.transactionId = lastTrId++;
//            }
//        }

        public void checkIfAbleToMine() {
            if (state != MiningTaskState.WAIT_FOR_NETWORK_PARAMETERS_UPDATE) {
                if (lastKnownBlock == null) {
                    requestUpdateOnNetworkParameters();
                    state = MiningTaskState.WAIT_FOR_NETWORK_PARAMETERS_UPDATE;
                } else if (!isValidTransactionPack(transactions)) {
                    terminate();
                }
            }
        }

        public void transitToMining() {
            if (state == MiningTaskState.STANDBY) {
                state = MiningTaskState.MINING;
                miner = new BasicMiner(this, lastKnownBlock, lastKnownNetworkComplexity, transactions);
                new Thread(miner).start();
            }
        }

        @Override
//        event
        public synchronized void onBlockFound(BasicMiner miner, Block block) {
            if (this.miner == miner) {
                state = MiningTaskState.BLOCK_FOUND;
                this.block = block;
                stopMining();
                sendToBlockChainClient(new DataPack(DataPack.DataPackKind.BLOCK_SUGGESTION).setBlock(block));
            }
        }
    }


}
