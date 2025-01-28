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

import javax.swing.text.NumberFormatter;
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

    private static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "ru-RU", new ResourceFormatter(Locale.of("ru", "RU")),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CN", new ResourceFormatter(Locale.CHINA));

    private ResourceFormatter formatter;

    public static Set<String> getSupportedLocales(){
        return formatters.keySet();
    }

    public void changeLocal(String languageTag){
        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
    }

    public ProductManager(String languageTag){
        this.changeLocal(languageTag);
    }

    public ProductManager(Locale locale){
        this(locale.toLanguageTag());
    }

    private static class ResourceFormatter{

        private ResourceBundle resource;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        public ResourceFormatter(Locale locale){
            resource = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct(Product product){

            String type = switch (product){
                case Food food -> getText("food");
                case Drink drink -> getText("drink");
            };

            return MessageFormat.format(getText("product"), product.getName(), moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(), dateFormat.format(product.getBestBefore()),type);
        }

        private String formatReview(Review review){

            return MessageFormat.format(getText("review"),
                    review.rating().getStars(), review.comments());
        }

        private String getText(String key){
            return resource.getString(key);
        }
    }

    private Map<Product, List<Review>> products = new HashMap<>();

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

        txt.append(formatter.formatProduct(product));
        txt.append("\n");

        for( Review review : reviews ) {

            txt.append(formatter.formatReview(review));

            txt.append("\n");
        }

        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no.reviews"));
            txt.append("\n");
        }

        System.out.println(txt);
    }




}