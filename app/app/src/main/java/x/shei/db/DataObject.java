package x.shei.db;

public class DataObject {
    private String part1;
    private String part2;
    public String img;
    public String title;


    public DataObject(String part1, String part2) {
        this.part1 = part1;
        this.part2 = part2;
    }
    public DataObject(String part1) {
        this.part1 = part1;
    }

    public String getPart1() {
        return part1;
    }

    public String getPart2() {
        return part2;
    }
    public String getImg() {
        return img;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "part1='" + part1 + '\'' +
                ", part2='" + part2 + '\'' +
                '}';
    }
}
