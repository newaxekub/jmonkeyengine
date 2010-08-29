package com.jme3.font;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BitmapText extends Geometry {

    private BitmapFont font;
    private StringBlock block;
    private QuadList quadList = new QuadList();
    private float lineWidth = 0f;
    private boolean rightToLeft = false;
    private boolean needRefresh = true;

    private final float[] pos;
    private final float[] tc;
    private final short[] idx;
    private final byte[] color;

    public BitmapText(BitmapFont font, boolean rightToLeft, boolean arrayBased){
        super("BitmapFont", new Mesh());

        if (font == null)
            throw new NullPointerException("'font' cannot be null.");

        setQueueBucket(Bucket.Gui);
        setCullHint(CullHint.Never);

        this.rightToLeft = rightToLeft;
        this.font = font;
        this.block = new StringBlock();
        block.setSize(font.getPreferredSize());

        Material mat = font.getPage(0);
        if (mat == null)
            throw new IllegalStateException("The font's texture was not found!");

        setMaterial(mat);

        // initialize buffers
        Mesh m = getMesh();
        m.setBuffer(Type.Position, 3, new float[0]);
        m.setBuffer(Type.TexCoord, 2, new float[0]);
        m.setBuffer(Type.Color, 4, new byte[0]);
        m.setBuffer(Type.Index, 3, new short[0]);
        
        // scale colors from 0 - 255 range into 0 - 1
        m.getBuffer(Type.Color).setNormalized(true);

        arrayBased = true;

        if (arrayBased){
            pos   = new float[4 * 3];  // 4 verticies * 3 floats
            tc    = new float[4 * 2];  // 4 verticies * 2 floats
            idx   = new short[2 * 3];  // 2 triangles * 3 indices
            color = new byte[4 * 4];   // 4 verticies * 4 bytes
        }else{
            pos = null; tc = null; idx = null; color = null;
        }
    }

    public BitmapText(BitmapFont font, boolean rightToLeft){
        this(font, rightToLeft, false);
    }

    public BitmapText(BitmapFont font){
        this(font, false, false);
    }

    @Override
    public BitmapText clone(){
        BitmapText clone = (BitmapText) super.clone();
        clone.mesh = mesh.deepClone();
        clone.needRefresh = true;
        clone.quadList = new QuadList();
        clone.block = block.clone();
        return clone;
    }

    public BitmapFont getFont() {
        return font;
    }

    public void setSize(float size) {
        block.setSize(size);
        needRefresh = true;
    }

    public void setText(CharSequence text){
        if (block.getText().equals(text))
            return;
        
        block.setText(text);
        needRefresh = true;
    }

    public String getText(){
        return block.getText();
    }

    public ColorRGBA getColor() {
        return block.getColor();
    }

    public void setColor(ColorRGBA color) {
        if (block.getColor().equals(color))
            return;

        block.setColor(color);
        needRefresh = true;
    }

    public void setBox(Rectangle rect){
        block.setTextBox(rect);
        needRefresh = true;
    }

    public float getLineHeight(){
        return font.getLineHeight(block);
    }

    public float getLineWidth(){
        return lineWidth;
    }

    @Override
    public void updateLogicalState(float tpf){
        super.updateLogicalState(tpf);
        if (needRefresh)
            assemble();
    }

    private void assemble(){
        // first generate quadlist
        if (block.getTextBox() == null){
            lineWidth = font.updateText(block, quadList, rightToLeft);
        }else{
            font.updateTextRect(block, quadList);
        }

        Mesh m = getMesh();
        m.setVertexCount(quadList.getQuantity() * 4);
        m.setTriangleCount(quadList.getQuantity() * 2);

        VertexBuffer pb = m.getBuffer(Type.Position);
        VertexBuffer tb = m.getBuffer(Type.TexCoord);
        VertexBuffer ib = m.getBuffer(Type.Index);
        VertexBuffer cb = m.getBuffer(Type.Color);

        FloatBuffer fpb = (FloatBuffer) pb.getData();
        FloatBuffer ftb = (FloatBuffer) tb.getData();
        ShortBuffer sib = (ShortBuffer) ib.getData();
        ByteBuffer bcb  = (ByteBuffer)  cb.getData();

        // increase capacity of buffers as needed
        fpb.rewind();
        fpb = BufferUtils.ensureLargeEnough(fpb, m.getVertexCount() * 3);
        pb.updateData(fpb);

        ftb.rewind();
        ftb = BufferUtils.ensureLargeEnough(ftb, m.getVertexCount() * 2);
        tb.updateData(ftb);

        bcb.rewind();
        bcb = BufferUtils.ensureLargeEnough(bcb, m.getVertexCount() * 4);
        cb.updateData(bcb);

        sib.rewind();
        sib = BufferUtils.ensureLargeEnough(sib, m.getTriangleCount() * 3);
        ib.updateData(sib);

        // go for each quad and append it to the buffers
        if (pos != null){
            for (int i = 0; i < quadList.getQuantity(); i++){
                FontQuad fq = quadList.getQuad(i);
                fq.storeToArrays(pos, tc, idx, color, i);
                fpb.put(pos);
                ftb.put(tc);
                sib.put(idx);
                bcb.put(color);
            }
        }else{
            for (int i = 0; i < quadList.getQuantity(); i++){
                FontQuad fq = quadList.getQuad(i);
                fq.appendPositions(fpb);
                fq.appendTexCoords(ftb);
                fq.appendIndices(sib, i);
                fq.appendColors(bcb);
            }
        }
        
        fpb.rewind();
        ftb.rewind();
        sib.rewind();
        bcb.rewind();
    }

}
