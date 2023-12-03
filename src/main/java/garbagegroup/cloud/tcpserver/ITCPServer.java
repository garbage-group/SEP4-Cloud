package garbagegroup.cloud.tcpserver;

import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.service.serviceImplementation.BinService;

import java.util.List;

public interface ITCPServer {

    public String setFillThreshold(double newThreshold);
    public String getDataById(int deviceId, String payload);
    void startServer();
    public List<ServerSocketHandler> getIoTDevices();
    void addDeviceStatusListener(DeviceStatusListener listener);
//    void removeDeviceStatusListener(DeviceStatusListener listener);

}
