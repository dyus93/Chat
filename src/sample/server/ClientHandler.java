package sample.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ServerMain server;
    private String nick;
    private List<String> blackList;

    public ClientHandler( ServerMain server, Socket socket){
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.server = server;
            this.blackList = new ArrayList<>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokes = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokes[1], tokes[2]);
                                if (newNick != null) {
                                    sendMsg("/authok");
                                    nick = newNick;
                                    server.subscribe(ClientHandler.this);
                                    break;
                                } else {
                                    sendMsg("Неверный логин или пароль");
                                }
                            }
                        }
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                               if (str.equals("/end")){
                                   out.writeUTF("/serverclosed");
                                   break;
                               }
                               if (str.startsWith("/w")){
                                    String[] tokens = str.split(" ",3);
                                    server.sendPersonalMessage(ClientHandler.this, tokens[1],tokens[2]);

                               }
                                if (str.startsWith("/blacklist")) {
                                    String[] tokens = str.split(" ");
                                    blackList.add(tokens[1]);
                                    sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список" );
                                }
                            }else{
                                   server.broadcastMsg(ClientHandler.this, nick + ": " + str);
                               }
                                System.out.println("client: " + str);

                        }
                    }catch (IOException e ){
                        e.printStackTrace();
                    }finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }server.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void sendMsg(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getNick(){
        return nick;
    }
    public List<String> getBlackList(){
        return blackList;
    }
    public boolean checkBlackList(String nick){
        return blackList.contains(nick);
    }
}
