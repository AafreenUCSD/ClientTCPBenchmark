package com.javacodegeeks.android.androidsocketclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Client extends Activity {
    //private ClientSocket clientSocket;
    private Socket socket;
    private int msgSize;
    private long endTime, startTime;
    private TextView label1;
    private String read;

    //private static final int SERVERPORT = 6000;
    //private static final String SERVER_IP = "137.110.90.29";
    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.0.2.2";

    Handler updateConversationHandler;

    Thread clientThread = null;

    public class MyTime {
        long time;
        String unit;

        public MyTime(long time, String unit) {
            this.time = time;
            this.unit = unit;
        }
    }

    public MyTime getTimeString(long time) {
        long convertedTime = time;
        if (time <= 999) {
            return new MyTime(convertedTime, "ns");
        } else if (time <= 999999) {
            convertedTime = time / 1000;
            return new MyTime(convertedTime, "us");
        } else if (time <= 999999999) {
            convertedTime = time / 1000000;
            return new MyTime(convertedTime, "ms");
        } else return new MyTime(convertedTime, "ns");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        label1 = (TextView) findViewById(R.id.textView);
        updateConversationHandler = new Handler();
        this.clientThread = new Thread(new ClientThread());
        this.clientThread.start();
    }

    //@Override
    /*
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

    private String createDataSize() {
        StringBuilder sb = new StringBuilder(msgSize);
        for (int i = 0; i < msgSize; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    /*
    public void onClick(View view) {
        try {
            EditText et = (EditText) findViewById(R.id.EditText01);
            String str = et.getText().toString();
            msgSize = Integer.parseInt(str); //str is the desired length of the string
            String sizeString = createDataSize();
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            Log.e("Client","Sending data to the server.");
            startTime = System.nanoTime();
            out.println(sizeString);
            try {
                while (in.readLine() != null) {
                    Log.e("Client",in.readLine());
                }
            }catch(Exception e){
                Log.e("Client", "Probably didn't receive anything from the server");
            }
            endTime = System.nanoTime();
            Log.e("Client","Received ack");
            updateConversationHandler.post(new updateUIThread());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void onClick(View view) {
        try {
            EditText et = (EditText) findViewById(R.id.EditText01);
            String str = et.getText().toString();
            msgSize = Integer.parseInt(str); //str is the desired length of the string
            String sizeString = createDataSize();
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            startTime = System.nanoTime();
            out.println(sizeString);
                try {
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket serverSocket;

        private BufferedReader input;

        public CommunicationThread(Socket serverSocket) {

            this.serverSocket = serverSocket;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    read = input.readLine();
                    endTime = System.nanoTime();
                    updateConversationHandler.post(new updateUIThread());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    class updateUIThread implements Runnable {
        public void updateUIThread() {
            // endTime = System.nanoTime();

        }
        public void run() {
            long measured_time = endTime - startTime;
            MyTime elapsedTime = getTimeString(measured_time);
            synchronized (label1) {
                label1.setText("RTT = " + elapsedTime.time + " " + elapsedTime.unit);
                //label1.setText(read);
            }
        }
    }
}