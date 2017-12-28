package code.ponfee.commons.excel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

public class Test {

    public static void main(String[] args) {

        /**
         * Mock数据，Java对象列表
         */
        List<ShopDTO> shopDTOList = new ArrayList<ShopDTO>();
        for (int i = 0; i < 100; i++) {
            ShopDTO shop = new ShopDTO(true, "商户"+i, (short) i, 1000+i, 10000+i, (float) (1000+i), (double) (10000+i), new Date());
            shopDTOList.add(shop);
        }
        String filePath = "/Users/xuxueli/Downloads/demo-sheet.xls";

        /**
         * Excel导出：Object 转换为 Excel
         */
        ExcelExporter.write(filePath, shopDTOList);

        /**
         * Excel导入：Excel 转换为 Object
          */
        List<Object> list = ExcelImporter.read(filePath, ShopDTO.class);

        System.out.println(list);

    }

    @ExcelSheet(name = "商户列表", headColor = HSSFColor.HSSFColorPredefined.LIGHT_GREEN)
    public static class ShopDTO {

        @ExcelField(name = "是否VIP商户")
        private boolean vip;

        @ExcelField(name = "商户名称", align = HorizontalAlignment.CENTER)
        private String shopName;

        @ExcelField(name = "分店数量")
        private short branchNum;

        @ExcelField(name = "商户ID")
        private int shopId;

        @ExcelField(name = "浏览人数")
        private long visitNum;

        @ExcelField(name = "当月营业额")
        private float turnover;

        @ExcelField(name = "历史营业额")
        private double totalTurnover;

        @ExcelField(name = "开店时间", dateformat = "yyyy-MM-dd HH:mm:ss")
        private Date addTime;


        public ShopDTO() {
        }

        public ShopDTO(boolean vip, String shopName, short branchNum, int shopId, long visitNum, float turnover, double totalTurnover, Date addTime) {
            this.vip = vip;
            this.shopName = shopName;
            this.branchNum = branchNum;
            this.shopId = shopId;
            this.visitNum = visitNum;
            this.turnover = turnover;
            this.totalTurnover = totalTurnover;
            this.addTime = addTime;
        }

        public boolean isVip() {
            return vip;
        }

        public void setVip(boolean vip) {
            this.vip = vip;
        }

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public short getBranchNum() {
            return branchNum;
        }

        public void setBranchNum(short branchNum) {
            this.branchNum = branchNum;
        }

        public int getShopId() {
            return shopId;
        }

        public void setShopId(int shopId) {
            this.shopId = shopId;
        }

        public long getVisitNum() {
            return visitNum;
        }

        public void setVisitNum(long visitNum) {
            this.visitNum = visitNum;
        }

        public float getTurnover() {
            return turnover;
        }

        public void setTurnover(float turnover) {
            this.turnover = turnover;
        }

        public double getTotalTurnover() {
            return totalTurnover;
        }

        public void setTotalTurnover(double totalTurnover) {
            this.totalTurnover = totalTurnover;
        }

        public Date getAddTime() {
            return addTime;
        }

        public void setAddTime(Date addTime) {
            this.addTime = addTime;
        }

        @Override
        public String toString() {
            return "ShopDTO{" +
                    "vip=" + vip +
                    ", shopName='" + shopName + '\'' +
                    ", branchNum=" + branchNum +
                    ", shopId=" + shopId +
                    ", visitNum=" + visitNum +
                    ", turnover=" + turnover +
                    ", totalTurnover=" + totalTurnover +
                    ", addTime=" + addTime +
                    '}';
        }
    }
}
