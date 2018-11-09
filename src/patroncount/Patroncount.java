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
package patroncount;

import RFIDEquipment.SupportedGateType;
import RFIDEquipment.CustomerGate;
import Network.GateIPv4;
import RFIDEquipment.CustomerCountFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Patroncount is the new incarnation of my C# project patroncount. The purpose
 * is to query RFID gates for counts of customers. See {@link SupportedGateType}
 * for the list of supported gates.
 * 
 * All output is written to standard out, and is displayed in the form of 2 
 * integers, the in-count and out-count separated by a pipe ('|') character. A 
 * trailing pipe is also added to conform the the output of the original 
 * application. 
 * 
 * @author Andrew Nisbet andrew.nisbet@epl.ca
 * @version 1.0
 * @since   2018-10-22
 */
public class Patroncount
{
    private static boolean DEBUG;
    private final static String VERSION = "1.2.1";

    public static void displayHelp(int i)
    {
        System.err.println("Usage: patroncount.jar [-dhvx] [-i gate_ip] [-t gate_type] [-s {integer}]");
        System.err.println(" Exmaple: Patroncount -g 10.2.19.113");
        System.err.println("    Only IPv4 is currently supported. This may change.");
        System.err.println("    This application will query a patron gate for patron in and out counts.");
	System.err.println("    If successful it will print 'in_count|out_count|'. On failure it will");
	System.err.println("    output '-1|-1|'. A timeout or any SocketException will cause failure.");
        System.err.println(" The application currently supports the following RFID gate models.");
        System.err.println();
        System.err.println(" Switches:");
        System.err.println(" -d output debug information.");
        System.err.println(" -h usage message.");
        System.err.println(" -i{10.0.0.127} the IPv4 address of the target gate.");
        System.err.println(" -s{seconds} Sets the expected delay between having received the query to the");
        System.err.println("   time it takes to respond, after which the gate is deemed to be off line.");
        System.err.println("   Each gate type has its own default value, so you shouldn't need this.");
        System.err.println(" -t{[3M]|[FEIG|FEIGx1]|FEIGx2|OFFLINE} (case insensitive)");
        System.err.println("   Specifies the type, (model and manufacturer) of the target gate.");
        System.err.println("   The default is '3M', in which case -t is optional.");
        System.err.println("   FEIG and FEIGx1 are equivalent.");
        System.err.println("   'offline', 'unknown', 'Undefined' are all equivalent and will "
                + "always return '-1|-1|'.");
        System.err.println(" -v display version information then exit.");
        System.err.println(" -x usage message. Same as -h, but consistent with other applications.");
        // TODO: add timeout for operations to match the times in the -t flag.
        // TODO: Exceptions should exit, not hang.
        System.err.println();
        System.err.println("Version: " + VERSION);
        System.exit(i);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // Test using these 2 gates:
//        String host = "10.2.19.113";  // 3M - IDY gate specifically
//        String host = "10.2.44.12"; // FEIGx1 - HVY gate specifically
//        String host = "10.2.30.38"; // FEIGx2 - LON gate specifically
        // First get the valid options
        Options options = new Options();
        options.addOption("d", false, "turns on debug information.");
        options.addOption("h", false, "usage help message.");
        options.addOption("x", false, "usage help message.");
        options.addOption("i", true, "gate IP. The IPv4 address for the gate to poll.");
        options.addOption("s", true, "sets the hardware delay (in seconds).");
        options.addOption("t", true, "type, or model of gate target.");
        options.addOption("v", false, "version information.");
        
        CustomerGate gate;
        String ip = "10.0.0.127";
        int timeout = 0;
        SupportedGateType gateType = SupportedGateType._3M_9100_;
        try
        {
            // parse the command line.
            CommandLineParser parser = new BasicParser();
            CommandLine cmd;
            cmd = parser.parse(options, args);
            if (cmd.hasOption("v"))
            {
                System.err.println("Patroncount version: " + Patroncount.VERSION);
                return;
            }
            if (cmd.hasOption("d")) // Turn on debug
            {
                DEBUG = true;
            }
            if (cmd.hasOption("i")) // gate IP
            {
                GateIPv4 gateIp = new GateIPv4(cmd.getOptionValue("i"));
                if (gateIp.isValid() == false)
                {
                    System.err.println("**error: the IP used with '-i' is invalid.");
                    Patroncount.displayHelp(1);
                }
                ip = cmd.getOptionValue("i");
            }
            else
            {
                System.err.println("**error: patroncount requires a valid IP specified"
                        + " with the '-i' flag to do anything useful.");
                Patroncount.displayHelp(1);
            }
            // Gate type specification.
            if (cmd.hasOption("t")) // location of the pidFile, default is current directory (relative to jar location).
            {
                switch (cmd.getOptionValue("t").toUpperCase())
                {
                    case "3M": // Add more gate types here, and extend code in CustomerGate.
                        gateType = SupportedGateType._3M_9100_;
                        break;
                    case "FEIGX1":
                    case "FEIG":
                        gateType = SupportedGateType._FEIG_ID_ISC_LR2500_B_;
                        break;
                    case "FEIGX2":
                        gateType = SupportedGateType._FEIG_ID_ISC_LR2500_B_DUAL_AISLE_;
                        break;
                    case "UNDEFINED":
                    case "UNKNOWN":
                    case "OFFLINE":
                        gateType = SupportedGateType._DUMMY_;
                        break;
                    default:
                        System.err.println("**error: "
                                + "invalid RFID gate type selected. Refrer to "
                                + "documentation for supported RFID gate types.");
                        Patroncount.displayHelp(1);
                }
            }
            if (cmd.hasOption("h") || cmd.hasOption("x"))
            {
                Patroncount.displayHelp(0);
            }
            if (cmd.hasOption("s"))
            {
                timeout = Integer.parseInt(cmd.getOptionValue("s"));
            }
        } 
        catch (ParseException | UnsupportedOperationException ex)
        {
            Logger.getLogger(Patroncount.class.getName()).log(Level.SEVERE, null, ex);
        }
        gate = CustomerGate.getInstance(gateType, ip, DEBUG);
        if (timeout > 0)
        {
            gate.setTimeout(timeout);
        }
        System.out.println(gate.queryGate());
    }
    
}
