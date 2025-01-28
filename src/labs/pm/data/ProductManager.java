/*
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package labs.pm.data;

import java.awt.dnd.DropTarget;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * @author AymanElMikh
 **/
public class ProductManager {


    private Map<Product, List<Review>> products = new HashMap<>();

    //private Product product;
    //private Review[] reviews = new Review[5];
    private final ResourceBundle resource;
    private final DateTimeFormatter dateFormat;
    private final NumberFormat moneyFormat;

    public ProductManager(Locale locale){

        resource = ResourceBundle.getBundle("labs.pm.data.resources", locale);
        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);

    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore){


        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<Review>());
        return product;
    }

    public  Product createProduct(int id, String name, BigDecimal price, Rating rating){

        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;

    }

    public Product reviewProduct(Product product, String comment,Rating rating){

        List<Review> reviews = products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comment));

        int sum = 0;
        int length = reviews.size();

        for( Review review : reviews){
            sum += review.rating().ordinal();
        }

        product = product.applyRating(Rateable.convert(Math.round((float) sum/length)));

        products.put(product, reviews);

        return product;
    }

    public Product reviewProduct(int id, String comment,Rating rating){
        return  reviewProduct(findProduct(id), comment, rating);
    }


        public Product findProduct(int id){
        Product result = null;

        for(Product product : products.keySet()){
            if( id == product.getId() ) {
                result = product;
                break;
            }
        }

        return  result;
    }

    public void printProductReport(int id){
        printProductReport(findProduct(id));
    }

    public void printProductReport(Product product){

        List<Review> reviews = products.get(product);
        Collections.sort(reviews);

        StringBuilder txt = new StringBuilder();

        String type = switch (product){
            case Food food -> resource.getString("food");
            case Drink drink -> resource.getString("drink");
        };

        txt.append(MessageFormat.format(resource.getString("product"), product.getName(), moneyFormat.format(product.getPrice()),
                product.getRating().getStars(), dateFormat.format(product.getBestBefore()),type));
        txt.append("\n");

        for( Review review : reviews ) {

            txt.append(MessageFormat.format(resource.getString("review"),
                    review.rating().getStars(), review.comments()));

            txt.append("\n");
        }

        if (reviews.isEmpty()) {
            txt.append(resource.getString("no.reviews"));
            txt.append("\n");
        }

        System.out.println(txt);
    }

}