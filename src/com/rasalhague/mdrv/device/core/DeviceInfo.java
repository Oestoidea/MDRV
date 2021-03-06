package com.rasalhague.mdrv.device.core;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * productID Must be Hex value exclude 0x. Example: 0241 vendorID the same as productID
 */
public class DeviceInfo
{
    private String     friendlyName;
    private String     vendorID;
    private String     productID;
    private String     name;
    private String     portName;
    private DeviceType deviceType;
    private byte[]     endPacketSequence;
    private float      initialFrequency;
    private float      channelSpacing;
    private int     id                  = 0;
    private boolean manualDeviceControl = false;

    //transient for avoid logger Deadlock with Device class
    private transient Device device;

    public DeviceInfo(HIDDeviceInfo hidDeviceInfo)
    {
        portName = hidDeviceInfo.getPath();
        deviceType = DeviceType.HID;

        name = hidDeviceInfo.getProduct_string();
        productID = Utils.normalizePidVidToLength(Integer.toString(hidDeviceInfo.getProduct_id(), 16).toUpperCase());
        vendorID = Utils.normalizePidVidToLength(Integer.toString(hidDeviceInfo.getVendor_id(), 16).toUpperCase());
    }

    public DeviceInfo(String devPortName)
    {
        portName = devPortName;
        deviceType = DeviceType.COM;

        HashMap<String, String> devInfMap = takeCOMDeviceInformation(portName);
        name = devInfMap.get("devName");
        productID = devInfMap.get("pid").toUpperCase();
        vendorID = devInfMap.get("vid").toUpperCase();
    }

    public DeviceInfo(String vendorID,
                      String productID,
                      String name,
                      String portName,
                      DeviceType deviceType,
                      byte[] endPacketSequence,
                      float initialFrequency,
                      float channelSpacing)
    {
        this.vendorID = vendorID;
        this.productID = productID;
        this.name = name;
        this.portName = portName;
        this.deviceType = deviceType;
        this.endPacketSequence = endPacketSequence;
        this.initialFrequency = initialFrequency;
        this.channelSpacing = channelSpacing;
    }

    public boolean isManualDeviceControl()
    {
        return manualDeviceControl;
    }

    public void setManualDeviceControl(boolean manualDeviceControl)
    {
        this.manualDeviceControl = manualDeviceControl;
    }

    public void setSomeFields(String friendlyName,
                              byte[] endPacketSequence,
                              float initialFrequency,
                              float channelSpacing,
                              Device device)
    {
        this.friendlyName = friendlyName;
        this.endPacketSequence = endPacketSequence;
        this.initialFrequency = initialFrequency;
        this.channelSpacing = channelSpacing;
        this.device = device;
    }

    public String getVendorID()
    {
        return vendorID;
    }

    public String getProductID()
    {
        return productID;
    }

    public String getName()
    {
        return name;
    }

    public String getPortName()
    {
        return portName;
    }

    public DeviceType getDeviceType()
    {
        return deviceType;
    }

    public byte[] getEndPacketSequence()
    {
        return endPacketSequence;
    }

    public float getInitialFrequency()
    {
        return initialFrequency;
    }

    public float getChannelSpacing()
    {
        return channelSpacing;
    }

    public void setChannelSpacing(float channelSpacing)
    {
        this.channelSpacing = channelSpacing;
    }

    public String getFriendlyName()
    {
        return friendlyName;
    }

    public String getFriendlyNameWithId()
    {
        if (this.id == 0)
        {
            return friendlyName;
        }
        else
        {
            return friendlyName + " #" + this.id;
        }
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Device getDevice()
    {
        return device;
    }

    public boolean equalsPidVid(String pId, String vId)
    {
        return pId.equals(productID) && vId.equals(vendorID);
    }

    @Override
    public int hashCode()
    {
        int result = vendorID.hashCode();
        result = 31 * result + productID.hashCode();
        result = 31 * result + portName.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (id != that.id) return false;
        if (!portName.equals(that.portName)) return false;
        if (!productID.equals(that.productID)) return false;
        if (!vendorID.equals(that.vendorID)) return false;

        return true;
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" +
                "friendlyName='" + friendlyName + '\'' +
                ", vendorID='" + vendorID + '\'' +
                ", productID='" + productID + '\'' +
                ", name='" + name + '\'' +
                ", portName='" + portName + '\'' +
                ", deviceType=" + deviceType +
                ", endPacketSequence=" + Arrays.toString(endPacketSequence) +
                ", initialFrequency=" + initialFrequency +
                ", channelSpacing=" + channelSpacing +
                '}';
    }

    private HashMap<String, String> takeCOMDeviceInformation(String devicePortName)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            return Utils.searchRegistry("HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB", devicePortName);
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            String output = "";

            ArrayList<String> strings = Utils.runShellScript("dmesg | grep -i usb");
            for (String string : strings)
            {
                output += string + "\n";
            }

            Pattern pattern = Pattern.compile(
                    "New USB device found.*?idVendor=(?<vid>.{4}), idProduct=(?<pid>.{4}).*?\\n.*?\\n.*?Product: (?<devName>.*)((\\n.*?){0,6}(?<portName>tty.*):)?");

            Matcher matcher = pattern.matcher(output);
            HashMap<String, String> devInfMap = new HashMap<>();
            while (matcher.find())
            {
                String portName = matcher.group("portName");
                if (portName != null && devicePortName.contains(portName))
                {
                    devInfMap.put("vid", matcher.group("vid").trim());
                    devInfMap.put("pid", matcher.group("pid").trim());
                    devInfMap.put("devName", matcher.group("devName").trim());
                }
            }

            return devInfMap;
        }

        ApplicationLogger.LOGGER.severe("OS does not support");

        return null;
    }

    public enum DeviceType
    {
        HID,
        COM,
        DUMMY
    }
}
