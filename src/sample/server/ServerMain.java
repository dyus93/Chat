package sample.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerMain {
    private Vector<ClientHandler> clients;

    public  ServerMain() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            String str = AuthService.getNickByLoginAndPass("login1", "pass1");
            System.out.println(str);
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен");

            while (true){
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    server.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }AuthService.disconnect();
            }
        }
    }

    public void subscribe(ClientHandler client){
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client){
        clients.remove(client);
    }

    public void broadcastMsg(ClientHandler from, String msg){
        for (ClientHandler o:clients) {
            if (!o.checkBlackList(from.getNick())){
                o.sendMsg(msg);
            }

        }
    }
    public void sendPersonalMessage(ClientHandler from, String nickTo,String msg){
        for (ClientHandler o : clients){
            if (o.getNick().equals(nickTo)){
                o.sendMsg("from " + from.getNick() + ": " + msg);
                from.sendMsg("to " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("Клиент с ником " + nickTo + " не найден в чате");
    }
    public boolean isNickBusy(String nick){
        for (ClientHandler o : clients){
            if (o.getNick().equals(nick)){
                return true;
            }
        }
    }
}
