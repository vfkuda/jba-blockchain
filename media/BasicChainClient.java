package blockchain.media;

import blockchain.Logger;
import blockchain.blockchain.CryptoKeys;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;

abstract public class BasicChainClient extends MediaClient {

    protected CryptoKeys clientKeys;

    public BasicChainClient() {
        try {
            clientKeys = new CryptoKeys(512);
            clientKeys.createKeys();
            Logger.info("keys created. client %s", this.getClientName());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return clientKeys.getPublicKey();
    }

    public CryptoKeys getKeys() {
        return clientKeys;
    }

    public String getClientName() {
        return address.toString();
    }
}
