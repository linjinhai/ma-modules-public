/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.m2m2.mbus.dwr;

import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;

/**
 *
 * @author aploese
 */
public class MBusDeviceBean{
    private final int index;
    private final MBusResponseFramesContainer dev;

    public static boolean compare(MBusResponseFramesContainer dev, String address, String id, String man, String medium,
            String version) {
        boolean result = address.equals(String.format("0x%02X", dev.getAddress()));
        result &= id.equals(String.format("%08d", dev.getIdentNumber()));
        result &= man.equals(dev.getManufacturer());
        result &= medium.equals(dev.getMedium().name());
        result &= version.equals(String.format("0x%02X", dev.getAddress()));
        return result;
    }

    public MBusDeviceBean(int index, MBusResponseFramesContainer dev) {
        this.index = index;
        this.dev = dev;
    }

    /**
     * @return the address
     */
    public byte getAddress() {
        return dev.getAddress();
    }

    /**
     * @return the address
     */
    public String getAddressHex() {
        return String.format("0x%02X", dev.getAddress());
    }

    /**
     * @return the id
     */
    public String getIdentNumber() {
        return String.format("%08d", dev.getIdentNumber());
    }

    /**
     * @return the man
     */
    public String getManufacturer() {
        return dev.getManufacturer();
    }

    /**
     * @return the medium
     */
    public String getMedium() {
        return dev.getMedium().name();
    }

    /**
     * @return the version
     */
    public byte getVersion() {
        return dev.getVersion();
    }

    /**
     * @return the version
     */
    public String getVersionHex() {
        return String.format("0x%02X", dev.getVersion());
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getAddress();
		result = prime * result + getIdentNumber().hashCode();
		result = prime * result + getManufacturer().hashCode();
		result = prime * result + getMedium().hashCode();
		result = prime * result + getVersion();
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object o) {
		
		if(o instanceof MBusDeviceBean){
			MBusDeviceBean other = (MBusDeviceBean)o;
			boolean same = other.getAddress() == getAddress();
	        same &= other.getIdentNumber().equals(getIdentNumber());
	        same &= other.getManufacturer().equals(getManufacturer());
	        same &= other.getMedium().equals(getMedium());
	        same &= other.getVersion() == getVersion();
	        return same;
		}else{
			return false;
		}
	}
}
