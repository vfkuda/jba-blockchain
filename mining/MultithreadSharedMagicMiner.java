package blockchain.mining;

import static java.lang.Thread.sleep;

public class MultithreadSharedMagicMiner { //implements Runnable, ObsoleteMiner {
//    private static final int MINING_POOL_SIZE = 1;
//
//    protected boolean terminationFlag;
//
//    Map<MiningRequest, SharedData> workList = new HashMap<>();
//    Queue<MTCommand> commands = new LinkedList<>();
//
//    //    protected boolean blockFound;
////    volatile long magic;
//    volatile int generation = 0;
//    private MiningNetwork network;
////    boolean isCompleted;
////    MiningRequest request;
////    MiningRequestor requestor;
//
////    public MultithreadSharedMagicMiner() {
////        isCompleted = false;
////        magic = 0;
////    }
//
//    public static ObsoleteMiner launch() {
//        MultithreadSharedMagicMiner miner = new MultithreadSharedMagicMiner();
//        (new Thread(miner)).start();
//        return miner;
//    }
//
////    public synchronized void incMagic() {
////        magic++;
////    }
////
////    public MultithreadMiner(MiningRequest request, MiningRequestor requestor) {
////        this();
////        this.requestor = requestor;
////        this.request = request;
////    }
//
//    @Override
//    public boolean isBusy() {
//        return !workList.isEmpty();
//    }
//
//    @Override
//    public void setRequest(MiningRequest request, MiningRequestor requestor) {
////        this.request = request;
////        this.requestor = requestor;
////        this.blockFound = false;
////
////        for (int i = 0; i < 3; i++) {
////            MinerWorker miner = new MinerWorker(this.generation + 1);
////            (new Thread(miner)).start();
////        }
////
////        while (!blockFound) {
////            try {
////                Thread.sleep(1000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////        }
////        request.getBlock().setMagic(magic);
////        request.setCompletedBy(Thread.currentThread().getName());
////        requestor.acceptTaskCompletion(request, Thread.currentThread().getName());
//
//    }
//
////    protected synchronized void terminateSwarm(long magic) {
////        if (!blockFound) {
////            blockFound = true;
////            MultithreadSharedMagicMiner.this.generation++;
////        }
////    }
//
//    @Override
//    public void cancelRequest(MiningRequest request, MiningRequestor requestor) {
//        generation++;
//        SharedData share = workList.get(request);
//        if (share != null) {
//            share.terminationFlag = true;
//            workList.remove(share);
//        }
//    }
//
//    @Override
//    public void dettach() {
//        terminationFlag = true;
//        generation++;
//    }
//
//    @Override
//    public void assign(MiningNetwork network, MiningRequest request) {
//        this.network = network;
//        queueCommand(new CommandAssign(request));
////
////        System.out.println("assign" + request.getBlock().getBlockId() + " " + request.getLeadingZeros());
////        this.network = network;
////        generation++;
////        SharedData share = new SharedData(request, generation);
////
////        for (int i = 0; i < MINING_POOL_SIZE; i++) {
////            share.workers.add(new MinerWorker(share));
////            System.out.println("new worker" + share.toString() + "for get" + generation);
////        }
////
////        workList.put(request, share);
////
////        share.workers.forEach(e -> (new Thread(e)).start());
//    }
//
//    @Override
//    public synchronized void candidateAccepted(MiningRequest request, boolean status) {
//        CommandCandidateAccepted cmd = new CommandCandidateAccepted(request, status);
//        queueCommand(cmd);
////        if (status) {
////            SharedData share = workList.get(request);
////            share.terminationFlag = true;
////            workList.remove(request);
////            System.out.println("accepted" + request.getBlock().getBlockId());
////
////        } else {
////            System.out.println("un accepted" + request.getBlock().getBlockId());
////
////        }
//
//    }
//
////    public synchronized void submitCandidate(MiningRequest request, long magic) {
////        SharedData share = workList.get(request);
////        if (!share.terminationFlag && !share.awaitsValidation) {
////            System.out.println("submit" + request.getBlock().getBlockId() + " " + request.getBlock().getBlockHash() + " " + request.getLeadingZeros());
////            request.setCompletedBy(Thread.currentThread().getName());
////            network.addCandidate(request, magic);
////            share.awaitsValidation = true;
////        }
////    }
//
//    @Override
//    public void run() {
//        while (!terminationFlag) {
//            if (!commands.isEmpty()) {
//                commands.poll().execute();
//            }
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    private synchronized void queueCommand(MTCommand command) {
//        commands.offer(command);
//    }
//
//    interface MTCommand {
//        void execute();
//    }
//
//    class MinerWorker implements Runnable {
//        SharedData share;
//
//        public MinerWorker(SharedData share) {
//            this.share = share;
//        }
//
//        @Override
//        public void run() {
//
//            MultithreadSharedMagicMiner outer = MultithreadSharedMagicMiner.this;
//            while ((!terminationFlag) && (!outer.terminationFlag) && (outer.generation == share.geneation)) {
//                if (!share.awaitsValidation) {
//                    Block b = share.request.getBlock();
//                    long magic = share.magic;
//                    while (!BlockchainUtils.isHashComplexEnough(b.getBlockHash(magic), share.request.getLeadingZeros())) {
//                        share.incMagic();
//                        magic = share.magic;
//                    }
////                    System.out.println("magic" + magic);
//                    queueCommand(new CommandSubmitCandidate(share.request, magic));
//                    try {
//                        sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//    }
//
//    class SharedData {
//        MiningRequest request;
//        List<MinerWorker> workers = new ArrayList<>();
//
//        volatile int geneation;
//        volatile int magic = 0;
//        volatile boolean awaitsValidation = false;
//        boolean terminationFlag = false;
//
//        public SharedData(MiningRequest request, int geneation) {
//            this.request = request;
//            this.geneation = geneation;
//        }
//
//        synchronized void incMagic() {
//            this.magic++;
//        }
//    }
//
//    class CommandSubmitCandidate implements MTCommand {
//        MiningRequest request;
//        long magic;
//
//        public CommandSubmitCandidate(MiningRequest request, long magic) {
//            this.request = request;
//            this.magic = magic;
//        }
//
//        @Override
//        public void execute() {
//            SharedData share = workList.get(request);
//            if (!share.terminationFlag && !share.awaitsValidation) {
////                System.out.println("submit" + request.getBlock().getBlockId() + " " + request.getBlock().getBlockHash() + " " + request.getLeadingZeros());
//                request.setCompletedBy(Thread.currentThread().getName());
//                network.addCandidate(request, magic);
//                share.awaitsValidation = true;
//            }
//        }
//    }
//
//    class CommandCandidateAccepted implements MTCommand {
//        MiningRequest request;
//        boolean status;
//
//        public CommandCandidateAccepted(MiningRequest request, boolean status) {
//            this.request = request;
//            this.status = status;
//        }
//
//        @Override
//        public void execute() {
//            if (status) {
//                SharedData share = workList.get(request);
//                share.terminationFlag = true;
//                workList.remove(request);
////                System.out.println("accepted" + request.getBlock().getBlockId());
//
//            }
////            else {
//////                System.out.println("un accepted" + request.getBlock().getBlockId());
////
////            }
//
//        }
//
//    }
//
//    class CommandAssign implements MTCommand {
//        //        MiningNetwork network;
//        MiningRequest request;
//
//        public CommandAssign(MiningRequest request) {
//            this.request = request;
//        }
//
//        @Override
//        public void execute() {
////            System.out.println("assign" + request.getBlock().getBlockId() + " " + request.getLeadingZeros());
////                this.network = network;
//            generation++;
//            SharedData share = new SharedData(request, generation);
//
//            for (int i = 0; i < MINING_POOL_SIZE; i++) {
//                share.workers.add(new MinerWorker(share));
////                System.out.println("new worker" + share.toString() + "for get" + generation);
//            }
//
//            workList.put(request, share);
//
//            share.workers.forEach(e -> (new Thread(e)).start());
//
//        }
//    }

}
