package blockchain.mining;

import blockchain.blockchain.CryptoHelper;
import blockchain.blockchain.CryptoKeys;

import java.security.PrivateKey;
import java.security.PublicKey;

public class TransactionSignedMessage extends Transaction {

    private byte[] signature;
    private PublicKey publicKey;
    private String message;

    public TransactionSignedMessage(String message, CryptoKeys keys) throws Exception {
        super(TransactionKind.MESSAGE, keys);
        this.message = message;
        this.signTransaction(keys);
    }

    @Override
    protected String getFields() {
        return message;
    }
}
