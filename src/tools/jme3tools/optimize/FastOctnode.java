package jme3tools.optimize;

public class FastOctnode {
    int offset;
    int length;
    FastOctnode child;
    FastOctnode next;

    public int getSide(){
        return ((offset & 0xE0000000) >> 29) & 0x7;
    }

    public void setSide(int side){
        offset &= 0x1FFFFFFF;
        offset |= (side << 29);
    }

    public void setOffset(int offset){
        if (offset < 0 || offset > 20000000){
            throw new IllegalArgumentException();
        }

        this.offset &= 0xE0000000;
        this.offset |= offset;
    }

    public int getOffset(){
        return this.offset & 0x1FFFFFFF;
    }

}
