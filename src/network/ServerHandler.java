package network;

import java.io.IOException;
import java.net.Socket;

/**
 * ServerManager
 *
 * Klasa za da se spremi i da ceka poraka od clientot preku soketot.
 *
 */
public class ServerHandler extends Thread{
    /* soketot od koj sto ke prima poraki */
    private Socket _socket = null;
    public ServerManager _svrMgr = null;

    public ServerHandler(ServerManager svrMgr, Socket socket) {
        _svrMgr = svrMgr;
        _socket = socket;
    }

    /*
     Loop funkcija, koja ke prima poraki od clientot se dodeka ne se prekine konekcijata ( ne stisne deka nesaka da glasa)
     Posle toa, se vika ServerManager za da se izbrise klientot
     */
    @Override
    public void run() {
        while (true) {
            try {
                _svrMgr.receiveMsg(_socket);
            } catch (IOException | ClassNotFoundException e) {
                _svrMgr.clientDisconnected(_socket);
                break;
            }
        }

    }
}

