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
 * Formats results from all gate types to a standard output.
 * @author Andrew Nisbet <andrew.nisbet@epl.ca>
 * @version 1.0
 * @since   2018-10-22
 */
public interface ResultsFormatter
{
    /**
     * Formats the response of a customer gate into a useful message to be 
     * consumed by other services. 
     * 
     * For example, in the case of patron count, the message is converted into 
     * '{n}|{m}|' message, where both 'n' and 'm' are sum of 
     * integer counts of all receivers (detectors) within the gate.
     * 
     * @param message String of bytes received from the gate.
     * @return the result of the message sent.
     */
    public String format(String message);
}
