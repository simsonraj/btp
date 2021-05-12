package foundation.icon.btp.lib.event;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;

public class EventRecord {
    private byte phaseEnum;
    private byte[] phaseData;
    private byte[] eventIndex;
    private byte[] eventData;
    private List<byte[]> topics;

    public EventRecord(Map<String, Object> rawData) {
        this.phaseEnum = ((BigInteger) rawData.get("a")).toByteArray()[0];
        this.phaseData = (byte[]) rawData.get("b");
        this.eventIndex = (byte[]) rawData.get("c");
        this.eventData = (byte[]) rawData.get("d");
        this.topics = (List<byte[]>) rawData.get("e");
    }

    public byte getPhaseEnum() {
        return this.phaseEnum;
    }

    public byte[] getPhaseData() {
        return this.phaseData;
    }

    public byte[] getEventIndex() {
        return this.eventIndex;
    }

    public byte[] getEventData() {
        return this.eventData;
    }

    public List<byte[]> getTopics() {
        return this.topics;
    }
}