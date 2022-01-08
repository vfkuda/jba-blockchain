package blockchain.mining;

import blockchain.Logger;
import blockchain.blockchain.Block;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.media.MediaClient;

import java.util.*;
import java.util.function.Predicate;

public class BasicMinerClient extends MediaClient {
    final Random rand = new Random();
    //    Queue<TransactionPack> transactions = new LinkedList<>();
    //    Queue<Block> sentBlocks = new LinkedList<>();
//    List<Block> candidateBlocks = new ArrayList<>();
//    ExecutorService executorService;
    Queue<MiningTask> tasks = new LinkedList<>();
    MediaAddress blockChainClient;

    volatile int lastKnownNetworkComplexity;
    volatile Block lastKnownBlock;

    public BasicMinerClient() {
        super();
//        executorService = Executors.newSingleThreadExecutor();
//        ExecutorService executorService2 = Executors.newFixedThreadPool(10);
//        miner = new BasicMiner(this);
//        new Thread(miner).start();
        requestUpdateOnNetworkParameters();
    }

//    public synchronized void addBlockCandidate(Block block) {
//        candidateBlocks.add(block);
//    }

//    private void launchMiner() {
//        BasicMiner miner = new BasicMiner(this, lastKnownBlock, lastKnownNetworkComplexity, transactions.peek());
//        new Thread(miner).start();
//    }

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

    private void requestTransactionsResign(TransactionPack transactions) {
        MiningTask task = getTaskFor(x -> x.transactions == transactions);
        if (task != null) {
            send(task.requestor, new DataPack(DataPack.DataPackKind.MINING_REQUEST_NEED_SIGNUP).
                    setTransactions(transactions));
        }
    }

//    SimpleMiner miner;

    @Override
    protected void onShutdown() {
//        miner.terminate();
//        super.onShutdown();
    }

    @Override
    protected void onIdle() {
//        while (candidateBlocks.size() > 0 && !isValidBlock(candidateBlocks.get(0))) {
//            candidateBlocks.remove(0);
//        }
//        sendCandidates();
        while (!tasks.isEmpty() && tasks.peek().state == MiningTaskState.COMPLETED) {
            tasks.poll();
        }
        if (!tasks.isEmpty()) {
            tasks.peek().proceed();
        }

    }

    private void sendCandidates() {
//        for (Block block : candidateBlocks) {
//            broadcast(new DataPack(DataPack.DataPackKind.BLOCK_SUGGESTION).setBlock(block));
//            sentBlocks.add(block);
//        }
    }

    public boolean isValidTransactionPack(TransactionPack transactionPack) {
        if (lastKnownBlock != null) {
            return transactionPack.getFirstTransactionId() > lastKnownBlock.getTransactionPack().getLastTransactionId();
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

            case MINING_REQUEST_AUCTION:
                send(pack.sender, new DataPack(DataPack.DataPackKind.MINING_REQUEST_AUCTION_BID).
                        setTransactions(pack.transactions).
                        setIntParam(rand.nextInt(1024) + (tasks.size() * 1024)));
                break;

            case MINING_REQUEST:
//                transactions.add(pack.transactions);
                tasks.add(new MiningTask(pack.sender, pack.transactions));
                break;

            case MINING_REQUEST_SIGN_UPDATED:
                tsk = getTaskFor(x -> x.transactions == pack.transactions);
                if (tsk != null) {
                    tsk.onEventTransactionPackUpdated(pack.transactions);
                }
                break;

            case MINING_REQUEST_STATUS:
                if (pack.status) {
                    tsk = getTaskFor(x -> x.transactions == pack.transactions);
                    if (tsk != null) {
                        tsk.terminate();

                        tasks.remove(tsk);
                    }
                }
//                if (pack.status) {
//                    transactions.remove(pack.transactions);
//                }

//                if (miner.transactions == pack.transactions) {
////                    miner.state = MinerStatus.IDLE;
////                    miner.stopMining();
//                }
                break;

            case BLOCK_DECISION:
                tsk = getTaskFor(x -> x.block == pack.block);
                if (tsk != null) {
                    if (pack.status) {
                        tsk.onEventBlockConfirmed(pack.block);
                        tasks.remove(tsk);
                    } else {
                        tsk.onWrongBlock(pack.block);
                    }
                }

//                if (sentBlocks.contains(pack.block)) {
//                    sentBlocks.remove(pack.block);
//                    if (pack.status) {
//                        transactions.remove(pack.block.getTransactionPack());
//                    } else {
//                    }
//                }
//                else {
////                    some other block change means block will change shortly
//                    miner.state = MinerStatus.WAITING_TASK_PARAMS;
//                }
                break;

//            default:
//                DUMMY:
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
        WAIT_FOR_RESIGN,
        WAIT_FOR_NETWORK_PARAMETERS_UPDATE,
        BLOCK_FOUND,
        BLOCK_FOUND_WAIT_CONFIRMATION,
        IDLE,
        COMPLETED,
        TERMINATED
    }

    class MiningTask implements BlockFound {
        MediaAddress requestor;
        TransactionPack transactions;
        Block block;
        BasicMiner miner;
        volatile MiningTaskState state;
        volatile int lastKnownNetworkComplexity;
        volatile Block lastKnownBlock;

        public MiningTask(MediaAddress requestor, TransactionPack transactions) {
            this.requestor = requestor;
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
            this.lastKnownBlock = BasicMinerClient.this.lastKnownBlock;
            this.lastKnownNetworkComplexity = BasicMinerClient.this.lastKnownNetworkComplexity;
            if (state == MiningTaskState.WAIT_FOR_NETWORK_PARAMETERS_UPDATE) {
                state = MiningTaskState.STANDBY;
            }
//            startMiningIfReady();
        }

        public void onEventTransactionPackUpdated(TransactionPack tp) {
            stopMining();
            if (state == MiningTaskState.WAIT_FOR_RESIGN) {
                state = MiningTaskState.STANDBY;
            }
            this.transactions = tp;
//            startMiningIfReady();
        }

        public void proceed() {
            checkIfReadyToMine();
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

        private void resetTransactionsIds() {
            long lastTrId = lastKnownBlock.getTransactionPack().getLastTransactionId() + 1;
            for (Transaction tr : transactions.transactions) {
                tr.transactionId = lastTrId++;
            }
        }

        public void checkIfReadyToMine() {
            if (state != MiningTaskState.WAIT_FOR_RESIGN & state != MiningTaskState.WAIT_FOR_NETWORK_PARAMETERS_UPDATE) {
                if (lastKnownBlock == null) {
                    requestUpdateOnNetworkParameters();
                    state = MiningTaskState.WAIT_FOR_NETWORK_PARAMETERS_UPDATE;
                } else if (!isValidTransactionPack(transactions)) {
                    resetTransactionsIds();
                    requestTransactionsResign(transactions);
                    state = MiningTaskState.WAIT_FOR_RESIGN;
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
