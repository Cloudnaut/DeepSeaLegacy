package com.deepsea.client.client;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Network;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.style.LineHeightSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;


public class MainActivity extends Activity {

    enum NetworkState{
        Init,
        Waiting,
        SendData,
        ReceiveData,
        Execution,
        TimeOut,
        Error
    }

     class NetworkTask implements Runnable
     {
         private Activity ParentActivity = null;
         private NetworkState CurrentState = NetworkState.Init;
         private DatagramSocket ServerSocket;
         private byte[] DataBuffer;
         int FrameWidth = 0;
         int FrameHeight = 0;


         public NetworkTask(Activity ParentActivity)
         {
             this.ParentActivity = ParentActivity;
         }


         private NetworkState StateInit()
         {
             try
             {
                 ServerSocket = new DatagramSocket(1066);
                 DataBuffer = new byte[1024 * 1024];

                 return NetworkState.Waiting;
             }
             catch (SocketException e)
             {
                 e.printStackTrace();
                 return NetworkState.Error;
             }
         }

         private NetworkState StateWaiting()
         {
             while(true)
             {
                 DatagramPacket ReceivedPacket = new DatagramPacket(DataBuffer, DataBuffer.length);

                 try
                 {
                     ServerSocket.receive(ReceivedPacket);

                     if(!NetUtils.ValidatePayload(ReceivedPacket))
                         return NetworkState.Error;

                     byte PayloadType = (byte)ReceivedPacket.getData()[4];

                     if(PayloadType == NetUtils.PacketType.Discover.getValue())
                     {
                         PayloadType = PayloadType;
                         //TODO: DiscoverReply
                     }
                     else if(PayloadType == NetUtils.PacketType.ConnectionRequest.getValue())
                     {
                         return NetworkState.SendData;
                     }
                 }
                 catch (IOException e)
                 {
                     e.printStackTrace();
                     return NetworkState.Error;
                 }
             }
         }


         private NetworkState StateSendData()
         {
             //TODO: Send client information
             return NetworkState.ReceiveData;
         }


         private NetworkState StateReceiveData()
         {
             DatagramPacket ReceivedPacket = new DatagramPacket(DataBuffer, DataBuffer.length);

             while(true)
             {
                 try
                 {
                     ServerSocket.receive(ReceivedPacket);

                     if(!NetUtils.ValidatePayload(ReceivedPacket))
                         return NetworkState.Error;

                     byte PayloadType = (byte)ReceivedPacket.getData()[4];

                     if(PayloadType == NetUtils.PacketType.EstablishConnection.getValue())
                     {
                         byte[] Width = Arrays.copyOfRange(ReceivedPacket.getData(), 5, 9);
                         byte[] Height = Arrays.copyOfRange(ReceivedPacket.getData(), 9, 13);

                         this.FrameWidth = ByteBuffer.wrap(Width).getInt();
                         this.FrameHeight = ByteBuffer.wrap(Height).getInt();

                         return NetworkState.Execution;
                     }
                 }
                 catch (IOException e)
                 {
                     e.printStackTrace();
                     return NetworkState.Error;
                 }
             }
         }


         private NetworkState StateExecution()
         {
             Runnable ChangeView = new Runnable() {
                 @Override
                 public void run() {
                     ParentActivity.setContentView(R.layout.activity_execution);

                     synchronized (this) {
                         this.notify();
                     }
                 }
             };

             synchronized (ChangeView)
             {
                 ParentActivity.runOnUiThread(ChangeView);
                 try {
                     ChangeView.wait();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }

             //Updaten der bilder und verschicken von sensordaten

             DatagramPacket ReceivedPacket = new DatagramPacket(DataBuffer, DataBuffer.length);

             final ImageView ScreenView = (ImageView) findViewById(R.id.ScreenView);

             Bitmap.Config conf = Bitmap.Config.RGB_565;
             final Bitmap ScreenBitmap = Bitmap.createBitmap(this.FrameWidth, this.FrameHeight, conf);

             ScreenBitmap.eraseColor(Color.BLACK);

             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     ScreenView.setImageBitmap(ScreenBitmap);
                 }
             });

             while(true)
             {
                 try
                 {
                     ServerSocket.receive(ReceivedPacket);

                     if(!NetUtils.ValidatePayload(ReceivedPacket))
                        break;

                     byte PayloadType = (byte)ReceivedPacket.getData()[4];

                     if(PayloadType == NetUtils.PacketType.ImageFragment.getValue())
                     {
                         //TODO: Pixel extrahieren
                         int PosX = ByteBuffer.wrap(ReceivedPacket.getData(), 5, 4).getInt();
                         int PosY = ByteBuffer.wrap(ReceivedPacket.getData(), 9, 4).getInt();
                         int PayloadLength = ByteBuffer.wrap(ReceivedPacket.getData(), 13, 4).getInt();
                         Bitmap ScreenFragment = BitmapFactory.decodeByteArray(ReceivedPacket.getData(), 17, 17 + PayloadLength);


                         int[] FragmentPixels = new int[ScreenFragment.getWidth() * ScreenFragment.getHeight()];
                         ScreenFragment.getPixels(FragmentPixels, 0, ScreenFragment.getWidth(), 0, 0, ScreenFragment.getWidth(), ScreenFragment.getHeight());

                         //TODO: Bilder aktualisieren

                         ScreenBitmap.setPixels(FragmentPixels, 0, ScreenFragment.getWidth(), PosX, PosY, ScreenFragment.getWidth(), ScreenFragment.getHeight());


                         ScreenView.postInvalidate();
                     }
                 }
                 catch (IOException e)
                 {
                     e.printStackTrace();
                 }
             }


             return NetworkState.Waiting;
         }



         private NetworkState ExecuteState(NetworkState State)
         {
             NetworkState NextState = NetworkState.Error;

             switch (State)
             {
                 case Init:
                     NextState = StateInit();
                     break;
                 case Waiting:
                     NextState = StateWaiting();
                     break;
                 case SendData:
                     NextState = StateSendData();
                     break;
                 case ReceiveData:
                     NextState = StateReceiveData();
                     break;
                 case Execution:
                     NextState = StateExecution();
                     break;
                 default:
                     break;
             }

             return NextState;
         }


         @Override
         public void run()
         {
             while(true)
             {
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {

                         TextView StatusText = ((TextView)findViewById(R.id.TxtStatus));

                         switch (CurrentState)
                         {
                             case Init:
                                 StatusText.setText("Initializing System");
                                 break;
                             case Waiting:
                                 StatusText.setText("Waiting for Host");
                                 break;
                             case SendData:
                                 StatusText.setText("Sending Client Information");
                                 break;
                             case ReceiveData:
                                 StatusText.setText("Receiving Server Information");
                                 break;
                             case Error:
                                 StatusText.setText("An Error occurred");
                                 break;
                         }
                     }
                 });


                 CurrentState = ExecuteState(CurrentState);
             }
         }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Hide useless bars
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        new Thread(new NetworkTask(this)).start();
    }

}