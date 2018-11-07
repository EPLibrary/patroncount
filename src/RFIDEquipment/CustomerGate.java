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
package RFIDEquipment;

import Network.IOSocket;
import Network.GateIPv4;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import patroncount.Patroncount;

/**
 * This application can talk to a number of different gates (that number is 
 * currently 3), and some manufacturers have variations. The class takes a
 * request for a given make/model of gate and builds a working instance, ready
 * for running queries against.
 * 
 * @author Andrew Nisbet andrew.nisbet@epl.ca
 * @version 1.0
 * @since   2018-10-22
 */
public abstract class CustomerGate
{
    private static CustomerGate instance;
    private static boolean DEBUG;
    
    public static CustomerGate getInstance(
            SupportedGateType gate, 
            String gateIP, 
            boolean debug)
    {
        DEBUG = debug;
        if (instance == null)
        {
            switch (gate)
            {
                case _3M_9100_:
                    instance = new ThreeMGate(gateIP);
                    break;
                case _FEIG_ID_ISC_LR2500_B_:
                    instance = new FeigGate(gateIP);
                    break;
                case _FEIG_ID_ISC_LR2500_B_DUAL_AISLE:
                    instance = new FeigGateDualAisle(gateIP);
                    break;
                default:
                    System.err.println("***error, customer"
                            + " gate type not supported.");
                    Patroncount.displayHelp();
            }
        }
        return instance;
    }
    
    /**
     * The older 3M gates need some time to fetch counts internally then relay
     * them. The sweet spot for the older gates is 3 seconds, pretty slow. If
     * after 3 seconds the gate fails to respond, consider it not connected 
     * via the network and move onto another gate.
     * @param seconds duration before considering a gate not connected via the
     * network.
     */
    public abstract void setTimeout(int seconds);
    
    /**
     * Sets the type of query we will ask the gate. The default for all gates
     * in this application is customer counts, which is set during the class
     * construction. If, in the future you would like to add functionality, you 
     * would extend the SupportedQuery types in each of the supported gate
     * models that can be instantiated with the 
     * {@link #getInstance(RFIDEquipment.SupportedGateType, java.lang.String, boolean)}
     * method.
     * 
     * @param type the query type that the gate is expected to execute.
     */
    public abstract void setQuery(SupportedQueryType type);
    
    /**
     * Triggers the query to be run against a given gate.
     * @return results string. 
     * @see ResultsFormatter for more information on displaying output results.
     */
    public abstract String queryGate();
    
    /** An alternate type of gate from Bibliotheca that has recorders for more
     * than one aisle.
     */
    private static class FeigGateDualAisle extends FeigGate
    {
        public FeigGateDualAisle(String ip)
        {
            super(ip);
            this.formatter = CustomerCountFormatter.getInstance(
                    SupportedGateType._FEIG_ID_ISC_LR2500_B_DUAL_AISLE, 
                    DEBUG
            );
        }
    }
    /**
     * Customer gate type manufactured by FEIG GmB. One of types of gates
     * EPL has purchased.
     */
    private static class FeigGate extends CustomerGate
    {
        /**
         * The types of queries this type of device supports.
         */
        protected enum SupportedQueries
        {
            CUSTOMER_COUNTS("020012ff9f000d02020008017700ee024431");

            private final String type;

            private SupportedQueries(String s)
            {
                this.type = s;
            }

            @Override
            public String toString()
            {
                return this.type;
            }

            public String getMessage()
            {
                return this.type;
            }
        }
        // Set the correct port for this type of gate, since the gateIP has 
        // a default of 10001.
        protected final static int PORT = 10001;
        protected final GateIPv4 ip;
        protected SupportedQueries QUERY;
        protected ResultsFormatter formatter;
        protected int timeout;

        /**
         * Constructor to create a patron gate of type FEIG, one or two aisle.
         * @param ip v4 IP of the gate.
         */
        public FeigGate(String ip)
        {
            this.ip        = new GateIPv4(ip, PORT);
            this.QUERY     = SupportedQueries.CUSTOMER_COUNTS;
            this.formatter = CustomerCountFormatter.getInstance(
                    SupportedGateType._FEIG_ID_ISC_LR2500_B_, 
                    DEBUG
            );
            this.timeout   = 1;
        }
        
