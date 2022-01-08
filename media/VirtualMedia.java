package blockchain.media;

import blockchain.Logger;

import java.util.*;

public class VirtualMedia {
    private static VirtualMedia instance;
    private Map<MediaAddress, Queue<DataPack>> clientOutboundQueues = new HashMap<>();

    public static VirtualMedia getMedia() {
        if (null == instance) {
            instance = new VirtualMedia();
        }
        return instance;
    }

    private Set<MediaAddress> getKnownClients() {
        return clientOutboundQueues.keySet();
    }

    private synchronized Queue<DataPack> getQueueForClient(MediaAddress client) {
        if (!clientOutboundQueues.containsKey(client)) {
            clientOutboundQueues.put(client, new LinkedList<>());
        }
        return clientOutboundQueues.get(client);
    }

    public synchronized void transmitTo(MediaAddress client, DataPack pack) {
        Logger.info("send from: %s to: %s pack: %s [%s]", pack.sender, client, pack, Thread.currentThread().getName());
//        System.out.printf("send from: %s to: %s pack: %s [%s]\n", pack.sender, client, pack.toString(), Thread.currentThread().getName());

        getQueueForClient(client).offer(pack);
    }

    public synchronized void transmitBroadcast(DataPack pack) {
        Logger.info("broadcast from: %s pack: %s", pack.sender, pack);
//        System.out.printf("broadcast from: %s pack: %s\n", pack.sender, pack.toString());

        getKnownClients().forEach(client -> {
            if (client != pack.sender) transmitTo(client, pack);
        });
    }

    private Map<MediaAddress, Queue<DataPack>> getClientOutboundQueues() {
        return clientOutboundQueues;
    }

    protected String queueStatus(MediaAddress addr) {
        String retVal = addr.toString() + "[";
        for (DataPack pack : clientOutboundQueues.get(addr)) {
            retVal = retVal + pack.kind + ":";
        }
        return retVal + "]";

    }

    protected String queueStatus() {
        String retval = "";
        for (MediaAddress addr : clientOutboundQueues.keySet()) {
            retval = retval + queueStatus(addr) + " | ";
        }
        return retval;
    }

    synchronized DataPack retrieveFor(MediaAddress client) {
        DataPack pack = getQueueForClient(client).poll();
        if (pack != null) {
            Logger.info("retrieve for: %s from: %s pack: %s [%s]", client, pack.sender, pack, Thread.currentThread().getName());
            Logger.debug(queueStatus());

//            System.out.printf("retrieve for: %s from: %s pack: %s [%s]\n", client, pack.sender, pack.toString(), Thread.currentThread().getName());
//            System.out.println(queueStatus());
//            System.out.print(" ");
        }
        return pack; //null != pack ? pack : DataPack.DUMMY();
    }

    public synchronized void subscribe(MediaAddress address) {
        getQueueForClient(address);
    }

    public synchronized void unsubscribe(MediaAddress address) {
        clientOutboundQueues.remove(address);
    }

    public void shutdown() {
        transmitBroadcast(new DataPack(DataPack.DataPackKind.MEDIA_OFF));

    }
}
