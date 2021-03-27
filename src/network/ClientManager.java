package network;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import blockchain.Block;

import static main.Main.decrypt;
import static java.nio.file.attribute.PosixFilePermission.*;


public class ClientManager extends NetworkManager {

    /* soket komunicira so serverot. */
    private Socket _socket = null;
    private Block genesisBlock;
    private ArrayList<SealedObject> blockList;
    private ArrayList<String> partii;
    private HashSet<String> hashVotes;
    private int prevHash=0;

    private int klientID;

    //Klasa odgovorna za komunikacijata od strana na klient.
    //Vo nov thread, ke ima loop kade sto ke prima poraki od serverot i ke gi prati na main threadot za handlanje.

    public ClientManager(String addr, int porta) {
        try {
            _socket = new Socket(addr, porta);
            System.out.println("Uspešno konektiran do serverot: " + addr + ":" + porta);
            genesisBlock=new Block(0, "", "", "");
            hashVotes=new HashSet<>();
            partii = new ArrayList<>();
            partii.add("VMRO-DPMNE");
            partii.add("SDSM");
            partii.add("LEVICA");

            blockList=new ArrayList<>();
            blockList.add(encrypt(genesisBlock));
        } catch (IOException e) {
            System.out.println("Ne može da se konektiram do serverot " + addr + ":" + porta);
            e.setStackTrace(e.getStackTrace());
            System.exit(0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void zapocniClient() {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Dobre dojdovte na mestoto za glasanje! ");
        String choice ="y";
        do{
            Block blockObj=null;

            String glasacID= null;
            String glasacIme =null;
            String glasacPartija=null;

            try {
                System.out.print("Vnesi ID na glasač: ");
                glasacID = br.readLine();
                System.out.print("Vnesi ime na glasač: ");
                glasacIme = br.readLine();

                System.out.println("Glasaj za partijata:");
                int glasIzbor;

                do {
                    for (int i=0 ;i<partii.size() ;i++) {
                        System.out.println((i+1)+". "+ partii.get(i));
                    }

                    System.out.println("Vnesi go tvojot glas: ");
                    glasacPartija=br.readLine();
                    glasIzbor=Integer.parseInt(glasacPartija);
//	                System.out.println("vote choice : "+ glasIzbor);
                    if(glasIzbor>partii.size()||glasIzbor<1)
                        System.out.println("Izberi validen broj.");
                    else
                        break;
                }while(true);

                glasacPartija = partii.get(glasIzbor-1);
                blockObj=new Block(prevHash, glasacID, glasacIme, glasacPartija);

                if(checkValidity(blockObj)) {
                    hashVotes.add(glasacID);
                    sendMsg(new MessageStruct( 1,encrypt(blockObj) ));

                    prevHash=blockObj.getBlockHash();
                    blockList.add(encrypt(blockObj));
                    //add
                }
                else
                {
                    System.out.println("Nevaliden glas.");
                }
                System.out.println("Glasaj povtorno (y/n) ? ");
                choice=br.readLine();

            } catch (IOException e) {
                System.out.println("ERROR: linijata ne se pročita uspešno!");
                return;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }while(choice.equals("y")||choice.equals("Y"));
        close();
    }

    public SealedObject encrypt(Block b) throws Exception
    {
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");

        // Kreiraj cipher
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        cipher.init( Cipher.ENCRYPT_MODE, sks );

        return new SealedObject( b, cipher);
    }

    private boolean checkValidity(Block blockObj) {
        // TODO Auto-generated method stub
        if( hashVotes.contains((String)blockObj.getVoteObj().getGlasacID() ))
            return false;
        else
            return true;
    }

    public void sendMsg(MessageStruct msg) throws IOException {
        sendMsg(_socket, msg);
    }

    public void close() {

        String userHomePath = System.getProperty("user.home");
        String fileName;
        fileName=userHomePath+"/Desktop/blockchain_data";
        File f=new File(fileName);

        try
        {
            if(!f.exists())
                f.createNewFile();
            else {
                f.delete();
                f.createNewFile();
            }

            Files.setPosixFilePermissions(f.toPath(),
                    EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE));
            System.out.println(fileName);

            ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(fileName,true));
            o.writeObject(blockList);

            o.close();

            _socket.close();

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.exit(0);
    }

    @Override
    public void msgHandler(MessageStruct msg, Socket src) {
        switch (msg._code) {
            case 0:

                try {

                    blockList.add((SealedObject)msg._content);

                    Block decryptedBlock=(Block) decrypt((SealedObject)msg._content);
                    hashVotes.add(decryptedBlock.getVoteObj().getGlasacID());

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case 1:

                break;
            case 2:
                klientID=(int)(msg._content);
            default:
                break;
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                receiveMsg(_socket);

            } catch (ClassNotFoundException | IOException e) {
                if(_socket.isClosed())
                {
                    System.out.println("Blagodarime, prijatno!");
                    System.exit(0);
                }

                System.out.println("Konekcijata do serverot se prekina. Restartiraj.");
                close(_socket);
                System.exit(-1);
            }
        }
    }
}