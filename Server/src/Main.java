import Sockets.*;

public class Main {

    public static void main(String[] args) {

        Server server = new Server(64502,new Client());
    }
}


