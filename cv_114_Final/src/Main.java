import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.util.Arrays;
import java.util.Random;



//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main
{
    public static void main(String[] args) throws IOException
    {
        String img_path = "src/image.jpg";

        BufferedImage image =  ImageProcessing.importImage(img_path);
        BufferedImage grayImage = cv_filter.grayScale(image);
        BufferedImage negativeImage = cv_filter.Negative(grayImage);
        BufferedImage gammaImage_2 = cv_filter.adjustGamma(grayImage, 2);
        BufferedImage gammaImage_01 = cv_filter.adjustGamma(grayImage, 0.1);
        BufferedImage contrastImage = cv_filter.contrastStretch(grayImage);
        BufferedImage pepperNoiseImage = cv_filter.addSaltAndPepperNoise(gammaImage_01, 0.1);
        BufferedImage medianFilterImage = cv_filter.MedianFilter(pepperNoiseImage);
        BufferedImage laplacianImage = cv_filter.Laplacian(contrastImage);
        BufferedImage otsuImage = cv_filter.OtsuThresholding(gammaImage_2);
        BufferedImage maxFilterImage = cv_filter.MaxFiltering(laplacianImage);

        //輸出所有圖片
        String[] file_name = new String[]{"image", "grayImage", "negativeImage", "gammaImage_01"
                , "gammaImage_2", "contrastImage", "pepperNoiseImage", "medianFilterImage"
                , "laplacianImage", "otsuImage", "maxFilterImage"};
        for (int i = 0; i < 11; i++) {
            ImageProcessing.exportImage(new BufferedImage[]{image,grayImage,negativeImage,gammaImage_01
                    , gammaImage_2,contrastImage,pepperNoiseImage,medianFilterImage
                    ,laplacianImage,otsuImage,maxFilterImage}[i]
                    , "processed_image_result/"+file_name[i]+".png");
        }
    }

}

class ImageProcessing
{
    public static BufferedImage  importImage(String path) throws IOException
    {
        File input = new File(path);
        return ImageIO.read(input);
    }
    public static void exportImage(BufferedImage image, String path) throws IOException
    {
        File output = new File(path);
        ImageIO.write(image, "png", output);
    }
    public static BufferedImage combineImages( BufferedImage[] images) throws IOException {
        // 計算新圖片的寬度和高度
        BufferedImage sampleImage =images[0];
        int maxWidth = sampleImage.getWidth() * 4;
        int maxHeight = sampleImage.getHeight() * 3;

        // 建立新圖片
        BufferedImage combinedImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = combinedImage.getGraphics();

        // 繪製每張圖片到新圖片上
        int currentX = 0;
        int currentY = 0;
        int i = 0;
        for (BufferedImage image : images) {
            g.drawImage(image, currentX, currentY, null);
            currentX += image.getWidth();

            if ((i + 1) % 4 == 0) {
                currentX = 0;
                currentY += image.getHeight();
            }
            i++;
        }
        g.dispose();

        return combinedImage;
    }
}

