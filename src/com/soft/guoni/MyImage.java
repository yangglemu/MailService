package com.soft.guoni;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 123456 on 2016/6/22.
 */
public class MyImage {
    private ImageIcon icon;
    private Image image;

    public MyImage() {

        icon = new ImageIcon(("src/images/icon.png"));
        image = Toolkit.getDefaultToolkit().getImage("src/images/icon.png");
    }

    public ImageIcon getIcon() {
        return icon;
    }
    public Image getImage(){
        return image;
    }
}
