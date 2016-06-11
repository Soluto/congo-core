package soluto.congo.core;

public class RemoteCall {
    public String service = "";
    public String method = "";
    public Object[] args = new Object[] {};
    public String correlationId = "";
    public boolean isCancelled = false;
}
