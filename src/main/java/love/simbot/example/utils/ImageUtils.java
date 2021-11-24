package love.simbot.example.utils;

import lombok.extern.slf4j.Slf4j;
import love.simbot.example.domain.RobotUserTeamDTO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ImageUtils {

    public static byte[] myGraphicsGeneration(List<RobotUserTeamDTO> cellsValue, String groupName) {
        // 字体大小
        int fontTitileSize = 15;
        // 横线的行数
        int totalrow = 6;
        // 竖线的行数
        int totalcol = 5;
        // 图片宽度
        int imageWidth = 1024;
        // 行高
        int rowheight = 40;
        // 图片高度
        int imageHeight = totalrow * rowheight + 50;
        // 起始高度
        int startHeight = 10;
        // 起始宽度
        int startWidth = 10;
        // 单元格宽度
        int colwidth = (int) ((imageWidth - 20) / totalcol);
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imageWidth, imageHeight);
        graphics.setColor(new Color(220, 240, 240));

        //画横线
        for (int j = 0; j < totalrow; j++) {
            graphics.setColor(Color.black);
            graphics.drawLine(startWidth, startHeight + (j + 1) * rowheight, startWidth + colwidth * totalcol, startHeight + (j + 1) * rowheight);
        }
        //画竖线
        for (int k = 0; k < totalcol + 1; k++) {
            graphics.setColor(Color.black);
            graphics.drawLine(startWidth + k * colwidth, startHeight + rowheight, startWidth + k * colwidth, startHeight + rowheight * totalrow);
        }
        //设置字体
        Font font = new Font("微软雅黑", Font.BOLD, fontTitileSize);
        graphics.setFont(font);
        //写标题
        graphics.drawString(groupName, startWidth, startHeight + rowheight - 10);
        //写入内容
        int sum = cellsValue.size();
        int cnt = 0;
        for (int n = 0; n < 5; n++) {
            for (int l = 0; l < 5; l++) {
                font = new Font("微软雅黑", Font.PLAIN, fontTitileSize);
                graphics.setFont(font);
                graphics.setColor(Color.BLACK);
                //sum--;
                String value;
                if(cnt>=sum) value = "";
                else{
                    value = cellsValue.get(cnt).getUserName();
                    if(value.length()>=6) {
                        value = value.substring(0,4) + "..";
                    }
                    value = value + " [" + cellsValue.get(cnt).getUserCode() + "]";
                }
                //String value = sum < 0 ? "未进组黑鬼" : cellsValue.get(sum).getUserName() + "（" + cellsValue.get(sum).getUserCode() + "）";
                cnt++;
                graphics.drawString(value, startWidth + colwidth * l + 5, startHeight + rowheight * (n + 2) - 10);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            log.error("构建图片异常", e);
        }
        return out.toByteArray();
    }
}