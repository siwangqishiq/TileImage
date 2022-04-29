package panyi.xyz.tileimage;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.ChunkLoadBehaviour;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {


    public static void main(String[] args){
        // TileImage tileImage = new TileImage();

//        PngReader reader = new PngReader(new File("D:\\testimg\\1.png"));
//        ImageInfo info = reader.imgInfo;
//        System.out.println(info);
        String workDir = "D:\\testimg";
        TileImage.convertJpgDir("E:\\assets\\images" , workDir);
        ArrayList<TileImage.PictureInfo> picInfoList = TileImage.genMainColorPictureDir(workDir);

        String originImage = "D:\\gk.jpeg";

        String pngDstImage = "D:\\result.png";
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(originImage));

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            int tileImageWidth = 64;
            float ratio = (float)tileImageWidth / width;
            int tileImageHeight =(int)(ratio * height);
            Image scaleImage = bufferedImage.getScaledInstance(tileImageWidth , tileImageHeight ,
                    Image.SCALE_SMOOTH);

            BufferedImage scaleImageBuffer = TileImage.toBufferedImage(scaleImage);

            List<String> imgList = new ArrayList<String>();
            for(int i = 0 ; i < scaleImageBuffer.getHeight() ; i++){
                for(int j = 0 ; j < scaleImageBuffer.getWidth() ;j++){
                    int originColor = scaleImageBuffer.getRGB(j , i);
                    TileImage.PictureInfo info = findClosestImage(picInfoList , originColor);
                    System.out.println("find cloest path : " + info.path);
                    imgList.add(info.path);
                }//end for i
            }

            String[] list = new String[imgList.size()];
            imgList.toArray(list);
            TileImage.doTiling(list , pngDstImage , tileImageWidth);
        } catch (IOException e) {
            e.printStackTrace();
        }

         // TileImage.convertPngToJpg(pngDstImage ,"D:");
    }

    public static TileImage.PictureInfo findClosestImage(List<TileImage.PictureInfo> infoList , int color){
        TileImage.PictureInfo result = null;

        double cloestValue = Double.MAX_VALUE;

        int originRed = (color & 0xff0000) >> 16;
        int originGreen = (color & 0xff00) >> 8;
        int originBlue = color & 0xff;

        for(int i = 0 ; i < infoList.size() ;i++){
            TileImage.PictureInfo info = infoList.get(i);
            double distance = Math.sqrt((originRed - info.mainColorRed) * (originRed - info.mainColorRed) +
                    (originGreen - info.mainColorGreen) *(originGreen - info.mainColorGreen) +
                            (originBlue - info.mainColorBlue) * (originBlue - info.mainColorBlue));
            if(distance < cloestValue){
                cloestValue = distance;
                result = info;
            }
        }

        return result;
    }
}
