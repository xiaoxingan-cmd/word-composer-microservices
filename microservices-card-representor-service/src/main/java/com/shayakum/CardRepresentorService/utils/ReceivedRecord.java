package com.shayakum.CardRepresentorService.utils;

import lombok.Getter;

@Getter
public class ReceivedRecord {
    private String value;
    private int partition;
    private long offset;

    public ReceivedRecord(String value, int partition, long offset) {
        this.value = value;
        this.partition = partition;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "[Value = " + value + "], " + "[Partition = " + partition + "], " + "[Offset = " + offset + "]";
    }
}
