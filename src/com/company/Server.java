package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class Server {
    private final static int BUFFER_SIZE = 256;

    private AsynchronousServerSocketChannel serverSocketChannel;
    private final HttpHandler httpHandler;

    Server(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public void bootstrap() {
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8080));

            while (true) {
                Future<AsynchronousSocketChannel> future = serverSocketChannel.accept();
                handleClient(future);
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Future<AsynchronousSocketChannel> future)
            throws InterruptedException, ExecutionException, IOException {
        System.out.println("New client connection");

        AsynchronousSocketChannel clientChannel = future.get();

        while (clientChannel != null && clientChannel.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder stringBuilder = new StringBuilder();
            boolean keepReading = true;

            while (keepReading) {
                int readBytes = clientChannel.read(buffer).get();

                keepReading = readBytes == BUFFER_SIZE;
                buffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
                stringBuilder.append(charBuffer);

                buffer.clear();
            }

            HttpRequest request = new HttpRequest(stringBuilder.toString());
            HttpResponse response = new HttpResponse();

            if (httpHandler != null) {
                try {
                    String body = this.httpHandler.handle(request, response);

                    if (body != null && !body.isBlank()) {
                        if (response.getHeader("Content-Type") == null) {
                            response.addHeader("Content-Type", "text/html; charset=utf-8");
                        }
                        response.setBody(body);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    response.setStatusCode(500);
                    response.setStatus("Internal server error");
                    response.addHeader("Content-Type", "text/html; charset=utf-8");
                    response.setBody("<html><body><h1>Internal server error</h1></body></html>");
                }
            } else {
                response.setStatusCode(404);
                response.setStatus("Not Found");
                response.addHeader("Content-Type", "text/html; charset=utf-8");
                response.setBody("<html><body><h1>Resource not found</h1></body></html>");
            }

            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
            clientChannel.write(responseBuffer);

            clientChannel.close();
        }
    }
}
