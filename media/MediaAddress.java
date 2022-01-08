package blockchain.media;

public class MediaAddress {
    private String id;

    public MediaAddress(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }


}
