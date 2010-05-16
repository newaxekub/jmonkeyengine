package com.jme3.scene.plugins.ogre;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.BufferUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Scanner;

public class MaterialLoader implements AssetLoader {

    private String folderName;
    private AssetManager assetManager;
    private Scanner scan;
    private ColorRGBA diffuse, specular;
    private Texture texture;
    private String texName;
    private String matName;
    private float shinines;
    private boolean vcolor = false;
    private boolean blend = false;
    private boolean twoSide = false;

    private String readString(String end){
        scan.useDelimiter(end);
        String str = scan.next();
        scan.useDelimiter("\\p{javaWhitespace}+");
        return str.trim();
    }

    private ColorRGBA readColor(){
        ColorRGBA color = new ColorRGBA();
        color.r = scan.nextFloat();
        color.g = scan.nextFloat();
        color.b = scan.nextFloat();
        if (scan.hasNextFloat()){
            color.a = scan.nextFloat();
        }
        return color;
    }

    private void readTextureImage(){
        // texture image def
        String ln = scan.nextLine();
        Scanner lnScan = new Scanner(ln);
        String path = lnScan.next();
        String mips = null;
        String type = null;
        if (lnScan.hasNext()){
            // more params
            type = lnScan.next();
            if (!lnScan.hasNext("\n")){
                mips = lnScan.next();
                if (lnScan.hasNext()){
                    // even more params..
                    // will have to ignore
                }
            }
        }

        boolean genMips = true;
        boolean cubic = false;
        if (type != null && type.equals("0"))
            genMips = false;

        if (type != null && type.equals("cubic")){
            cubic = true;
        }

        TextureKey key = new TextureKey(folderName + path, false);
        key.setGenerateMips(genMips);
        key.setAsCube(cubic);
        texture = assetManager.loadTexture(key);
        if (texture == null){
            ByteBuffer tempData = BufferUtils.createByteBuffer(3);
            tempData.put((byte)0xFF).put((byte)0x00).put((byte)0x00);
            texture = new Texture2D(new Image(Format.RGB8, 1,1,tempData));
            System.out.println("WARNING! Using white mat instead of "+path);
        }
        
        texture.setWrap(WrapMode.Repeat);
        if (texName != null){
            texture.setName(texName);
            texName = null;
        }
    }

    private void readTextureUnitStatement(){
        String keyword = scan.next();
        if (keyword.equals("texture")){
            readTextureImage();
        }else if (keyword.equals("texture_alias")){
            texture.setName(scan.next());
        }else if (keyword.equals("tex_address_mode")){
            String mode = scan.next();
            if (mode.equals("wrap")){
                texture.setWrap(WrapMode.Repeat);
            }else if (mode.equals("clamp")){
                texture.setWrap(WrapMode.Clamp);
            }else if (mode.equals("mirror")){
                texture.setWrap(WrapMode.MirroredRepeat);
            }else if (mode.equals("border")){
                texture.setWrap(WrapMode.BorderClamp);
            }
        }else if (keyword.equals("filtering")){
            // ignored.. only anisotrpy is considered
            readString("\n");
        }else if (keyword.equals("max_anisotropy")){
            int amount = scan.nextInt();
            texture.setAnisotropicFilter(amount);
        }else{
            System.out.println("Unsupported texture_unit directive: "+keyword);
            readString("\n");
        }
    }

    private void readTextureUnit(){
        // name is optional
        if (!scan.hasNext("\\{")){
            texName = readString("\\{");
        }else{
            texName = null;
        }
        scan.next(); // skip "{"
        while (!scan.hasNext("\\}")){
            readTextureUnitStatement();
        }
        scan.next(); // skip "}"
    }

