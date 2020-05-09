/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that allows a client application to connect to a server socket and then     */
/* the application sends the "note on" command to the client application.                                 */
/* I created a simple Java client application that ran on a Windows PC and used the Java MIDI             */
/* implementation to send the commands to a synthesizer running on the PC.  Unfortunately, the delay      */
/* was unacceptable                                                                                       */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.audiogenerator.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.romware.i2cdrumkit.audiogenerator.IAudioGenerator;
import uk.co.romware.i2cdrumkit.audiogenerator.midi.MidiAudioGenerator;

public class ServerAudioGenerator implements IAudioGenerator {

	private class Client {
		private Socket m_Socket;
		private ObjectOutputStream m_Stream;
		
		public Client(Socket p_Socket) throws IOException {
			m_Socket = p_Socket;
			m_Stream = new ObjectOutputStream(p_Socket.getOutputStream());
			m_Stream.flush();
		}
		
		public boolean write(int p_Note, int p_Velocity) {
			try {
				m_Stream.writeInt(p_Note);
				m_Stream.writeInt(p_Velocity);
				m_Stream.flush();
				return true;
			} catch (Exception ex) {
				return false;
			}
		}
		
		public boolean isClosed() {
			return m_Socket.isClosed();
		}
	}
	private final static Logger LOGGER = LoggerFactory.getLogger(MidiAudioGenerator.class);
	
	private ServerSocket m_ServerSocket;
	private Set<Client> m_ClientSockets = new HashSet<Client>();
	
	public ServerAudioGenerator() throws IOException {
		m_ServerSocket = new ServerSocket(Integer.getInteger("serverPort"));
		LOGGER.info("Listening on port " + Integer.getInteger("serverPort"));
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						LOGGER.info("Waiting for connection");
						Socket socket = m_ServerSocket.accept();
						LOGGER.info("Got connection");
						
						synchronized (m_ClientSockets) {
							m_ClientSockets.add(new Client(socket));
							LOGGER.info("Added connection");
						}
					} catch (Exception ex) {
					}
				}
			}
		});
		t.setName("Server Socket Listener");
		t.start();
	}

	@Override
	public void playNote(int p_Note, int p_Velocity) {
		synchronized(m_ClientSockets) {
			for (Iterator<Client> it = m_ClientSockets.iterator(); it.hasNext();) {
			    Client client = it.next();
			    if (client.isClosed()) {
			    	it.remove();
			    	continue;
			    }
			
			    if (!client.write(p_Note, p_Velocity)) {
			    	it.remove();
			    }
			}
		}
	}
	
	
	public void stopNote (int p_Note) {		
	}

}
