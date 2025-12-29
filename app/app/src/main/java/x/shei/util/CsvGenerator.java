package x.shei.util;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import x.shei.db.Bean;

public class CsvGenerator {
    /**
     * 生成 CSV 文件
     * @param beanList 解析后的 Bean 列表
     * @param filePath 保存路径（如 /sdcard/movie_data.csv，需申请存储权限）
     * @return 是否生成成功
     */
    public static boolean generateCsv(List<Bean> beanList, String filePath) {
        if (beanList == null || beanList.isEmpty() || filePath == null) {
            return false;
        }

        // CSV 写入器（GBK 编码避免中文乱码）
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {

            // 1. 写入 CSV 表头（与 Bean 字段对应）
            String[] header = {
                    "标题", "导演", "主演", "国家",
                    "更新时间", "详情介绍", "图片链接", "详情页链接",
                    "播放页链接", "m3u8 链接"
            };
            writer.writeNext(header);

            // 2. 写入数据行（遍历 Bean 列表）
            for (Bean bean : beanList) {
                String[] dataRow = {
                        bean.getTitle() == null ? "" : bean.getTitle(),
                        bean.getDirector() == null ? "" : bean.getDirector(),
                        bean.getMainActor() == null ? "" : bean.getMainActor(),
                        bean.getCountry() == null ? "" : bean.getCountry(),
                        bean.getUpdateTime() == null ? "" : bean.getUpdateTime(),
                        bean.getIntro() == null ? "" : bean.getIntro(),
                        bean.getSrc() == null ? "" : bean.getSrc(),
                        bean.getA() == null ? "" : bean.getA(),
                        bean.getA2() == null ? "" : bean.getA2(),
                        bean.getM3u8() == null ? "" : bean.getM3u8()
                };
                writer.writeNext(dataRow);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
