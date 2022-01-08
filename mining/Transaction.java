package blockchain.mining;

import blockchain.blockchain.CryptoHelper;
import blockchain.blockchain.CryptoKeys;

import java.security.PublicKey;

abstract public class Transaction {
    public TransactionKind kind = TransactionKind.DUMMY;
    public long transactionId;
    //    String id;
    private long timeStamp;
    private byte[] signature;
    private PublicKey publicKey;

    public Transaction(TransactionKind kind, CryptoKeys keys) throws Exception {
        this.timeStamp = System.currentTimeMillis();
        this.kind = kind;
        this.publicKey = keys.getPublicKey();
        signTransaction(keys);
    }

    public void touch() {
        this.timeStamp = System.currentTimeMillis();
    }

    public void signTransaction(CryptoKeys keys) throws Exception {
        this.signature = CryptoHelper.sign(getData(), keys.getPrivateKey());
    }

    public boolean verify() throws Exception {
        return CryptoHelper.verifySignature(getData(), signature, publicKey);
    }

    abstract protected String getFields();

    public String getData() {
        return String.format("[%d]:%s: %s",
                transactionId, kind.name(), getFields());
    }

    @Override
    public String toString() {
        return String.format("[%d]:%s: %s {%s}",
                transactionId, kind.name(), getFields(), CryptoHelper.exncodeHex(signature));

    }

    public enum TransactionKind {
        DUMMY,
        MESSAGE,
        CURRENCY_TRANSFER
    }
}
