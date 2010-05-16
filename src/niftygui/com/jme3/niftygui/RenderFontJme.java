package com.jme3.niftygui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.spi.render.RenderFont;

public class RenderFontJme implements RenderFont {

    private NiftyJmeDisplay display;
    private BitmapFont font;
    private BitmapText text;
    private Texture texture;
    private float actualSize;

    /**
     * Initialize the font.
     * @param name font filename
     */
    public RenderFontJme(String name, NiftyJmeDisplay display) {
        this.display = display;
        font = display.getAssetManager().loadFont(name);
        texture = font.getPage(0).getTextureParam("m_Texture").getTextureValue();
        text = new BitmapText(font);
        actualSize = font.getPreferredSize();
        text.setSize(actualSize);
    }

    public BitmapText getText(){
        return text;
    }

    public Texture2D getTexture(){
        return (Texture2D) texture;
    }

    /**
     * get font height.
     * @return height
     */
    public int getHeight() {
        return (int) text.getLineHeight();
    }

    /**
     * get font width of the given string.
     * @param text text
     * @return width of the given text for the current font
     */
    public int getWidth(final String str) {
        if (str.length() == 0)
            return 0;
        
        int result = (int) font.getLineWidth(str);
//        text.setText(str);
//        text.updateLogicalState(0);
//        int result = (int) text.getLineWidth();

        return result;
    }

    /**
     * Return the width of the given character including kerning information.
     * @param currentCharacter current character
     * @param nextCharacter next character
     * @param size font size
     * @return width of the character or null when no information for the character is available
     */
    public Integer getCharacterAdvance(final char currentCharacter, final char nextCharacter, final float size) {
        return null;
        //        CharacterInfo currentCharacterInfo = font.getChar(currentCharacter);
//        if (currentCharacterInfo == null) {
//            return null;
//        } else {
//            return new Integer(
//                    (int) (currentCharacterInfo.getXadvance() * size + getKerning(currentCharacterInfo, nextCharacter)));
//        }
    }

    public void dispose() {
    }
}