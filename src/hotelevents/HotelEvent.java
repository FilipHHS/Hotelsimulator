package hotelevents;

public class HotelEvent {
    private int guestId;
    private HotelEventType type;
    private int time;
    private int data;

    public HotelEvent(int guestId, HotelEventType type, int time, int data) {
        this.guestId = guestId;
        this.type = type;
        this.time = time;
        this.data = data;
    }

    public int getGuestId() {
        return guestId;
    }

    public HotelEventType getEventType() {
        return type;
    }

    public HotelEventType getType() {
        return type;
    }

    public int getTime() {
        return time;
    }

    public int getData() {
        return data;
    }
}

