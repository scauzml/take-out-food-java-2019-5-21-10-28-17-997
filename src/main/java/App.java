import com.sun.org.apache.xpath.internal.operations.Or;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {
    private ItemRepository itemRepository;
    private SalesPromotionRepository salesPromotionRepository;

    public App(ItemRepository itemRepository, SalesPromotionRepository salesPromotionRepository) {
        this.itemRepository = itemRepository;
        this.salesPromotionRepository = salesPromotionRepository;
    }

    public String bestCharge(List<String> inputs) {
        //TODO: write code here
        class Order{
            private Item item;
            private double orderMoney;//菜品总额
            private int num;//菜品数量
            private boolean isDiscount=false;//是否是半价商品

            public Order() {
            }

            public Order(Item item, int num) {
                this.item = item;
                this.num = num;
            }

            public Item getItem() {
                return item;
            }

            public void setItem(Item item) {
                this.item = item;
            }

            public double getOrderMoney() {
                return orderMoney;
            }

            public void setOrderMoney(double orderMoney) {
                this.orderMoney = orderMoney;
            }

            public int getNum() {
                return num;
            }

            public void setNum(int num) {
                this.num = num;
            }

            public boolean isDiscount() {
                return isDiscount;
            }

            public void setDiscount(boolean discount) {
                isDiscount = discount;
            }
        }
        class Bill{
            private double billMoney;//优惠后的总额
            private double balance;//差额
            private String salePromotionType;//优惠类型

            public Bill() {
            }

            public Bill(double billMoney, double balance, String salePromotionType) {
                this.billMoney = billMoney;
                this.balance = balance;
                this.salePromotionType = salePromotionType;
            }

            public double getBillMoney() {
                return billMoney;
            }

            public void setBillMoney(double billMoney) {
                this.billMoney = billMoney;
            }

            public double getBalance() {
                return balance;
            }

            public void setBalance(double balance) {
                this.balance = balance;
            }

            public String getSalePromotionType() {
                return salePromotionType;
            }

            public void setSalePromotionType(String salePromotionType) {
                this.salePromotionType = salePromotionType;
            }
        }
        //查出所有菜品，优惠方式
        List<Item> itemList= this.itemRepository.findAll();
        List<SalesPromotion> salesPromotionList=this.salesPromotionRepository.findAll();
        //处理inputs,获取菜品订单信息
        List<Order> orders = new ArrayList<>();
        for (String s:inputs
             ) {
            String[] result = s.split(" ");
            for (Item item: itemList
                 ) {
                if(result[0].trim().equals(item.getId())){
                    Order order=new Order();
                    order.setItem(item);
                    order.setNum(Integer.valueOf(result[result.length-1]));
                    order.setOrderMoney(item.getPrice()*order.getNum());
                    orders.add(order);
                    break;
                }
            }
        }
        //记录各种优惠方式的最后金额
        List<Bill> listBill = new ArrayList<>();
        double totalMoney=0;//总额
        for (Order order:orders
        ) {
            totalMoney+=order.getOrderMoney();
        }
        List<String> balanceItemNameList = new ArrayList<>();
        for (SalesPromotion sp:salesPromotionList
            ) {
                Bill bill=new Bill();
               if(sp.getType().equals("BUY_30_SAVE_6_YUAN")){

                   if(totalMoney>=30){
                       bill.setBillMoney(totalMoney-6);
                       bill.setBalance(6);
                       bill.setSalePromotionType(sp.getDisplayName());
                       listBill.add(bill);
                   }
               }else if(sp.getType().equals("50%_DISCOUNT_ON_SPECIFIED_ITEMS")){
                   double balance=0;
                   for (Order order:orders
                        ) {
                       for (String itemId: sp.getRelatedItems()
                            ) {
                           if (itemId.equals(order.getItem().getId())) {
                               order.setDiscount(true);
                               balanceItemNameList.add(order.getItem().getName());
                               balance+=order.getOrderMoney()/2;
                               break;
                           }
                       }
                   }
                   if(balance>0){
                       bill.setBalance(balance);
                       bill.setBillMoney(totalMoney-balance);
                       bill.setSalePromotionType(sp.getDisplayName());
                       listBill.add(bill);
                   }

               }
            }
        String receiptString="============= 订餐明细 =============\n";
        for (Order o:orders
        ) {
            receiptString+=o.getItem().getName()+" x "+o.getNum()+" = "+(int)o.getOrderMoney()+"元\n";
        }
        receiptString+= "-----------------------------------\n";
        String minType="";
        if (listBill.size() > 0) {
            Bill bill=null;
           double minMoney=listBill.get(0).getBillMoney();
            minType=listBill.get(0).getSalePromotionType();
            for (Bill b : listBill) {
                if (b.getBillMoney() < minMoney) {
                    minMoney=b.getBillMoney();
                    minType=b.getSalePromotionType();
                }
            }
            for (Bill b :listBill) {
                if (minType.equals(b.getSalePromotionType())) {
                    bill=b;
                    break;
                }
            }

            receiptString+="使用优惠:\n";
            if(minType.equals("满30减6元")){
                receiptString+=minType+"，";
                receiptString+="省"+(int)bill.getBalance()+"元\n";
                receiptString+= "-----------------------------------\n";
                receiptString+="总计："+(int)bill.getBillMoney()+"元\n"+
                        "===================================";
            }else if(minType.equals("指定菜品半价")) {
                receiptString+=minType+"(";
                for (int i=0;i<balanceItemNameList.size();i++
                     ) {
                    if (i != balanceItemNameList.size() - 1) {
                        receiptString+=balanceItemNameList.get(i)+"，";
                    }else {
                        receiptString+=balanceItemNameList.get(i)+")，";
                    }
                }
                receiptString+="省"+(int)bill.getBalance()+"元\n";
                receiptString+= "-----------------------------------\n";
                receiptString+="总计："+(int)bill.getBillMoney()+"元\n"+
                        "===================================";
            }

        } else {

            receiptString+="总计："+(int)totalMoney+"元\n"+
                    "===================================";
        }



        return receiptString;
    }
}
