package com.deepsea.client.client;

import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Akira on 03.02.2015.
 */
public class NetUtils {
    static byte[] Magic = "DSCS".getBytes(Charset.forName("UTF-8"));

    enum PacketType
    {
        Discover((byte)0),
        DiscoverReply((byte)1),
        ConnectionRequest((byte)2),
        ConnectionReply((byte)3),
        EstablishConnection((byte)4),
        ImageFragment((byte)5);

        private final byte Type;
        PacketType(byte Type) {this.Type = Type;}
        public byte getValue() {return Type;}
    }


    static boolean ValidatePayload(DatagramPacket Source)
    {
        byte[] Data = Source.getData();
        for(int i = 0; i < Magic.length; i++)
            if(Magic[i] != Data[i])
                return false;
        return true;
    }
}
