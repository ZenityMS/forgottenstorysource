/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.odinms.tools;

/**
 * Provides a suite of tools for manipulating Korean Timestamps.
 * 
 * @author Frz
 * @since Revision 746
 * @version 1.0
 */
public class KoreanDateUtil {
        private final static int ITEM_YEAR2000 = -1085019342;
	        private final static long REAL_YEAR2000 = 946681229830l;
 	//      private final static long FT_UT_OFFSET2 = 116444484000000000L; // PDT
 	        private final static long FT_UT_OFFSET2 = 116444448000000000L; // PST
 	        private final static long FT_UT_OFFSET = 116444736000000000L; // 100 nsseconds from 1/1/1601 -> 1/1/1970
 	
 	        /**
 	         * Dummy constructor for static classes.
	         */
 	        private KoreanDateUtil() {
	        }
 	
 	        /**
	         * Converts a Unix Timestamp into File Time
 	         *
	         * @param realTimestamp The actual timestamp in milliseconds.
	         * @return A 64-bit long giving a filetime timestamp
 	         */
	        public static long getTempBanTimestamp(long realTimestamp) {
 	                // long time = (realTimestamp / 1000);//seconds
 	                return ((realTimestamp * 10000) + FT_UT_OFFSET);
	        }
	
	        /**
	         * Gets a timestamp for item expiration.
 	         *
 	         * @param realTimestamp The actual timestamp in milliseconds.
 	         * @return The Korean timestamp for the real timestamp.
 	         */
 	        public static int getItemTimestamp(long realTimestamp) {
	                int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert to minutes
 	                return (int) (time * 35.762787) + ITEM_YEAR2000;
	        }
	
 	        /**
 	         * Gets a timestamp for quest repetition.
 	         *
	         * @param realTimestamp The actual timestamp in milliseconds.
 	         * @return The timestamp
	         */
	
	        public static long getQuestTimestamp(long realTimestamp) {
	                long time = (realTimestamp / 1000); // convert to seconds
 	                return ((time * 10000000) + FT_UT_OFFSET2);
 	        }
 	}