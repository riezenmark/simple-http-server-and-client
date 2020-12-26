package com.company.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8080);

        InputStream response = socket.getInputStream();
        OutputStream request = socket.getOutputStream();

        byte[] data = ("GET / HTTP/1.1\n"
                + "Host: localhost\n\n").getBytes();

        request.write(data);

        int c;

        while ((c = response.read()) != -1) {
            System.out.print((char) c);
        }

        socket.close();
    }
}
