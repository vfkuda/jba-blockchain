package blockchain;

import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.mining.Transaction;
import blockchain.mining.TransactionPack;

import java.util.HashMap;
import java.util.Map;

// different approach for transaction requests. transaction is sent to those who voited most.
public class UserVoitingClient extends UserClient {
    final Map<TransactionPack, TransactionPackVote> minersCandidates = new HashMap<>();

    public void addTransaction(Transaction tr) {
        TransactionPack tp = new TransactionPack();
        tp.addTransasction(tr);
        transactionsHistory.add(tp);
        broadcast(DataPack.create(DataPack.DataPackKind.MINING_REQUEST_AUCTION).setTransactions(tp));
    }

    @Override
    protected void onIdle() {
        super.onIdle();
        trackAuctionWinners();
    }

    private void trackAuctionWinners() {
        synchronized (minersCandidates) {
            for (Map.Entry<TransactionPack, TransactionPackVote> entry : minersCandidates.entrySet()) {
                if (!entry.getValue().requested) {
                    if ((System.currentTimeMillis() > entry.getKey().getFirstTransactionTimeStampInThePack() + 1000) || (entry.getValue().bidsMade > 2)) {
                        entry.getValue().makeWinner();
                        send(entry.getValue().miner, new DataPack(DataPack.DataPackKind.MINING_REQUEST).
                                setTransactions(entry.getKey()));
                        entry.getValue().requested = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onPackReceived(DataPack pack) {
        super.onPackReceived(pack);
        switch (pack.kind) {
            case MINING_REQUEST_AUCTION_BID:
                synchronized (minersCandidates) {
                    TransactionPackVote currentVote = minersCandidates.getOrDefault(pack.transactions, new TransactionPackVote());
                    if (pack.intParam < currentVote.bid) {
                        currentVote.miner = pack.sender;
                        currentVote.bid = pack.intParam;
                    }
                    currentVote.bidsMade++;
                    minersCandidates.put(pack.transactions, currentVote);
                }
                break;
            case MINING_REQUEST_NEED_SIGNUP:
                try {
                    for (Transaction t : pack.transactions.getTransactions()) {
                        t.touch();
                        t.signTransaction(clientKeys);
                    }
                    send(pack.sender, new DataPack(DataPack.DataPackKind.MINING_REQUEST_SIGN_UPDATED).
                            setTransactions(pack.transactions));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

//            case MINING_REQUEST_STATUS:
//                if (pack.status && transactionsHistory.contains(pack.transactions)) {
//                    transactionsHistory.remove(pack.transactions);
////                    System.out.println("!!! transaction confirmed:" + pack.transactions.get(0).toString());
//                    if (transactionsHistory.isEmpty()) {
//                        shutdown();
//                    }
//                }
//                break;
        }

    }

    @Override
    protected void onShutdown() {
        Logger.info("client %s on shutdown", this);
    }

    class TransactionPackVote {
        public int bidsMade = 0;
        public int bid;
        boolean requested = false;
        MediaAddress miner;

        //        sure looser by default
        public TransactionPackVote() {
            this.miner = null;
            makeLooser();
        }

        public TransactionPackVote(MediaAddress miner, int bid) {
            this.miner = miner;
            this.bid = bid;

        }

        void makeLooser() {
            bid = Integer.MAX_VALUE;
        }

        void makeWinner() {
            bid = -1;
        }
//        TODO: create equal and move here cmp tasks

    }


}
