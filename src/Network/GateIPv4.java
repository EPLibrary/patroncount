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

import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * A simple IPv4 class. 
 * @author Andrew Nisbet andrew.nisbet@epl.ca
 * @version 1.0
 * @since   2018-10-22
 */
public class GateIPv4
{

    public String ip;
    public int port;

    /**
     * Creates GateIPv4 object with a default port of 10001.
     * @param ipValue the IP of the host being queried.
     */
    public GateIPv4(String ipValue)
    {
        // parse and check we have 4 octets of integers.
        this.ip = ipValue;
        // set the default port.
        this.port = 10001;
    }
    
    /**
     * Creates GateIPv4 object.
     * @param ipValue the stored IP (v4).
     * @param port the port used later for the socket connection.
     */
    public GateIPv4(String ipValue, int port)
    {
        // set the default to local host.
        this.ip = ipValue;
        // set the default port.
        this.port = port;
    }

    /**
     * Creates GateIPv4 object with default of '127.0.0.1' address, and port
     * 10001
     */
    public GateIPv4()
    {
        // set the default to local host.
        this.ip = "127.0.0.1";
        // set the default port.
        this.port = 10001;
    }

    /**
     * Tests if the stored IP is valid.
     * @return true if valid IP (v4), and false otherwise.
     */
    public boolean isValid()
    {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(this.getIp());
    }

    /**
     * @param ip the IP(v4) of the RFID gate.
     */
    public void setIp(String ip)
    {
        this.ip = ip;
    }

    /**
     * @param port the target gate's query communication port.
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the IPv4. 
     */
    public String getIp()
    {
        return ip;
    }

    /**
     * @return the port number.
     */
    public int getPort()
    {
        return port;
    }
    
    /**
     * Gets the host name for the gate.
     * @return The name or IP of the host of the query.
     */
    public String getHost()
    {
        return this.getIp();
    }
    
}
