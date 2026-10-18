package com.cinema.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String seatID;

    @Column(nullable = false, length = 20)
    private String seatCode; // VD: A01, B05, E06

    @Column(nullable = false, length = 5)
    private String seatRow; // VD: A, B, C...

    @Column(nullable = false)
    private int seatCol; // VD: 1, 2, 3...

    @Column(nullable = false, length = 30)
    private String seatType = "STANDARD"; // STANDARD, VIP, COUPLE

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, MAINTENANCE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Seat() {
    }

    public Seat(String seatCode, String seatRow, int seatCol, String seatType, String status, Room room) {
        this.seatCode = seatCode;
        this.seatRow = seatRow;
        this.seatCol = seatCol;
        this.seatType = seatType;
        this.status = status;
        this.room = room;
    }

    public String getSeatID() {
        return seatID;
    }

    public void setSeatID(String seatID) {
        this.seatID = seatID;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public int getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(int seatCol) {
        this.seatCol = seatCol;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
