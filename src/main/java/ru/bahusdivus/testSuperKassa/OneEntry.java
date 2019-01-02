package ru.bahusdivus.testSuperKassa;

class OneEntry {
    private byte mask;
    private String[] values;

    OneEntry(byte mask, String[] values) {
        this.mask = mask;
        this.values = values;
    }

    public byte getMask() {
        return mask;
    }
    public String[] getValues() {
        return values;
    }
}
