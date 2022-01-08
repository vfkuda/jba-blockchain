package blockchain.mining;

import blockchain.blockchain.CryptoKeys;

public class TransactionNop extends Transaction {

    public TransactionNop(TransactionKind kind, CryptoKeys keys) throws Exception {
        super(kind, keys);
    }

    @Override
    protected String getFields() {
        return "";
    }

}
