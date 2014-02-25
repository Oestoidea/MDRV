package com.rasalhague.mdrv;

import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.HashMap;

class DeviceInfo
{
    private final String         deviceVid;
    private final String         devicePid;
    public        String         deviceName;
    public        String         devicePortName;
    public        DeviceTypeEnum deviceType;

    public static ArrayList<DeviceInfo> createArrayListFromNames(String[] portNames, DeviceTypeEnum deviceTypeEnum)
    {
        ArrayList<DeviceInfo> deviceInfoList = new ArrayList<DeviceInfo>();

        for (String portName : portNames)
        {
            deviceInfoList.add(new DeviceInfo(portName, deviceTypeEnum));
        }

        return deviceInfoList;
    }

    enum DeviceTypeEnum
    {
        USB, COM
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" +
                "devicePortName='" + devicePortName + '\'' +
                ", deviceType=" + deviceType +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (devicePortName != null ? !devicePortName.equals(that.devicePortName) : that.devicePortName != null)
        {
            return false;
        }
        if (deviceType != that.deviceType) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = devicePortName != null ? devicePortName.hashCode() : 0;
        result = 31 * result + (deviceType != null ? deviceType.hashCode() : 0);
        return result;
    }

    DeviceInfo(String devPortName, DeviceTypeEnum devTypeEnum)
    {
        devicePortName = devPortName;
        deviceType = devTypeEnum;

        HashMap<String, String> devInfMap = getDeviceName();
        deviceName = devInfMap.get("devName");
        devicePid = devInfMap.get("vid");
        deviceVid = devInfMap.get("pid");
    }

    private HashMap<String, String> getDeviceName()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            HashMap<String, String> map = Utils.getDeviceNameFromWinRegistry(devicePortName);

            return map;
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            //TODO IS_OS_LINUX get device names impl
        }

        ApplicationLogger.LOGGER.severe("OS do not support");

        return null;
    }
}
