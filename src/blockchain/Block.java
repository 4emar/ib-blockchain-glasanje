package blockchain;

import java.io.Serializable;

public class Block implements Serializable
{
    //inicijalizacija na klasata za glasanje, so informaciite za glasacot
    public class Vote implements Serializable
    {
        private String glasacID;
        private String glasacIme;
        private String glasacPartija;

        public Vote(String glasacID, String glasacIme, String glasacPartija)
        {
            this.glasacIme=glasacIme;
            this.glasacID=glasacID;
            this.glasacPartija=glasacPartija;
        }

        public String getGlasacID() {
            return glasacID;
        }

        public void setGlasacID(String glasacID) {
            this.glasacID = glasacID;
        }

        public String getGlasacIme() {
            return glasacIme;
        }

        public void setGlasacIme(String glasacIme) {
            this.glasacIme = glasacIme;
        }

        public String getGlasacPartija() {
            return glasacPartija;
        }

        public void setGlasacPartija(String glasacPartija) {
            this.glasacPartija = glasacPartija;
        }
    }

    private Vote voteObj;

    private int previousHash;
    private int blockHash;

    public Block(int previousHash, String glasacID, String glasacIme, String glasacPartija)
    {
        this.previousHash=previousHash;
        voteObj=new Vote(glasacID, glasacIme, glasacPartija);

        Object[] contents={voteObj.hashCode(), previousHash};
        this.blockHash=contents.hashCode();
    }

    public Vote getVoteObj() {
        return voteObj;
    }

    public void setVoteObj(Vote voteObj) {
        this.voteObj = voteObj;
    }

    public int getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(int previousHash) {
        this.previousHash = previousHash;
    }

    public int getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(int blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public String toString() {
        return "Glasac ID:" + this.voteObj.glasacID + "\nGlasac Ime: " + this.voteObj.glasacIme + "\nGlasa za partija: " + this.voteObj.glasacPartija;
    }
}