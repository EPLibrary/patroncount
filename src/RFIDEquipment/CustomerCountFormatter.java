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

/**
 * Each gate reports using different responses and each response needs to be
 * formatted into a standard format that the database at EPL can consume. 
 * These classes do that job.
 * 
 * @author Andrew Nisbet <andrew.nisbet@epl.ca>
 * @version 1.0
 * @since   2018-10-22
 */
public abstract class CustomerCountFormatter implements ResultsFormatter
{
    private static ResultsFormatter instance;
    private static boolean DEBUG;
    
    public static ResultsFormatter getInstance(SupportedGateType gate, boolean debug)
    {
        DEBUG = debug;
        
        switch (gate)
        {
            case _3M_9100_:
                instance = new ThreeMCustomerCountFormatter();
                break;
            case _FEIG_ID_ISC_LR2500_B_:
                instance = new FeigCustomerCountFormatter();
                break;
            case _FEIG_ID_ISC_LR2500_B_DUAL_AISLE:
                instance = new FeigCustomerCountDualAisleFormatter();
                break;
            default:
                throw new UnsupportedOperationException("***error, unsupported "
                        + "output formatter type requiested.");
        }
        return instance;
    }
    
    /**
     * Instance of a 3M 9100 gate formatter.
     */
    private static class ThreeMCustomerCountFormatter extends CustomerCountFormatter
    {
        @Override
        public String format(String message)
        {
            String result = "-1|-1|";
            // If there is another application connected to the port, you won't get
            // any data, because someone else is hogging the connection so test if 
            // you get any data back.
            if (message.length() == 0)
            {
                return result;
            }
            else
            {
                // Return the count.
                if (DEBUG)
                {
                    System.out.println("count data recv'd:" + message);
                }
                long outCount = Long.parseLong(message.substring(11, 18), 16);
                long inCount = Long.parseLong(message.substring(19, 26), 16);
                // or more like patroncount: 'in|out|'
                result = String.valueOf(inCount) + "|" + String.valueOf(outCount) + "|";
            }
            return result;
        }
    }
    
    /**
     * Instance of a FEIG single-aisle gate formatter.
     */
    private static class FeigCustomerCountFormatter extends CustomerCountFormatter
    {
        @Override
        public String format(String message)
        {
            String result = "-1|-1|";
            // If there is another application connected to the port, you won't get
            // any data, because someone else is hogging the connection so test if 
            // you get any data back.
            if (message.length() == 0)
            {
                return result;
            }
            else
            {
                // Display the count.
                if (DEBUG)
                {
                    System.out.println("count data recv'd:" + message);
                }
                long inCount = Long.parseLong(message.substring(24, 32), 16);
                long outCount = Long.parseLong(message.substring(32, 40), 16);
                // or more like patroncount: 'in|out|'
                result = String.valueOf(inCount) + "|" + String.valueOf(outCount) + "|";
            }
            return result;
        }
    }
    
    /**
     * Instance of a FEIG dual-aisle gate formatter. Gates of these types 
     * report from 2 detectors, and add the counts together to produce a total
     * in-count and total out-count.
     * See {@linkplain http://ilswiki.epl.ca/index.php/Bibliotheca_gate_hacking#People_Counter_LON_gates}
     * for more details on how this gate reports values.
     */
    private static class FeigCustomerCountDualAisleFormatter extends CustomerCountFormatter
    {
        @Override
        public String format(String message)
        {
            String result = "-1|-1|";
            // If there is another application connected to the port, you won't get
            // any data, because someone else is hogging the connection so test if 
            // you get any data back.
            if (message.length() == 0)
            {
                return result;
            }
            else
            {
                // Display the count.
                if (DEBUG)
                {
                    System.out.println("count data recv'd:" + message);
                }
                // 02 00 20 00 9F 00 02 00 18 01 77 00 [00 00 0D 13] [00 00 14 B0] [00 00 11 8A] [00 00 11 E0] EA EB 81 A3 OK
                //
                //Radar Detector 1 Counter 1
                //Radar Detector 1 Counter 2
                //Radar Detector 2 Counter 1
                //Radar Detector 2 Counter 2
                long inCountOne = Long.parseLong(message.substring(24, 32), 16);
                long outCountOne = Long.parseLong(message.substring(32, 40), 16);
                // Second aisle
                long inCountTwo = Long.parseLong(message.substring(40, 48), 16);
                long outCountTwo = Long.parseLong(message.substring(48, 56), 16);
                // The count on the display is the sum of inCountOne and inCountTwo
                // the out count on the display is sum of outCountOne and outCountTwo.
                long inCount = inCountOne + inCountTwo;
                long outCount = outCountOne + outCountTwo;
                // or more like patroncount: 'in|out|'
                result = String.valueOf(inCount) + "|" + String.valueOf(outCount) + "|";
            }
            return result;
        }
    }
}
