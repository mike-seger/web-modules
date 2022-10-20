package com.net128.oss.web.webshell.util;

import org.apache.commons.cli.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class NetCat {
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("l", "listen", false, "listen mode");
        options.addOption("p", "port", true, "port number");

        CommandLine line = parser.parse(options, args);

        if (line.hasOption('l')) {
            if (line.hasOption('p')) {
                int port = Integer.parseInt(line.getOptionValue('p'));
                listen(port);
            }
        } else {
            if (line.hasOption('p')) {
                int port = Integer.parseInt(line.getOptionValue('p'));
                connect(line.getArgs()[0], port);
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("netcat [OPTIONS] <HOST>", options);
            }
        }
    }

    private static void connect(String host, int port) throws Exception {
        System.err.println("Connecting to " + host + " port " + port);
        final Socket socket = new Socket(host, port);
        transferStreams(socket);
    }

    private static void listen(int port) throws Exception {
        System.err.println("Listening at port " + port);
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.err.println("Accepted");
        transferStreams(socket);
    }

    private static void transferStreams(Socket socket) throws IOException,
            InterruptedException {
        InputStream input1 = System.in;
        OutputStream output1 = socket.getOutputStream();
        InputStream input2 = socket.getInputStream();
        PrintStream output2 = System.out;
        Thread thread1 = new Thread(new StreamTransferer(input1, output1));
        Thread thread2 = new Thread(new StreamTransferer(input2, output2));
        thread1.start();
        thread2.start();
        thread1.join();
        socket.shutdownOutput();
        thread2.join();
    }

    private static class StreamTransferer implements Runnable {
        private final InputStream input;
        private final OutputStream output;

        public StreamTransferer(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                PrintWriter writer = new PrintWriter(output);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}