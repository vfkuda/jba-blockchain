package blockchain.mining;

import blockchain.blockchain.Block;

public interface BlockFound {
    public void onBlockFound(BasicMiner miner, Block block);
}
