package com.cinema.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Một hàng của Timeline (một phòng chiếu) gồm các khối suất chiếu trong ngày.
 */
public class TimelineRowDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private RoomDTO room;
    private List<TimelineBlockDTO> blocks = new ArrayList<>();

    public TimelineRowDTO() {
    }

    public RoomDTO getRoom() {
        return room;
    }

    public void setRoom(RoomDTO room) {
        this.room = room;
    }

    public List<TimelineBlockDTO> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<TimelineBlockDTO> blocks) {
        this.blocks = blocks;
    }
}