class cv_filter
{
    // 灰階化
    public static BufferedImage grayScale(BufferedImage image){
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                int grayValue = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                Color grayColor = new Color(grayValue, grayValue, grayValue);
                grayImage.setRGB(x, y, grayColor.getRGB());
            }
        }
        return grayImage;
    }
    // 負片
    public static BufferedImage Negative(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        // 建立與原圖片大小相同的新圖片
        BufferedImage negativeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 獲取原像素值 (灰階值)
                Color color = new Color(image.getRGB(x, y));
                int gray = color.getBlue();

                // 計算負片值
                int negativeGray = 255 - gray;
                int negativeColor = new Color(negativeGray, negativeGray, negativeGray).getRGB();
                negativeImage.setRGB(x, y, negativeColor);
            }
        }

        return negativeImage;
    }
    // Gamma 調整
    public static BufferedImage adjustGamma(BufferedImage image, double gamma) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 建立與原圖片大小相同的新圖片
        BufferedImage gammaAdjusted = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 預先計算 Gamma 映射表，加速運算
        int[] gammaLUT = new int[256];
        for (int i = 0; i < 256; i++) {
            gammaLUT[i] = (int) (255 * Math.pow(i / 255.0, gamma));
            gammaLUT[i] = Math.min(255, Math.max(0, gammaLUT[i])); // 限制值在 [0, 255]
        }

        // 進行像素調整
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 獲取原像素值
                Color color = new Color(image.getRGB(x, y));
                int gray = color.getBlue();

                // Gamma 調整
                int adjustedGray = gammaLUT[gray];

                // 設定新的像素值
                int newColor = new Color(adjustedGray, adjustedGray, adjustedGray).getRGB();
                gammaAdjusted.setRGB(x, y, newColor);
            }
        }

        return gammaAdjusted;
    }
    // 對比拉伸
    public static BufferedImage contrastStretch(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 找到灰階值的最大值與最小值
        int minGray = 255;
        int maxGray = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = color.getBlue();
                minGray = Math.min(minGray, gray);
                maxGray = Math.max(maxGray, gray);
            }
        }
        // 創建新圖片並進行拉伸
        BufferedImage stretchedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = color.getBlue();

                // 計算拉伸後的灰階值
                int newGray = (gray - minGray) * 255 / (maxGray - minGray);

                // 設定新像素值
                int newColor = new Color(newGray, newGray, newGray).getRGB();
                stretchedImage.setRGB(x, y, newColor);
            }
        }

        return stretchedImage;
    }

    // 添加胡椒鹽雜訊
    public static BufferedImage addSaltAndPepperNoise(BufferedImage image, double noiseDensity) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        // 創建雜訊隨機生成器
        Random random = new Random();

        // 計算需添加雜訊的像素數
        int noisyPixels = (int) (totalPixels * noiseDensity);

        // 複製原圖片
        BufferedImage noisyImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                noisyImage.setRGB(x, y, image.getRGB(x, y));
            }
        }

        // 隨機選擇像素並添加雜訊
        for (int i = 0; i < noisyPixels; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // 隨機選擇黑色 (胡椒) 或白色 (鹽)
            int noiseColor = random.nextBoolean() ? 0 : 255;
            int newColor = new Color(noiseColor, noiseColor, noiseColor).getRGB();
            noisyImage.setRGB(x, y, newColor);
        }

        return noisyImage;
    }
    // 中值濾波
    public static BufferedImage MedianFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 定義濾波器窗口的大小
        int windowSize = 3;
        int offset = windowSize / 2;

        // 處理每個像素
        for (int y = offset; y < height - offset; y++) {
            for (int x = offset; x < width - offset; x++) {

                // 擷取 3x3 區域的灰階值
                int[] window = new int[windowSize * windowSize];
                int idx = 0;
                for (int ky = -offset; ky <= offset; ky++) {
                    for (int kx = -offset; kx <= offset; kx++) {
                        Color color = new Color(image.getRGB(x + kx, y + ky));
                        int gray = color.getBlue();
                        window[idx++] = gray;
                    }
                }

                // 排序並取得中位數
                Arrays.sort(window);
                int median = window[window.length / 2];

                // 設定中位數作為新的像素值
                int newColor = new Color(median, median, median).getRGB();
                filteredImage.setRGB(x, y, newColor);
            }
        }

        return filteredImage;
    }
    // 拉普拉斯邊緣檢測
    public static BufferedImage Laplacian(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 創建一個新圖片來存儲邊緣偵測結果
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 定義 Laplacian 核
        int[][] laplacianKernel = {
                {-1, -1, -1},
                {-1, 8, -1},
                {-1, -1, -1}
        };

        // 遍歷每個像素，對其進行 Laplacian 運算
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sum = 0;

                // 遍歷 3x3 的相鄰像素
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        // 取得周圍像素的灰階值
                        Color color = new Color(image.getRGB(x + kx, y + ky));
                        int gray = color.getBlue();

                        // 卷積計算
                        sum += gray * laplacianKernel[ky + 1][kx + 1];
                    }
                }

                // 使結果不超過 0 到 255 的範圍
                sum = Math.min(255, Math.max(0, sum));

                // 設定計算後的灰階值
                int newColor= new Color(sum, sum, sum).getRGB();
                resultImage.setRGB(x, y, newColor);
            }
        }

        return resultImage;
    }


    // Otsu 閾值二值化
    public static BufferedImage OtsuThresholding(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 計算灰度直方圖
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = rgb & 0xFF;  // 提取灰度值
                histogram[gray]++;
            }
        }

        // 計算總體的平均灰度值
        int totalPixels = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int weightB = 0;
        int weightF;
        float varMax = 0;
        int threshold = 0;

        // Otsu 的閾值選擇算法
        for (int t = 0; t < 256; t++) {
            weightB += histogram[t];  // 背景權重
            if (weightB == 0) continue;

            weightF = totalPixels - weightB;  // 前景權重
            if (weightF == 0) break;

            sumB += (float) (t * histogram[t]);  // 背景的加權灰度總和
            float meanB = sumB / weightB;  // 背景的平均灰度
            float meanF = (sum - sumB) / weightF;  // 前景的平均灰度

            // 計算類間方差
            float varBetween = (float) weightB * weightF * (meanB - meanF) * (meanB - meanF);

            // 找到最大類間方差對應的閾值
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        // 應用選擇的閾值進行二值化
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x , y));
                int gray = color.getBlue();

                // 根據閾值將像素設為黑或白
                int newColor = (gray >= threshold) ?  new Color(255, 255, 255).getRGB() : new Color(0, 0, 0).getRGB();
                binaryImage.setRGB(x, y, newColor);
            }
        }

        return binaryImage;
    }
    // 最大值濾波
    public static BufferedImage MaxFiltering(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 創建一個新圖片來存儲濾波後的結果
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 遍歷圖像中的每個像素，並對每個像素周圍的 3x3 區域進行最大值濾波
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxGray = 0;

                // 遍歷 3x3 區域
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = new Color(image.getRGB(x + kx, y + ky));
                        int gray = color.getBlue();

                        // 找出區域中的最大灰度值
                        maxGray = Math.max(maxGray, gray);
                    }
                }

                // 設定結果圖像中的該像素為最大灰度值
                int newColor = new Color(maxGray, maxGray, maxGray).getRGB();
                resultImage.setRGB(x, y, newColor);
            }
        }

        return resultImage;
    }

}
