package jme3tools.converters.model;

import com.jme3.scene.Geometry;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import jme3tools.converters.model.strip.PrimitiveGroup;
import jme3tools.converters.model.strip.TriStrip;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Comparator;

public class ModelConverter {

    private static final class PrimComparator
            implements Comparator<PrimitiveGroup> {

        public int compare(PrimitiveGroup g1, PrimitiveGroup g2) {
            if (g1.type < g2.type)
                return -1;
            else if (g1.type > g2.type)
                return 1;
            else
                return 0;
        }
    }

    private static final PrimComparator primComp = new PrimComparator();

    public static void generateStrips(Mesh mesh, boolean stitch, boolean listOnly, int cacheSize, int minStripSize){
        TriStrip ts = new TriStrip();
        ts.setStitchStrips(stitch);
        ts.setCacheSize(cacheSize);
        ts.setListsOnly(listOnly);
        ts.setMinStripSize(minStripSize);

        IndexBuffer ib = mesh.getIndexBuffer();
        int[] indices = new int[ib.size()];
        for (int i = 0; i < indices.length; i++)
            indices[i] = ib.get(i);

        PrimitiveGroup[] groups = ts.generateStrips(indices);
        Arrays.sort(groups, primComp);

        int numElements = 0;
        for (PrimitiveGroup group : groups)
            numElements += group.numIndices;

        VertexBuffer original = mesh.getBuffer(Type.Index);
        Buffer buf = VertexBuffer.createBuffer(original.getFormat(),
                                               original.getNumComponents(),
                                               numElements);
        original.updateData(buf);
        ib = mesh.getIndexBuffer();

        mesh.setMode(Mode.Hybrid);

        int curIndex = 0;
        int[] modeStart = new int[]{ -1, -1, -1 };
        int[] elementLengths = new int[groups.length];
        for (int i = 0; i < groups.length; i++){
            PrimitiveGroup group = groups[i];
            elementLengths[i] = group.numIndices;

            if (modeStart[group.type] == -1){
                modeStart[group.type] = i;
            }

            int[] trimmedIndices = group.getTrimmedIndices();
            for (int j = 0; j < trimmedIndices.length; j++){
                ib.put(curIndex + j, trimmedIndices[j]);
            }

            curIndex += group.numIndices;
        }

        mesh.setElementLengths(elementLengths);
        mesh.setModeStart(modeStart);
    }

    public static void optimize(Mesh mesh){
        // update any data that need updating
        mesh.updateBound();
        mesh.updateCounts();

        // set all buffers into STATIC_DRAW mode
        mesh.setStatic();

        if (mesh.getBuffer(Type.Index) != null){
            // compress index buffer from UShort to UByte (if possible)
            FloatToFixed.compressIndexBuffer(mesh);

            // generate triangle strips stitched with degenerate tris
            generateStrips(mesh, false, false, 16, 0);
        }

        IntMap<VertexBuffer> bufs = mesh.getBuffers();
        for (Entry<VertexBuffer> entry : bufs){
            VertexBuffer vb = entry.getValue();
            if (vb == null || vb.getBufferType() == Type.Index)
                continue;

             if (vb.getFormat() == Format.Float){
                if (vb.getBufferType() == Type.Color){
                    // convert the color buffer to UByte
                    vb = FloatToFixed.convertToUByte(vb);
                }else{
                    // convert normals, positions, and texcoords
                    // to fixed-point (16.16)
                    vb = FloatToFixed.convertToFixed(vb);
//                    vb = FloatToFixed.convertToFloat(vb);
                }
                mesh.clearBuffer(vb.getBufferType());
                mesh.setBuffer(vb);
            }
        }
        mesh.setInterleaved();
    }

    private static void optimizeScene(Spatial source){
        if (source instanceof Geometry){
            Geometry geom = (Geometry) source;
            Mesh mesh = geom.getMesh();
            optimize(mesh);
        }else if (source instanceof Node){
            Node node = (Node) source;
            for (int i = node.getQuantity() - 1; i >= 0; i--){
                Spatial child = node.getChild(i);
                optimizeScene(child);
            }
        }
    }

    public static void optimize(Spatial source){
        optimizeScene(source);
        source.updateLogicalState(0);
        source.updateGeometricState();
    }

}
