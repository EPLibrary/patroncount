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
 * This controls what types of queries gates can be asked. It does not mean
 * that a specific gate model will be able to answer this query, so the 
 * developer is expected to determine if, and how the gate could answer a query
 * and add that capability to {@link CustomerGate} nested classes. Each gate
 * object controls whether it will respond to a given type of message.
 * @see CustomerGate
 * 
 * @author Andrew Nisbet <andrew.nisbet@epl.ca>
 * @version 1.0
 * @since   2018-10-22
 */
public enum SupportedQueryType
{
    CUSTOMER_COUNTS,
    RESET_COUNTS;
}
