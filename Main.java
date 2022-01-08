package blockchain;

import blockchain.media.DataPack;
import blockchain.media.MediaAddress;
import blockchain.media.VirtualMedia;
import blockchain.mining.*;

import java.util.List;
import java.util.stream.IntStream;


public class Main {

    public static void labHowItDesigned_task4() {


        final TransactionManager tm = new TransactionManager();
        tm.connectAndRun();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IntStream.range(1, 3).forEach(i -> {
            (new BasicMinerClient()).connectAndRun();
        });

        final List<String> messages = List.of(
//                "message",
                "another message",
                "yet another message",
                "forth message",
                "final message"
        );

        UserClient userClient = new UserClient();
        userClient.connectAndRun();
        try {
            userClient.persistMessages(new String[]{"multiple transactions", "in a single", "transaction pack"});
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

        while (tm.getBlockChain().getChainSize() < 6) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        VirtualMedia.getMedia().shutdown();

//        assert tm.getBlockChain().isValid();
//        System.out.println(tm.getBlockChain().isValid());
    }

    public static void main(String[] args) {
        Logger.get().setLevel(Logger.ERROR);
//        testLogger();
//        testTransactionSignature();

        labHowItDesigned_task4();

    }

    private static void testTransactionSignature() {
        UserClient uc = new UserClient();
        try {
            TransactionSignedMessage msg = new TransactionSignedMessage("Test", uc.clientKeys);
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

}
