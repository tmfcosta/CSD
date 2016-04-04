package csd.aula2.net;

import java.io.* ;
import java.net.* ;

import javax.net.ssl.SSLSocketFactory;

public class SSLClient {
    
    public static void main(String[] args) throws Exception {
    	if( args.length != 2) {
    		System.err.println("Use: java csd.aula2.SSLClient hostname port");
    		return;
    	}
        
        String input ;
        
        System.setProperty("javax.net.ssl.trustStore", "client.ks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
        
        Socket cs = SSLSocketFactory.getDefault().createSocket( args[0], Integer.parseInt(args[1]) ) ;
        OutputStream os = cs.getOutputStream() ;
        BufferedReader dis = new BufferedReader( new InputStreamReader( cs.getInputStream())) ;
        BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
        do {
            input = reader.readLine() + "\n";
            os.write( input.getBytes() ) ;
            System.out.println(dis.readLine());
        } while( ! input.equals(".\n") ) ;
        os.close() ;
        cs.close() ;
    }
}
