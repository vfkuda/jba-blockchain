package blockchain;

import blockchain.blockchain.CryptoKeys;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.media.MediaClient;
import blockchain.mining.Transaction;
import blockchain.mining.TransactionPack;
import blockchain.mining.TransactionSignedMessage;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class UserClient extends MediaClient {
    final Map<TransactionPack, TransactionPackVote> minersCandidates = new HashMap<>();
    List<TransactionPack> transactionsHistory = new ArrayList<>();
    CryptoKeys clientKeys;

    public UserClient() {
        try {
            clientKeys = new CryptoKeys(512);
            clientKeys.createKeys();
            Logger.info("keys created. client %s", this);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void persistMessages(String[] messages) throws Exception {
        List<Transaction> transactionList = new ArrayList<>();
        TransactionPack tp = new TransactionPack();
        for (String message : messages) {
            tp.addTransasction(new TransactionSignedMessage(message, clientKeys));
        }
        transactionsHistory.add(tp);
        broadcast(DataPack.create(DataPack.DataPackKind.MINING_REQUEST_AUCTION).setTransactions(tp));
    }

    public void persistMessage(String message) throws Exception {
        persistMessages(new String[]{message});
    }

    @Override
    protected void onIdle() {
        synchronized (minersCandidates) {
            for (Map.Entry<TransactionPack, TransactionPackVote> entry : minersCandidates.entrySet()) {
                if (!entry.getValue().requested) {
                    if ((System.currentTimeMillis() > entry.getKey().timeStamp + 1000) || (entry.getValue().bidsMade > 2)) {
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
                        t.signTransaction(clientKeys);
                    }
                    send(pack.sender, new DataPack(DataPack.DataPackKind.MINING_REQUEST_SIGN_UPDATED).
                            setTransactions(pack.transactions));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case MINING_REQUEST_STATUS:
                if (pack.status && transactionsHistory.contains(pack.transactions)) {
                    transactionsHistory.remove(pack.transactions);
//                    System.out.println("!!! transaction confirmed:" + pack.transactions.get(0).toString());
                    shutdown();
                }
                break;
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
