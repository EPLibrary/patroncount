/*
 * Copyright (C) 2018 Andrew Nisbet <andrew.nisbet@epl.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a raw socket system connection. 
 * 
 * This class is unusual since it sends and receives data in strings of hex
 * bytes. These strings get converted and sent on the hardware layer to their
 * destination socket.
 * 
 * @author Andrew Nisbet andrew.nisbet@epl.ca
 * @version 1.0
 * @since   2018-10-22
 */
public class IOSocket 
{
    private Socket clientSocket;
    private DataOutputStream out;
    private InputStream in;
 
    /**
     * Starts the connection to remote device with a default timeout of 15 seconds.
     * @param ip
     * @param port 
     */
    public boolean startConnection(String ip, int port)
    {
        return this.startConnection(ip, port, 5000);
    }
    
    /**
     * Starts connection to the remote device.
     * @param ip host or IP of the remote device.
     * @param port port on the remote device.
     * @param timeout in milliseconds
     */
    public boolean startConnection(String ip, int port, int timeout)
    {
        try 
        {
            InetAddress address = InetAddress.getByName(ip);
            SocketAddress sockAddress = new InetSocketAddress(address,port);
            clientSocket = new Socket();
            clientSocket.connect(sockAddress, timeout);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
        } 
        catch (SocketTimeoutException ex)
        {
            System.err.println("**warn: host '" + ip + "' is not responding "
                    + "within " + timeout + " milliseconds.");
            return false;
        } 
        catch (UnknownHostException ex)
        {
            System.err.println("***error: unknown host '" + ip + "'.");
            return false;
        }
        catch (ConnectException ex)
        {
            System.err.println("***error: host '" + ip + "' refusing connection.");
            return false;
        }
        catch (IOException ex)
        {
            Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * Converts a String into a byte array to be put as raw bytes.
     * @param s - string that will be converted into an array of bytes.
     * @return byte array.
     */
    public static byte[] hexStringToByteArray(String s) 
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
 
    /**
     * Used by caller to send a string message over a socket. The message is
     * converted to a byte array before sending. See {@link #hexStringToByteArray(java.lang.String) }.
     * 
     * @param msg - message string to be sent over socket.
     * @return - string of the response, converted from byte array.
     */
    public String sendMessage(String msg) 
    {
        byte[] message = IOSocket.hexStringToByteArray(msg);
        try 
        {
            out.write(message);
        } catch (IOException ex) {
            Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    /**
     * Reads bytes from a socket and converts them string form.
     * @return string form of the bytes read from a socket.
     */
    public String readBytes() 
    {
        byte[] buffer = new byte[500];
        Formatter f = new Formatter();
        try 
        {
            int count;
            while(true)
            {
                count = in.read(buffer,0,500);
                break;   
            }
            // Most messages fit the buffer so format it for consumption.
            for (int i = 0; i < count; i++)
            {
                f.format("%02x", buffer[i]);
            }
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally 
        {
            // close stream resources
            if (in != null) {
                try 
                {
                    in.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return f.toString();
    }
 
    /**
     * Closes the socket connection.
     */
    public void stopConnection() 
    {
        try 
        {
            in.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            out.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        try 
        {
            clientSocket.close();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(IOSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
