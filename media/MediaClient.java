package blockchain.media;

abstract public class MediaClient implements Runnable {
    public MediaAddress address;
    private boolean terminationFlag = false;

    protected MediaClient() {
        super();
        this.address = new MediaAddress(getClass().getSimpleName() + hashCode());
    }

    protected void send(MediaAddress addr, DataPack data) {
        VirtualMedia.getMedia().transmitTo(addr, data.setSender(address));
    }

    protected void broadcast(DataPack pack) {
        VirtualMedia.getMedia().transmitBroadcast(pack.setSender(address));
    }

    public void shutdown() {
        onShutdown();
        terminationFlag = true;
    }

    public void run() {
//        System.out.printf("client: %s runs in thread: %s\n", address.toString(), Thread.currentThread().getName());

        while (!terminationFlag) {
            DataPack pack = VirtualMedia.getMedia().retrieveFor(address);
            if (pack != null) {
                switch (pack.kind) {
                    case MEDIA_OFF:
                        shutdown();
                        break;
                    default:
                        onPackReceived(pack);
                }
            }
            onIdle();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        VirtualMedia.getMedia().unsubscribe(address);

//        System.out.printf("disconnected. client: %s runs in thread: %s\n", address.toString(), Thread.currentThread().getName());
    }

    public void connectAndRun() {
        VirtualMedia.getMedia().subscribe(address);
        new Thread(
                this,
                this.address.toString()
        ).start();
    }

    public MediaAddress getAddress() {
        return address;
    }

    abstract protected void onPackReceived(DataPack pack);

    protected abstract void onIdle();

    abstract protected void onShutdown();
}
