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
import java.util.Random;

public class Main {


    public static void main(String[] args){
        String workDir = "D:\\testimg";
        String originImage = "D:\\gakki.jpg";
        String pngDstImage = "D:\\result.png";

        File workDirFile = new File(workDir);
        if(workDirFile.exists()){
            workDirFile.deleteOnExit();
        }
        workDirFile.mkdirs();

        TileImage.convertJpgDir("E:\\assets\\img\\wang" , workDir);

        ArrayList<TileImage.PictureInfo> picInfoList = TileImage.genMainColorPictureDir(workDir);
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(originImage));

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            int tileImageWidth = 64;
            float ratio = (float)tileImageWidth / width;
            int tileImageHeight =(int)(ratio * height);
            Image scaleImage = bufferedImage.getScaledInstance(tileImageWidth , tileImageHeight ,
                    Image.SCALE_AREA_AVERAGING);

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

    public static class CandidateItem{
        TileImage.PictureInfo picInfo;
        double distance = 0.0f;

        public CandidateItem(TileImage.PictureInfo picInfo, double distance) {
            this.picInfo = picInfo;
            this.distance = distance;
        }
    }

    public static TileImage.PictureInfo findClosestImage(List<TileImage.PictureInfo> infoList , int color){
        final int candidateSize = 1;

        List<CandidateItem> candidateList = new ArrayList<CandidateItem>(candidateSize);
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

                if(candidateList.size() > candidateSize){//remove farest item
                    int delIndex = -1;
                    double farestValue = 0.0f;
                    for(int j = 0 ; j < candidateList.size();j++){
                        if(farestValue < candidateList.get(j).distance){
                            farestValue = candidateList.get(j).distance;
                            delIndex = j;
                        }
                    }//end for j

                    if(delIndex >= 0){
                        candidateList.remove(delIndex);
                    }
                }

                candidateList.add(new CandidateItem(info , cloestValue));
            }
        }//end for i

        Random rnd = new Random();
        int rndIndex = rnd.nextInt(candidateList.size());
        return candidateList.get(rndIndex).picInfo;
    }
}
