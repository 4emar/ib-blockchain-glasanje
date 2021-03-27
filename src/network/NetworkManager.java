package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class NetworkManager implements Runnable{

    /*
     * Prati poraka na soket
     */
    public void sendMsg(Socket socket, MessageStruct msg)
            throws IOException {
        ObjectOutputStream prati;

        prati = new ObjectOutputStream(socket.getOutputStream());
        prati.writeObject(msg);
    }

    /*
     * Primi poraka
     */
    public void receiveMsg(Socket socket)
            throws ClassNotFoundException, IOException {
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
        Object inObj = inStream.readObject();

        if (inObj instanceof MessageStruct) {
            MessageStruct msg = (MessageStruct) inObj;
            msgHandler(msg, socket);
        }

    }

    /*
     * Zatvori go soketot
     */
    public void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
       Inerfejs za ServerManagaer i ClientManager
     */
    public abstract void msgHandler(MessageStruct msg, Socket src);
}
