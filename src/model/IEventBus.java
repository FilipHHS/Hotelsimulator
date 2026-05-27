package model;

import hotelevents.HotelEventType;

public interface IEventBus {
    void triggerHotelEvent(HotelEventType eventType, int guestId, int data);
}