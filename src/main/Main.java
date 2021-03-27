package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import blockchain.Block;
import network.ClientManager;
import network.ServerManager;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import static java.lang.System.exit;
import static java.lang.System.lineSeparator;

public class Main {

    private static final String DEFAULT_SERVER_ADDR = "localhost";
    private static final int DEFAULT_PORT = 6777;

    public static void main(String[] args) {
//        int clientId=0;
        System.out.println(" --------GLAVNO MENI----------- \n");
        System.out.println("1. Glasaj");
        System.out.println("2. Pregledaj gi glasovite");
        System.out.println("3. Izbroj gi glasovite");
        System.out.println("0. Izlezi\n");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Vnesi go tvojot izbor: ");
        int izbor = scanner.nextInt();

        if(izbor == 1)
        {
            System.out.println("\n ----- Glasanje... ----- \n");
            System.out.println("Izberi uloga koja sto sakas da bides: server ili klient");
            System.out.println("Za SERVER: server PORT - Default porta e 6777");
            System.out.println("Za KLIENT: client SERVER_ADDRESS PORT - Default server address i port kombinacija se  \"localhost:6777\"");
            System.out.println("Prvo konektiraj se kako server, a potoa kako klient.");
            System.out.println("> ---------- ");

            Scanner in = new Scanner(System.in);
            String line = in.nextLine();
            String[] cmd = line.split("\\s+");

            if (cmd[0].contains("s"))
            {   // selektirano e da bide server

                /* raboti kako server */
                int porta = DEFAULT_PORT;
                if (cmd.length > 1) {
                    try {
                        porta = Integer.parseInt(cmd[1]);
                    } catch(NumberFormatException e) {
                        System.out.println("Error: portata mora da bide broj!");
                        in.close();
                        return;
                    }
                }

                ServerManager _svrMgr =new ServerManager(porta);
                new Thread(_svrMgr).start();


            }
            else if (cmd[0].contains("c"))
            {
                //klient

                /* raboti kako klient */
                String svrAddr = DEFAULT_SERVER_ADDR;
                int porta = DEFAULT_PORT;
                if (cmd.length > 2) {
                    try {
                        svrAddr = cmd[1];
                        porta = Integer.parseInt(cmd[2]);
                    } catch(NumberFormatException e) {
                        System.out.println("Error: Portata mora da bide broj.");
                        in.close();
                        return;
                    }
                }

                ClientManager _cltMgr = new ClientManager(svrAddr, porta);

                /* nova niska za da prima poraka */
                new Thread(_cltMgr).start();

                _cltMgr.zapocniClient();
            }
            else {
                showHelp();
                in.close();
                return;
            }
            in.close();
        }

        // vidi gi glasovite
        else if(izbor == 2)
        {
            System.out.println("\n ----- Prikazuvanje na glasovite ----- \n");

            String direc = System.getProperty("user.home");
            String fileName;
            fileName=direc+"/Desktop/blockchain_data";
            File f=new File(fileName);

            try
            {
                if(!f.exists())
                    System.out.println("Blockchain fajlot ne e najden.");

                ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));

                ArrayList<SealedObject> arr=(ArrayList<SealedObject>) in.readObject();
                for(int i=1;i<arr.size();i++) {
                    System.out.println(decrypt(arr.get(i)));
                }
                in.close();

                System.out.println("-------------------------\n");

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }

        // izbroj gi glasovite
        else if(izbor == 3)
        {
            String direc = System.getProperty("user.home");
            String fileName;
            fileName=direc+"/Desktop/blockchain_data";
            File f=new File(fileName);

            try
            {
                if(!f.exists())
                    System.out.println("Ve molime prvo glasajte.");

                else
                {
                    System.out.println();
                    System.out.println("-------------------------");
                    System.out.println("Broj Glasovi: ");
                    ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));

                    ArrayList<SealedObject> arr=(ArrayList<SealedObject>) in.readObject();
                    HashMap<String,Integer> voteMap = new HashMap<>();

                    for(int i=1; i<arr.size(); i++)
                    {
                        Block blk = (Block) decrypt(arr.get(i));
                        String key = blk.getVoteObj().getGlasacPartija();

                        voteMap.put(key,0);
                    }

                    for(int i=1;i<arr.size();i++) {
                        Block blk = (Block) decrypt(arr.get(i));
                        String key = blk.getVoteObj().getGlasacPartija();

                        voteMap.put(key, voteMap.get(key)+1);
                    }
                    in.close();

                    for(Map.Entry<String, Integer> entry : voteMap.entrySet()) {
                        System.out.println(entry.getKey() + " : " + entry.getValue());
                    }

                    System.out.println("-------------------------\n");
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }

        else if(izbor == 0)
            exit(0);
    }

    public static void showHelp() {
        System.out.println("Restartiraj, i selektiraj uloga kako server ili klient.");
        exit(0);
    }

    public static Object decrypt(SealedObject sealedObject) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");


        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sks);

        try {
//    		System.out.println(sealedObject.getObject(cipher));
            return sealedObject.getObject(cipher);
        } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}