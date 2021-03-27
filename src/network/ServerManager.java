package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SealedObject;

import blockchain.Block;

public class ServerManager extends NetworkManager {


    private ServerSocket _svrSocket = null;

    /* id na klient so moznost da se zgolemuva */
    private volatile AtomicInteger _cid = null;

    /* mapa megu id na klient i socket na klient */
    private volatile Map<Integer, Socket> _clients = null;


    public ServerManager(int svrPort) {
        try {
            _clients = new ConcurrentSkipListMap<Integer, Socket>();
            _cid = new AtomicInteger(0);

            _svrSocket = new ServerSocket(svrPort);

            System.out.println("Čekam da se povrze klient..");
            System.out.println("Klientot neka se povrze na " + InetAddress.getLocalHost() + ":" + svrPort + ".");
        } catch (IOException e) {
            System.out.println("ERROR: Ne možam da slušam na portata " + svrPort);
            e.printStackTrace();
        }
    }

    /*
     * Loop za da se prifakaat novi klienti. Koga ima vospostaveno konekcija,
     * kreiraj nova istanca od ServerHandler vo nov thread, za da primas poraki
     * preku ovoj loop.
     */
    @Override
    public void run() {
        while (true) {
            try {
                /* prifakanje na nov klient */
                Socket socket = _svrSocket.accept();
                addClient(socket);
                System.out.println("Nov klient(clientID is " + getCid(socket) + ") povrzan!");

                /* Kreiraj nova istanca na ServerHandler za da primas poraki */
                new ServerHandler(this, socket).start();
                /* Prati go klient id-to na noviot klient */
                sendMsg(socket, new MessageStruct(2, Integer.valueOf(getCid(socket))));
            } catch (IOException e) {
                /* Server soketot e zatvoren */
                break;
            }
        }
    }

    public void clientDisconnected(Socket client) {
        int cid = getCid(client);
        System.out.println("Klientot " + cid + " se diskonektiraše.");

        deleteClient(cid);
    }

    /* Message handler */

    @Override
    public void msgHandler(MessageStruct msg, Socket src) {
        switch (msg._code) {
            case 0:
                break;
            case 1:
                System.out.println("Broadcasting : " + (String)msg._content.toString());

                broadcast((SealedObject)msg._content,src );
                break;
            default:
                break;
        }
    }

    private void broadcast(SealedObject o, Socket src) {

        //pravi broadcast
        int srcCid=getCid(src);
        for(int i = _cid.get()-1 ;i>=0;i--) {
            if(i!=srcCid) {
                try {
                    sendMsg(getClient(i), new MessageStruct(0, o));
                } catch (IOException e) {
                    System.out.println("ERROR: Konekcijata so " + srcCid + " e prekinata, ne može da se prati poraka!");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    continue;
                }


            }
        }
    }

    /* Klient */
    private void addClient(Socket socket) {
        _clients.put(Integer.valueOf(_cid.getAndIncrement()), socket);
    }

    private boolean deleteClient(int idx) {
        if (_clients.remove(Integer.valueOf(idx)) == null) {
            System.out.println("Neuspešno brišenje!");
            return false;
        }
        return true;
    }

    private Socket getClient(int cid) {
        return (Socket)_clients.get(Integer.valueOf(cid));
    }

    private int getCid(Socket socket) {
        for (Map.Entry<Integer, Socket> entry : _clients.entrySet()) {
            if (entry.getValue() == socket) {
                return entry.getKey().intValue();
            }
        }
        return -1;
    }

    public void close() {
        System.out.println("Serverot kje se zatvori. Site konektirani klienti kje se diskonektiraat.");
        try {
            _svrSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Prijatno!");
    }

}
