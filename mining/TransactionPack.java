package blockchain.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionPack {
    public long timeStamp;
    List<Transaction> transactions = new ArrayList<>();

    public TransactionPack() {
        this.timeStamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        List<String> strings = transactions.stream().map(Transaction::toString).collect(Collectors.toList());
        return String.join("\n", strings);
    }

    public long getLastTransactionId() {
        if (transactions.size() == 0) {
            return 0;
        }
        return transactions.get(transactions.size() - 1).transactionId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransasction(Transaction transaction) {
        transactions.add(transaction);
    }

    public long getFirstTransactionId() {
        if (transactions.size() == 0) {
            return 0;
        }
        return transactions.get(0).transactionId;
    }
}
