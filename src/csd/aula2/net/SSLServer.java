package csd.aula2.net;

import java.io.* ;
import java.net.* ;

import javax.net.ssl.SSLServerSocketFactory;

public class SSLServer {
    
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "server.ks");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");    	
    	
        ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket( Integer.parseInt(args[0]) ) ;
        while( true ) {
            Socket cs = ss.accept() ;
            System.out.println("Accepted connection from client at: " + cs.getInetAddress().getHostName() + ":" + cs.getPort() ) ;
            
            InputStream is = cs.getInputStream() ;
            OutputStream os = cs.getOutputStream() ;
            byte[] buffer = new byte[1024] ;
            int n ;
            while( (n = is.read( buffer )) != -1 ) {
                System.out.write( buffer, 0, n ) ;

                byte[] b = new String( buffer, 0, n).toUpperCase().getBytes();
            	os.write(b, 0, b.length);
            }
            cs.close() ;
            System.out.println("Connection closed.") ;
        }
    }
    
}
