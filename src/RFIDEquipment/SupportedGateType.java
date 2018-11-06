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
 * The canonical list of makes and models of gates this application supports.
 * Creating an entry here is the first step to adding support for 
 * a new customer gate.
 * 
 * @author Andrew Nisbet <andrew.nisbet@epl.ca>
 * @version 1.0
 * @since   2018-10-22
 */
public enum SupportedGateType
{
    _3M_9100_("3M 9100"),
    _FEIG_ID_ISC_LR2500_B_("FEIG LR2500"),
    _FEIG_ID_ISC_LR2500_B_DUAL_AISLE("FEIG LR2500 DUAL AISLE");
    
    private String type;

    private SupportedGateType(String s)
    {
        this.type = s;
    }

    @Override
    public String toString()
    {
        return this.type;
    } 
}
