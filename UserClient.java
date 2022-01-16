package blockchain;

import blockchain.blockchain.CryptoKeys;
import blockchain.media.BasicChainClient;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.media.MediaClient;
import blockchain.mining.*;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class UserClient extends BasicChainClient {
    List<TransactionPack> transactionsHistory = new ArrayList<>();

    public void persistMessage(String message) throws Exception {
        addTransaction(new TransactionSignedMessage(message, getKeys()));
    }

    public void persistTransfer(int amount, String recepient) throws Exception {
        addTransaction(new TransactionTransfer(this, amount, recepient));
    }

    public void addTransaction(Transaction tr) {
        TransactionPack tp = new TransactionPack();
        tp.addTransasction(tr);
        transactionsHistory.add(tp);
        broadcast(DataPack.create(DataPack.DataPackKind.MINING_REQUEST).setTransactions(tp));
    }

    @Override
    protected void onIdle() {
        if (transactionsHistory.isEmpty()) {
        }
    }

    @Override
    protected void onPackReceived(DataPack pack) {
        switch (pack.kind) {
            case BLOCK_DECISION:
                if (pack.status) {
                    for (Transaction tr : pack.transactions.getTransactions()) {
                        transactionsHistory.remove(tr);
                    }
                }
        }
    }

    @Override
    protected void onShutdown() {
        Logger.info("client %s on shutdown", this);
    }

}
