package blockchain.mining;

import blockchain.Logger;
import blockchain.blockchain.Block;
import blockchain.blockchain.BlockChain;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.media.MediaClient;

public class TransactionManager extends MediaClient {
    //        in milliseconds
    private static final long MINING_TIME_THRESHOLD_MIN = 1_000;
    private static final long MINING_TIME_THRESHOLD_MAX = 3_000;
    private TransactionManagetMonitor monitor = new TransactionManagerMonitorImpl();
    private NetworkCompexity complexity;
    private BlockChain blockChain;

    public TransactionManager() {
        super();
        blockChain = new BlockChain();
        complexity = new NetworkCompexity(0);
//        transactions = new LinkedList<>();
    }
//    private Queue<Transaction> transactions;

    public BlockChain getBlockChain() {
        return blockChain;
    }

    public void setMonitor(TransactionManagetMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    protected void onIdle() {
    }

    @Override
    protected void onShutdown() {
    }

    @Override
    protected void onPackReceived(DataPack pack) {
        switch (pack.kind) {
            case MINING_NETWORK_UPDATE_REQUEST:
                send(pack.sender, new DataPack(DataPack.DataPackKind.MINING_NETWORK_UPDATE).
                        setBlock(blockChain.getLastBlock()).
                        setIntParam(complexity.complexity)
                );
                break;
            case BLOCK_SUGGESTION:
                Block prevBlock = blockChain.getLastBlock();
                if (blockChain.registerBlock(pack.block)) {
                    send(pack.sender, (new DataPack(DataPack.DataPackKind.BLOCK_DECISION)).
                            setBlock(pack.block).
                            setStatus(true));

//                    long completionTime = System.currentTimeMillis() - prevBlock
//                            .getTimeStamp();
                    long completionTime = pack.block.getTimeStamp() - prevBlock
                            .getTimeStamp();
                    int adjustmentFactor = complexity.doesReqireAdjustment(completionTime);
                    complexity.adjust(adjustmentFactor);

//                    log(pack.sender, pack.block, completionTime, adjustmentFactor);
                    if (monitor != null) {
                        monitor.log(pack.sender, pack.block, completionTime, adjustmentFactor, complexity.complexity);
                    }

                    broadcast(new DataPack(DataPack.DataPackKind.MINING_NETWORK_UPDATE).
                            setBlock(blockChain.getLastBlock()).
                            setIntParam(complexity.complexity)
                    );
//                    broadcast(new DataPack(DataPack.DataPackKind.MINING_REQUEST_STATUS).
//                            setTransactions(pack.block.getTransactionPack()).
//                            setStatus(true));

                } else {
                    int err = blockChain.checkBlockValidity(pack.block);
                    Logger.info("block %s not accepted %d", pack.block, err);

                    send(pack.sender, new DataPack(DataPack.DataPackKind.BLOCK_DECISION).
                            setBlock(pack.block).
                            setStatus(false).
                            setIntParam(err));
                }
                break;

//            default:
//                DUMMY:
        }

    }


//    public boolean hasActiveTransactions() {
//        return !transactions.isEmpty();
//    }

    interface TransactionManagetMonitor {
        public void log(MediaAddress minderAddress, Block block, long completionTimeInMilliSeconds, int complexityAdjustment, int complexity);
    }

    public static class TransactionManagerMonitorImpl implements TransactionManagetMonitor {
        public void log(MediaAddress minderAddress, Block block, long completionTimeInMilliSeconds, int complexityAdjustment, int complexity) {
            StringBuilder sb = new StringBuilder();
            String ln = System.getProperty("line.separator");
            sb.append("Block:");
            sb.append(ln);
            sb.append(String.format("Created by miner # %s:", minderAddress.toString()));
            sb.append(ln);
            sb.append(String.format("%s gets 100 VC ", minderAddress.toString()));
            sb.append(ln);
            sb.append(String.format("Id: %d", block.getBlockId()));
            sb.append(ln);
            sb.append(String.format("Timestamp: %d", block.getTimeStamp()));
            sb.append(ln);
            sb.append(String.format("Magic number: %d", block.getMagic()));
            sb.append(ln);
            sb.append("Hash of the previous block:");
            sb.append(ln);
            sb.append(block.getPrevHash());
            sb.append(ln);
            sb.append("Hash of the block:");
            sb.append(ln);
            sb.append(block.getBlockHash());
            sb.append(ln);
            sb.append("Block data:");
            sb.append(ln);
            sb.append(block.getTransactionPack().toString());
            sb.append(ln);
            sb.append(String.format("Block was generating for %d seconds", Math.round(completionTimeInMilliSeconds / 1000.0)));
            sb.append(ln);
            switch (complexityAdjustment) {
                case -1:
                    sb.append("N was decreased by 1");
                    break;
                case 1:
                    sb.append(String.format("N was increased to %d", complexity));
                    break;
                default:
                    sb.append("N stays the same");
            }
            sb.append(ln);

            System.out.println(sb.toString());
        }
    }

    class NetworkCompexity {
        private int complexity;

        public NetworkCompexity(int complexity) {
            this.complexity = complexity;
        }

        void adjust(int factor) {
            complexity = complexity + factor;
        }

        int doesReqireAdjustment(long completionTimeMIllies) {
            if (completionTimeMIllies < MINING_TIME_THRESHOLD_MIN) {
                return 1;
            }
            if (completionTimeMIllies > MINING_TIME_THRESHOLD_MAX) {
                return -1;
            }
            return 0;
        }
    }

}
