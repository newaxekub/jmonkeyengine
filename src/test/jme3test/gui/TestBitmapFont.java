package jme3test.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;

public class TestBitmapFont extends SimpleApplication {

    private String txtB =
    "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?";

    public static void main(String[] args){
        TestBitmapFont app = new TestBitmapFont();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText txt = new BitmapText(fnt, false);
        txt.setBox(new Rectangle(0, 0, settings.getWidth(), settings.getHeight()));
        txt.setSize(fnt.getPreferredSize() * 2f);
        txt.setText(txtB);
        txt.setLocalTranslation(0, settings.getHeight(), 0);
        guiNode.attachChild(txt);

        BitmapText txt4 = new BitmapText(fnt, false);
        txt4.setSize(fnt.getPreferredSize() * 1.2f);
        txt4.setText("Text without restriction. Text without restriction. Text without restriction. Text without restriction");
        txt4.setLocalTranslation(40, txt4.getLineHeight() * 2, 0);

        guiNode.attachChild(txt4);
    }

}