        @Override
        public void setQuery(SupportedQueryType type)
        {
            // FIEG gates can support 2 useful queries, customer counts and
            // reset counts (not implemented yet).
            switch(type)
            {
                case CUSTOMER_COUNTS:
                    this.QUERY = SupportedQueries.CUSTOMER_COUNTS;
                    break;
                case RESET_COUNTS:
                    System.err.println(
                            "***error the reset counts command is not "
                            + "supported yet."
                    );
                    Patroncount.displayHelp();
                default:
                    System.err.println(
                            "***error this command is not "
                            + "supported yet."
                    );
                    Patroncount.displayHelp();
            }
        }
        
        @Override
        public void setTimeout(int seconds)
        {
            this.timeout = seconds;
        }
        
        @Override
        public String queryGate()
        {
            IOSocket socket = new IOSocket();
            socket.startConnection(this.ip.getIp(), this.ip.getPort());
            socket.sendMessage(this.QUERY.getMessage());
            try 
            {
                // The old gates needed some delay for the hardware to respond.
                TimeUnit.SECONDS.sleep(this.timeout);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(FeigGate.class.getName()).log(Level.SEVERE, null, ex);
            }
            String results = socket.readBytes();
            // If there is another application connected to the port, you won't get
            // any data, because someone else is hogging the connection so test if 
            // you get any data back. 
            if (results.length() == 0)
            {
                System.err.println("Can't read socket. Host:" + this.ip.getIp() 
                        + ", port:" + this.ip.getPort() 
                        + ". Is another application connected?");
            }
            else
            {
                if (DEBUG)
                {
                    System.out.println("count data recv'd:" + results);
                }
            }
            socket.stopConnection();
            return this.formatter.format(results);
        }
    }
    
    /**
    * Concrete implementation of the 3M gate.
    * @author Andrew Nisbet <andrew.nisbet@epl.ca>
    */
    private static class ThreeMGate extends CustomerGate
    {
        /**
         * The types of queries this type of device supports.
         */
        private enum SupportedQueries
        {
           CUSTOMER_COUNTS("63000406000D59");

           private final String type;

           private SupportedQueries(String s)
           {
               this.type = s;
           }

           @Override
           public String toString()
           {
               return this.type;
           }

           public String getMessage()
           {
               return this.type;
           }
       }

       private final static int PORT = 2101;
       private SupportedQueries QUERY;
       private final ResultsFormatter formatter;
       private final GateIPv4 ip;
       private int timeout;

       /**
        * Constructor to make a patron gate of 3M manufacture.
        * @param ip v4 IP of the gate.
        */
        public ThreeMGate(String ip)
        {
            this.ip        = new GateIPv4(ip, PORT);
            this.QUERY     = SupportedQueries.CUSTOMER_COUNTS;
            this.formatter = CustomerCountFormatter.getInstance(
                    SupportedGateType._3M_9100_,
                    DEBUG
            );
            this.timeout   = 3;
        }

        @Override
        public void setQuery(SupportedQueryType type)
        {
            // FIEG gates can support 2 useful queries, customer counts and
            // reset counts (not implemented yet).
            switch(type)
            {
                case CUSTOMER_COUNTS:
                    this.QUERY = SupportedQueries.CUSTOMER_COUNTS;
                    break;
                default:
                    System.err.println(
                            "***error this command is not "
                            + "supported by this gate make and model."
                    );
                    Patroncount.displayHelp();
            }
        }
        
        @Override
        public String queryGate()
        {
            IOSocket socket = new IOSocket();
            socket.startConnection(this.ip.getIp(), this.ip.getPort());
            socket.sendMessage(this.QUERY.getMessage());
            try 
            {
                // The old gates needed some delay for the hardware to respond.
                TimeUnit.SECONDS.sleep(this.timeout);
            } 
            catch (InterruptedException ex) 
            {
                // Logger.getLogger(ThreeMGate.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("**warn: device " + this.ip.getHost() +
                        " didn't respond within " + (this.timeout + 2) +
                        " seconds. Is the gate up and connected to the network?");
            }
            String response = socket.readBytes();
            // If there is another application connected to the port, you won't get
            // any data, because someone else is hogging the connection so test if 
            // you get any data back. 
            if (response.length() == 0)
            {
                System.err.println("Can't read socket. Host:" + this.ip.getIp() 
                        + ", port:" + this.ip.getPort() 
                        + ". Is another application connected?");
            }
            else
            {
                if (DEBUG)
                {
                    System.out.println("count data recv'd:" + response);
                }
            }
            socket.stopConnection();
            return this.formatter.format(response);
        }
        
        @Override
        public void setTimeout(int seconds)
        {
           this.timeout = seconds;
        }
    }
}