    private void readPassStatement(){
        // read until newline
        String keyword = scan.next();
        if (keyword.equals(""))
            return;

        if (keyword.equals("diffuse")){
            if (scan.hasNext("vertexcolour")){
                // use vertex colors
                diffuse = ColorRGBA.White;
                vcolor = true;
                scan.next(); // skip it
            }else{
                diffuse = readColor();
            }
        }else if (keyword.equals("specular")){
            specular = new ColorRGBA();
            specular.r = scan.nextFloat();
            specular.g = scan.nextFloat();
            specular.b = scan.nextFloat();
            float unknown = scan.nextFloat();
            if (scan.hasNextFloat()){
                // using 5 float values
                specular.a = unknown;
                shinines = scan.nextFloat();
            }else{
                // using 4 float values
                specular.a = 1f;
                shinines = unknown;
            }
        }else if (keyword.equals("texture_unit")){
            readTextureUnit();
        }else if (keyword.equals("scene_blend")){
            String mode = scan.next();
            if (mode.equals("alpha_blend")){
                blend = true;
            }
        }else if (keyword.equals("cull_hardware")){
            String mode = scan.next();
            if (mode.equals("none")){
                twoSide = true;
            }
        }else if (keyword.equals("cull_software")){
            // ignore
            scan.next();
        }else{
            System.out.println(matName + ": " + keyword);
            readString("\n");
        }
    }

    private void readPass(){
        scan.next(); // skip "pass"
        // name is optional
        String name;
        if (scan.hasNext("\\{")){
            // no name
            name = null;
        }else{
            name = readString("\\{");
        }
        scan.next(); // skip "{"
        while (!scan.hasNext("\\}")){
            readPassStatement();
        }
        scan.next(); // skip "}"
    }

    private void readTechnique(){
        scan.next(); // skip "technique"
        // name is optional
        String name;
        if (scan.hasNext("\\{")){
            // no name
            name = null;
        }else{
            name = readString("\\{");
        }
        scan.next(); // skip "{"
        while (!scan.hasNext("\\}")){
            readPass();
        }
        scan.next();
    }

    private boolean readMaterialStatement(){
        if (scan.hasNext("technique")){
            readTechnique();
            return true;
        }else if (scan.hasNext("receive_shadows")){
            // skip "recieve_shadows"
            scan.next();
            String isOn = scan.next();
            if (isOn != null && isOn.equals("true")){

            }
            return true;
        }else{
            return false;
        }
    }

    @SuppressWarnings("empty-statement")
    private void readMaterial(){
        scan.next(); // skip "material"
        // read name
        matName = readString("\\{");
        scan.next(); // skip "{"
        while (!scan.hasNext("\\}")){
            readMaterialStatement();
        }
        scan.next();
    }

    private Material compileMaterial(){
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        if (blend){
            RenderState rs = mat.getAdditionalRenderState();
            rs.setAlphaTest(true);
            rs.setAlphaFallOff(0.01f);
            rs.setBlendMode(RenderState.BlendMode.Alpha);
            if (twoSide)
                rs.setFaceCullMode(RenderState.FaceCullMode.Off);
//            rs.setDepthWrite(false);
            mat.setTransparent(true);
            mat.setBoolean("m_UseAlpha", true);
        }else{
            if (twoSide){
                RenderState rs = mat.getAdditionalRenderState();
                rs.setFaceCullMode(RenderState.FaceCullMode.Off);
            }
        }

        if (shinines > 0f)
            mat.setFloat("m_Shininess", shinines);
        
        if (vcolor)
            mat.setBoolean("m_UseVertexColor", true);

        if (texture != null)
            mat.setTexture("m_DiffuseMap", texture);

        texture = null;
        diffuse = null;
        specular = null;
        texture = null;
        shinines = 0f;
        vcolor = false;
        blend = false;
        return mat;
    }

    public Object load(AssetInfo info) throws IOException {
        folderName = info.getKey().getFolder();
        assetManager = info.getManager();
        OgreMaterialList list = new OgreMaterialList();
        scan = new Scanner(info.openStream());
        scan.useLocale(Locale.US);
        while (scan.hasNext("material")){
            readMaterial();
            Material mat = compileMaterial();
            list.put(matName, mat);
        }
        return list;
    }

}
