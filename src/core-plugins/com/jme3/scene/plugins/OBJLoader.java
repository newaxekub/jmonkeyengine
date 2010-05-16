package com.jme3.scene.plugins;

import com.jme3.asset.*;
import com.jme3.util.*;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Reads OBJ format models.
 */
public class OBJLoader implements AssetLoader {

    protected List<Vector3f> verts = new ArrayList<Vector3f>();
    protected List<Vector2f> texCoords = new ArrayList<Vector2f>();
    protected List<Vector3f> norms = new ArrayList<Vector3f>();
    protected List<Face> faces = new ArrayList<Face>();

    protected Map<Vertex, Integer> vertIndexMap = new HashMap<Vertex, Integer>();
    protected Map<Integer, Vertex> indexVertMap = new HashMap<Integer, Vertex>();
    protected int curIndex = 0;

    protected Scanner scan;

    protected static class Vertex {

        Vector3f v;
        Vector2f vt;
        Vector3f vn;
        int index;

        @Override
        public boolean equals(Object o){
            if (!(o instanceof Vertex))
                return false;

            Vertex other = (Vertex) o;
            return v == other.v && vt == other.vt && vn == other.vn;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.v != null ? this.v.hashCode() : 0);
            hash = 53 * hash + (this.vt != null ? this.vt.hashCode() : 0);
            hash = 53 * hash + (this.vn != null ? this.vn.hashCode() : 0);
            return hash;
        }
    }

    protected static class Face {
        int[] vertIndexes;
    }

    public OBJLoader(){
    }

    protected void loadVertexIndex(Vertex vert){
        Integer index = vertIndexMap.get(vert);
        if (index != null){
            vert.index = index.intValue();
            indexVertMap.put(index, vert);
        }else{
            vert.index = curIndex++;
            vertIndexMap.put(vert, vert.index);
            indexVertMap.put(vert.index, vert);
        }
    }

    protected Face readFace(){
        Face f = new Face();
        List<Vertex> vertList = new ArrayList<Vertex>();

        String line = scan.nextLine().trim();
        String[] verticies = line.split(" ");
        for (String vertex : verticies){
            int v = 0;
            int vt = 0;
            int vn = 0;

            String[] split = vertex.split("/");
            if (split.length == 1){
                v = Integer.parseInt(split[0]);
            }else if (split.length == 2){
                v = Integer.parseInt(split[0]);
                vt = Integer.parseInt(split[1]);
            }else if (split.length == 3 && !split[1].equals("")){
                v = Integer.parseInt(split[0]);
                vt = Integer.parseInt(split[1]);
                vn = Integer.parseInt(split[2]);
            }else if (split.length == 3){
                v = Integer.parseInt(split[0]);
                vn = Integer.parseInt(split[2]);
            }

            Vertex vx = new Vertex();
            vx.v = verts.get(v - 1);

            if (vt > 0)
                vx.vt = texCoords.get(vt - 1);

            if (vn > 0)
                vx.vn = norms.get(vn - 1);

            loadVertexIndex(vx);
            vertList.add(vx);
        }

        if (vertList.size() == 0)
            return null; // error

        f.vertIndexes = new int[vertList.size()];
        for (int i = 0; i < vertList.size(); i++){
            f.vertIndexes[i] = vertList.get(i).index;
        }
        return f;
    }

    protected Vector3f readVector3(){
        Vector3f v = new Vector3f();
        v.setX(scan.nextFloat());
        if (scan.hasNextFloat()){
            v.setY(scan.nextFloat());
            if (scan.hasNextFloat()){
                v.setZ(scan.nextFloat());
            }
        }
        return v;
    }

    protected Vector2f readVector2(){
        Vector2f v = new Vector2f();
        v.setX(scan.nextFloat());
        if (scan.hasNextFloat()){
            v.setY(scan.nextFloat());
        }
        return v;
    }

    protected boolean readLine(){
        if (!scan.hasNext()){
            return false;
        }

        String cmd = scan.next();
        if (cmd.startsWith("#")){
            scan.useDelimiter("\n");
            scan.next(); // skip entire comment until next line
            scan.useDelimiter("\\p{javaWhitespace}+");
        }else if (cmd.equals("v")){
            verts.add(readVector3());
        }else if (cmd.equals("vn")){
            norms.add(readVector3());
        }else if (cmd.equals("vt")){
            texCoords.add(readVector2());
        }else if (cmd.equals("f")){
            faces.add(readFace());
        }else if (cmd.equals("usemtl")){
            // TODO: Material support
            String matName = scan.next();
        }else if (cmd.equals("mtllib")){
            String mtllib = scan.next();
        }else if (cmd.equals("s")){
            boolean on = scan.next().equals("on");
        }else if (cmd.equals("o")){
            // object name
            scan.next();
        }else if (cmd.equals("g")){
            // group name
            scan.useDelimiter("\n");
            scan.next(); // will retrieve entire group list
            scan.useDelimiter("\\p{javaWhitespace}+");
        }else{
            System.out.println("Unknown statement in OBJ! "+cmd);
            scan.useDelimiter("\n");
            scan.next(); // skip entire command until next line
            scan.useDelimiter("\\p{javaWhitespace}+");
        }

        return true;
    }

    protected Mesh constructMesh(){
        Mesh m = new Mesh();

        FloatBuffer posBuf = BufferUtils.createFloatBuffer(vertIndexMap.size() * 3);
        FloatBuffer normBuf = norms.size() > 0 ? BufferUtils.createFloatBuffer(vertIndexMap.size() * 3) : null;
        FloatBuffer tcBuf = texCoords.size() > 0 ? BufferUtils.createFloatBuffer(vertIndexMap.size() * 2) : null;

        // only use Shortbuffer if faces.size() * 3 > Short.MAX_VALUE
        ShortBuffer indexBuf = BufferUtils.createShortBuffer(faces.size() * 3);

        for (Face f : faces){
            Vertex v0 = indexVertMap.get(f.vertIndexes[0]);
            Vertex v1 = indexVertMap.get(f.vertIndexes[1]);
            Vertex v2 = indexVertMap.get(f.vertIndexes[2]);

            posBuf.position(v0.index * 3);
            posBuf.put(v0.v.x).put(v0.v.y).put(v0.v.z);
            posBuf.position(v1.index * 3);
            posBuf.put(v1.v.x).put(v1.v.y).put(v1.v.z);
            posBuf.position(v2.index * 3);
            posBuf.put(v2.v.x).put(v2.v.y).put(v2.v.z);

            if (normBuf != null){
                normBuf.position(v0.index * 3);
                normBuf.put(v0.vn.x).put(v0.vn.y).put(v0.vn.z);
                normBuf.position(v1.index * 3);
                normBuf.put(v1.vn.x).put(v1.vn.y).put(v1.vn.z);
                normBuf.position(v2.index * 3);
                normBuf.put(v2.vn.x).put(v2.vn.y).put(v2.vn.z);
            }

            if (tcBuf != null){
                tcBuf.position(v0.index * 2);
                tcBuf.put(v0.vt.x).put(v0.vt.y);
                tcBuf.position(v1.index * 2);
                tcBuf.put(v1.vt.x).put(v1.vt.y);
                tcBuf.position(v2.index * 2);
                tcBuf.put(v2.vt.x).put(v2.vt.y);
            }

            indexBuf.put((short)v0.index);
            indexBuf.put((short)v1.index);
            indexBuf.put((short)v2.index);
        }

        m.setBuffer(VertexBuffer.Type.Position, 3, posBuf);
        m.setBuffer(VertexBuffer.Type.Normal,   3, normBuf);
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, tcBuf);
        m.setBuffer(VertexBuffer.Type.Index,    3, indexBuf);

        m.setStatic();
        m.updateBound();

        return m;
    }

    @SuppressWarnings("empty-statement")
    public Object load(AssetInfo info){
        InputStream in = info.openStream();
        scan = new Scanner(in);
        scan.useLocale(Locale.US);
        while (readLine());
        Mesh m = constructMesh();
        try{
            in.close();
        }catch (IOException ex){
        }
        return new Geometry(info.getKey().getName(), m);
    }

}
