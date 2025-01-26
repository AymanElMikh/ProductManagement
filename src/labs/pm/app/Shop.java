package labs.pm.app;

import labs.pm.data.Product;

import java.math.BigDecimal;

public class Shop {
    public static void main(String[] args) {

        Product p1 = new Product();

        p1.setId(1);
        p1.setName("Shoes");
        p1.setPrice(BigDecimal.valueOf(121.211));

        System.out.println(p1);
        System.out.println(p1.getDiscount());
    }

}