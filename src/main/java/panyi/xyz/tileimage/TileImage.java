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

public class TileImage {
    public static class PictureInfo{
        String path;

        int mainColorRed;
        int mainColorGreen;
        int mainColorBlue;
    }

    public void readTileImages(String dir){
    }



    public static PictureInfo parseImageFile(String path){
        PictureInfo info = new PictureInfo();
        info.path = path;

        try {
            BufferedImage bufferedImage = ImageIO.read(new File(path));

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            int totalRed = 0;
            int totalGreen = 0;
            int totalBlue = 0;

            int pixelCount = 0;
            for(int i = 0 ; i < height ; i++){
                for(int j = 0 ; j < width ;j++){
                    int color = bufferedImage.getRGB(j , i);

                    int red = (color & 0xff0000) >> 16;
                    int green = (color & 0xff00) >> 8;
                    int blue = color & 0xff;

                    totalRed += red;
                    totalGreen += green;
                    totalBlue += blue;

                    pixelCount++;
                }//end for j
            }//end for i

            info.mainColorRed = totalRed / pixelCount;
            info.mainColorGreen = totalGreen / pixelCount;
            info.mainColorBlue = totalBlue / pixelCount;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return info;
    }

    public static void convertJpgDir(String dir ,String dstDir){
        File file = new File(dir);
        File jpgFiles[] = file.listFiles();
        for(File f: jpgFiles){
            if(f.isFile()){
                System.out.println("convert " + f.getAbsolutePath() +" to png!");
                convertResizeJpgToPng(f.getAbsolutePath() , dstDir , 126,126);
            }
        }//end for
    }

    public static ArrayList<PictureInfo> genMainColorPictureDir(String dir){
        File file = new File(dir);
        File pngFiles[] = file.listFiles();

        ArrayList<PictureInfo> infoList = new ArrayList<PictureInfo>();

        for(File f: pngFiles){
            if(f.isFile()){
                PictureInfo info = parseImageFile(f.getAbsolutePath());
                infoList.add(info);
            }
        }//end for

        return infoList;
    }

    public static void convertResizeJpgToPng(String jpgFilePath , String dstDir , int width , int height){
        try{
            File originFile = new File(jpgFilePath);
            String nameNoSuffix = findFileNameNoSuffix(originFile.getName());
            String pngName = nameNoSuffix+".png";
            File pngFile = new File(dstDir , pngName);

            if(pngFile.exists()){
                pngFile.deleteOnExit();
            }

            try {
                BufferedImage bufferedImage = ImageIO.read(originFile);
                BufferedImage scaleImage = toBufferedImage(bufferedImage.getScaledInstance(width , height , Image.SCALE_AREA_AVERAGING));
                ImageIO.write(scaleImage, "png", pngFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void convertJpgToPng(String jpgFilePath , String dstDir){
        File originFile = new File(jpgFilePath);
        String nameNoSuffix = findFileNameNoSuffix(originFile.getName());
        String pngName = nameNoSuffix+".png";
        File pngFile = new File(dstDir , pngName);

        if(pngFile.exists()){
            pngFile.deleteOnExit();
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(originFile);
            ImageIO.write(bufferedImage, "png", pngFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void convertPngToJpg(String pngFilePath , String dstDir){
        File originFile = new File(pngFilePath);
        String nameNoSuffix = findFileNameNoSuffix(originFile.getName());
        String jpgName = nameNoSuffix+".jpg";
        File pngFile = new File(dstDir , jpgName);

        if(pngFile.exists()){
            pngFile.deleteOnExit();
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(originFile);
            ImageIO.write(bufferedImage, "jpg", pngFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static String findFileNameNoSuffix(String name){
        int index = name.lastIndexOf(".");
        if(index != -1){
            return name.substring(0 , index);
        }
        return name;
    }


    public static void doTiling(String tiles[], String dest, int nTilesX) {
        int ntiles = tiles.length;
        int nTilesY = (ntiles + nTilesX - 1) / nTilesX; // integer ceil
        ImageInfo imi1, imi2; // 1:small tile   2:big image
        PngReader pngr = new PngReader(new File(tiles[0]));
        imi1 = pngr.imgInfo;
        PngReader[] readers = new PngReader[nTilesX];
        imi2 = new ImageInfo(imi1.cols * nTilesX, imi1.rows * nTilesY, imi1.bitDepth, imi1.alpha, imi1.greyscale,
                imi1.indexed);
        PngWriter pngw = new PngWriter(new File(dest), imi2, true);
        // copy palette and transparency if necessary (more chunks?)
        pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_PALETTE
                | ChunkCopyBehaviour.COPY_TRANSPARENCY);
        pngr.readSkippingAllRows(); // reads only metadata
        pngr.end(); // close, we'll reopen it again soon
        ImageLineInt line2 = new ImageLineInt(imi2);
        int row2 = 0;
        for (int ty = 0; ty < nTilesY; ty++) {
            System.out.println("do tile " + ty +" / " + nTilesY);

            int nTilesXcur = ty < nTilesY - 1 ? nTilesX : ntiles - (nTilesY - 1) * nTilesX;
            Arrays.fill(line2.getScanline(), 0);
            for (int tx = 0; tx < nTilesXcur; tx++) { // open several readers
                readers[tx] = new PngReader(new File(tiles[tx + ty * nTilesX]));
                readers[tx].setChunkLoadBehaviour(ChunkLoadBehaviour.LOAD_CHUNK_NEVER);
                if (!readers[tx].imgInfo.equals(imi1))
                    throw new RuntimeException("different tile ? " + readers[tx].imgInfo);
            }
            for (int row1 = 0; row1 < imi1.rows; row1++, row2++) {
                for (int tx = 0; tx < nTilesXcur; tx++) {
                    ImageLineInt line1 = (ImageLineInt) readers[tx].readRow(row1); // read line
                    System.arraycopy(line1.getScanline(), 0, line2.getScanline(), line1.getScanline().length * tx,
                            line1.getScanline().length);
                }
                pngw.writeRow(line2, row2); // write to full image
            }
            for (int tx = 0; tx < nTilesXcur; tx++)
                readers[tx].end(); // close readers
        }
        pngw.end(); // close writer
    }
}
