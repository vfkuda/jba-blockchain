package blockchain;

import blockchain.blockchain.Block;
import blockchain.media.BasicChainClient;
import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.media.VirtualMedia;
import blockchain.mining.*;
import blockchain.mining.TransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main {
    private final Random rand = new Random(System.currentTimeMillis());
    private final List<BasicChainClient> miners = new ArrayList<>();
    private final List<UserClient> users = new ArrayList<>();

    public static void main(String[] args) {
        Main m = new Main();
        Logger.get().setLevel(Logger.ERROR);
//        testLogger();
//        testTransactionSignature();

        try {
            m.lab();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testTransactionSignature() {
        UserClient uc = new UserClient();
        try {
            TransactionSignedMessage msg = new TransactionSignedMessage("Test", uc.getKeys());
            System.out.println(msg.verify());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testLogger() {
        Logger.get().addFiler(MediaAddress.class, x -> {
            MediaAddress address = (MediaAddress) x;
            return (address.toString().contains(UserClient.class.getName()));
        });
        Logger.get().addFiler(DataPack.class, x -> {
            DataPack pack = (DataPack) x;
            return (pack.sender.toString().contains(UserClient.class.getName()));
        });
    }

    public void lab() throws Exception {

        final TransactionManager tm = new TransactionManager();
        tm.setMonitor(new TransactionManagerMonitorImpl2());
        tm.connectAndRun();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int minersN = 2;
        for (int i = 0; i < minersN; i++) {
            BasicChainClient miner = new MinerClient();
            miners.add(miner);
            miner.connectAndRun();
        }

//        createBulkMessageTransactions();
        createBulkTransferTransactions(2, 100);

        while (tm.getBlockChain().getChainSize() < 16) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        VirtualMedia.getMedia().shutdown();

//        assert tm.getBlockChain().isValid();
//        System.out.println(tm.getBlockChain().isValid());
    }

    private void createBulkTransferTransactions(int clientsN, int transactionsN) throws Exception {
        //        create userclients
        for (int i = 0; i < clientsN; i++) {
            UserClient userClient = new UserClient();
            users.add(userClient);
            userClient.connectAndRun();
        }

        //        create transfers
        for (int i = 0; i < transactionsN; i++) {
            UserClient sender = getRandomUser();
            UserClient recipient = getRandomUserBut(sender);
            int amount = rand.nextInt(10) + 1;
            try {
                sender.persistTransfer(amount, recipient.getClientName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createBulkMessageTransactions() {
        final List<String> messages = List.of(
//                "message",
//                "another message",
//                "yet another message",
//                "forth message",
                "final message"
        );

        UserClient userClient = new UserClient();
        userClient.connectAndRun();
        try {
            userClient.persistMessage("multiple transactions");
            userClient.persistMessage("in a single");
//            userClient.persistMessage("transaction pack");
        } catch (Exception ex) {
            userClient.shutdown();
            ex.printStackTrace();
        }

        messages.forEach(msg -> {
            UserClient userClient1 = new UserClient();
            userClient1.connectAndRun();
            try {
                userClient1.persistMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

    protected UserClient getRandomUser() {
        return users.get(rand.nextInt(users.size()));
    }

    protected UserClient getRandomUserBut(UserClient exclude) {
        UserClient uc;
        do {
            uc = getRandomUser();
        } while (uc == exclude);
        return uc;
    }

    class TransactionManagerMonitorImpl2 extends TransactionManager.TransactionManagerMonitorImpl {
        @Override
        public void log(MediaAddress minderAddress, Block block, long completionTimeInMilliSeconds, int complexityAdjustment, int complexity) {
            if (block.getBlockId() <= 15) {
                super.log(minderAddress, block, completionTimeInMilliSeconds, complexityAdjustment, complexity);
            }
        }
    }

}
