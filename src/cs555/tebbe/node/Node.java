package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.wireformats.*;
public interface Node {
    public void onEvent(Event event);
    public void registerConnection(NodeConnection connection);
}
