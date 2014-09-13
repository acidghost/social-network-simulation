package it.uniba.di.itps.SNVSimulation.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

/**
 * Created by acidghost on 13/09/14.
 */
public class Serializer {
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException, ClassNotFoundException {
        byte [] data = decoder.decodeBuffer(s);
        //byte[] data = s.getBytes();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.flush();
        oos.close();
        return encoder.encode( baos.toByteArray() );
        //return new String(baos.toByteArray());
    }
}
