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

/**
 * Exception thrown if an incorrect IPv4 address is used, or referenced.
 * @author Andrew Nisbet <andrew.nisbet@epl.ca>
 * @version 1.0
 * @since   2018-10-22
 */
public class InvalidIPEv4Exception extends Exception
{
    /**
     * Thrown if a specified v4IP is invalid as defined.
     * @param message - exception message.
     */
    public InvalidIPEv4Exception(String message)
    {
        System.err.println(message);
    }
}
