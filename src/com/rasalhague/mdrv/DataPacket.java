package com.rasalhague.mdrv;

import com.rasalhague.mdrv.device.core.DeviceInfo;

import java.util.ArrayList;
import java.util.Date;

/**
 * DataPacket means one pass for each channel. Class used for gson serializing.
 */
public class DataPacket
{
    private final String          rawDataPacket;
    private final ArrayList<Byte> dataPacketValues;
    private final long            packetCreationTimeMs;
    private       int             pointsAmount;
    private       boolean         isAnalyzable;
    private final DeviceInfo      deviceInfo;

    public ArrayList<Byte> getDataPacketValues()
    {
        return dataPacketValues;
    }

    public DeviceInfo getDeviceInfo()
    {
        return deviceInfo;
    }

    public long getPacketCreationTimeMs()
    {
        return packetCreationTimeMs;
    }

    public boolean isAnalyzable()
    {
        return isAnalyzable;
    }

    public DataPacket(ArrayList<Byte> rawData, DeviceInfo deviceInfo)
    {
        this.rawDataPacket = rawData.toString();
        this.deviceInfo = deviceInfo;
        //TODO new Date()
        this.packetCreationTimeMs = new Date().getTime();
        this.dataPacketValues = deviceInfo.getDevice().parse(rawData);
        if (dataPacketValues != null)
        {
            this.pointsAmount = dataPacketValues.size();
            isAnalyzable = true;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataPacket that = (DataPacket) o;

        if (isAnalyzable != that.isAnalyzable) return false;
        if (packetCreationTimeMs != that.packetCreationTimeMs) return false;
        if (pointsAmount != that.pointsAmount) return false;
        if (!dataPacketValues.equals(that.dataPacketValues)) return false;
        if (!deviceInfo.equals(that.deviceInfo)) return false;
        if (!rawDataPacket.equals(that.rawDataPacket)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = rawDataPacket.hashCode();
        result = 31 * result + dataPacketValues.hashCode();
        result = 31 * result + (int) (packetCreationTimeMs ^ (packetCreationTimeMs >>> 32));
        result = 31 * result + pointsAmount;
        result = 31 * result + (isAnalyzable ? 1 : 0);
        result = 31 * result + deviceInfo.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "DataPacket{" +
                "rawDataPacket='" + rawDataPacket + '\'' +
                ", dataPacketValues=" + dataPacketValues +
                ", deviceInfo=" + deviceInfo +
                ", packetCreationTimeMs=" + packetCreationTimeMs +
                ", pointsAmount=" + pointsAmount +
                '}';
    }
}
