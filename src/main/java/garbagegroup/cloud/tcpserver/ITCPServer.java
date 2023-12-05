package garbagegroup.cloud.tcpserver;

import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.service.serviceImplementation.BinService;

import java.util.List;

public interface ITCPServer {

    public boolean setIoTData(int deviceId, String payload);
    public String getDataById(int deviceId, String payload);
    void startServer();
    public List<ServerSocketHandler> getIoTDevices();
}
