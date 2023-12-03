package garbagegroup.cloud.tcpserver;

public interface DeviceStatusListener {
    void onDeviceConnected(int deviceId);
    void onDeviceDisconnected(int deviceId);
}
