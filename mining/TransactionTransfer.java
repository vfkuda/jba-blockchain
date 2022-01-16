package blockchain.mining;

import blockchain.UserClient;
import blockchain.blockchain.CryptoKeys;
import blockchain.media.BasicChainClient;

public class TransactionTransfer extends Transaction {
    private final String recepient;
    private final String sender;
    int amount;

    public TransactionTransfer(BasicChainClient client, int amount, String recepient) throws Exception {
        super(TransactionKind.TRANSFER, client.getKeys());
        sender = client.getClientName();
        this.amount = amount;
        this.recepient = recepient;
        this.signTransaction(client.getKeys());
    }

    @Override
    protected String getFields() {
        return String.format("%s sent %d VC to %s", sender, amount, recepient);
    }
}
