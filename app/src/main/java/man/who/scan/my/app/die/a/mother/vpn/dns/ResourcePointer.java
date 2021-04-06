package man.who.scan.my.app.die.a.mother.vpn.dns;

import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class ResourcePointer {
    static final short offset_Domain = 0;
    static final short offset_Type = 2;
    static final short offset_Class = 4;
    static final int offset_TTL = 6;
    static final short offset_DataLength = 10;
    static final int offset_IP = 12;

    byte[] Data;
    int Offset;

    public ResourcePointer(byte[] data, int offset) {
        this.Data = data;
        this.Offset = offset;
    }

    public void setDomain(short value) {
        CommonUtil.writeShort(Data, Offset + offset_Domain, value);
    }

    public short getType() {
        return CommonUtil.readShort(Data, Offset + offset_Type);
    }

    public void setType(short value) {
        CommonUtil.writeShort(Data, Offset + offset_Type, value);
    }

    public short getClass(short value) {
        return CommonUtil.readShort(Data, Offset + offset_Class);
    }

    public void setClass(short value) {
        CommonUtil.writeShort(Data, Offset + offset_Class, value);
    }

    public int getTTL() {
        return CommonUtil.readInt(Data, Offset + offset_TTL);
    }

    public void setTTL(int value) {
        CommonUtil.writeInt(Data, Offset + offset_TTL, value);
    }

    public short getDataLength() {
        return CommonUtil.readShort(Data, Offset + offset_DataLength);
    }

    public void setDataLength(short value) {
        CommonUtil.writeShort(Data, Offset + offset_DataLength, value);
    }

    public int getIP() {
        return CommonUtil.readInt(Data, Offset + offset_IP);
    }

    public void setIP(int value) {
        CommonUtil.writeInt(Data, Offset + offset_IP, value);
    }
}
