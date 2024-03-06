package com.xuecheng.base.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * @description 二维码生成工具
 * @author Mr.M
 * @date 2022/10/3 0:03
 * @version 1.0
 */
public class QRCodeUtil {

    public static String createQRCode(String content, int width, int height) throws IOException {
        String resultImage = "";
        //除了尺寸，传入内容不能为空
        if (!StringUtils.isEmpty(content)) {
            ServletOutputStream stream = null;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            @SuppressWarnings("rawtypes")
            HashMap<EncodeHintType, Comparable> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); //字符编码
            //L->M->Q->H四个纠错等级从低到高，指定二维码的纠错等级为M，纠错级别越高，可以修正的错误就越多，需要的纠错码的数量也变多，相应的二维吗可储存的数据就会减少
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1); //图片边距
            try {
                //zxing生成二维码核心类
                QRCodeWriter writer = new QRCodeWriter();
                //把输入文本按照指定规则转成二维吗
                BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
                //生成二维码图片流
                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                //输出流
                ImageIO.write(bufferedImage, "png", os);
                /**
                 * 原生转码前面没有 data:image/png;base64 这些字段，返回给前端是无法被解析，所以加上前缀
                 */
                resultImage = new String("data:image/png;base64," + EncryptUtil.encodeBase64(os.toByteArray()));
                return resultImage;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("生成二维码出错");
            } finally {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                }
            }
        }
        return null;
    }
}