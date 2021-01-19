public class Block {

    String redundancy;

    String preSha;

    String curSha;

    String allSha;

    String data;

    public Block(String allSha, String redundancy, String preSha, String curSha,  String data) {
        this.redundancy = redundancy;
        this.preSha = preSha;
        this.curSha = curSha;
        this.data = data;
        this.allSha = allSha;
    }

    public Block(){};
}
