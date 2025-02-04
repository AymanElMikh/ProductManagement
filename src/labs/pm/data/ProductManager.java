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

import java.lang.runtime.SwitchBootstraps;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    private ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");

    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));

    private ResourceFormatter formatter;

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());

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

    public Product reviewProduct(Product product, Rating rating, String comment){

        List<Review> reviews = products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comment));


        product = product.applyRating(Rateable.
                convert(
                    (int) Math.round(
                        reviews.stream()
                                .mapToInt(r -> r.rating().ordinal())
                                .average()
                                .orElse(0)
                )));

        products.put(product, reviews);

        return product;
    }

    public Product reviewProduct(int id, Rating rating,String comment){
        try {
            return  reviewProduct(findProduct(id), rating, comment);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }
    }

    public Product findProduct(int id) throws ProductManagerException{

        return products.keySet()
                .stream()
                .filter( p -> p.getId() == id )
                .findFirst()
                .orElseThrow( () -> new ProductManagerException("Product with id " + id + " not found"));
    }

    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void printProductReport(Product product){

        List<Review> reviews = products.get(product);
        Collections.sort(reviews);

        StringBuilder txt = new StringBuilder();

        txt.append(formatter.formatProduct(product));
        txt.append("\n");


        if (reviews.isEmpty()) {

            txt.append(formatter.getText("no.reviews"));
            txt.append("\n");
        } else{
            txt.append(reviews.stream()
                            .map( r -> formatter.formatReview(r) + '\n')
                            .collect(Collectors.joining())
                    );
        }

        System.out.println(txt);
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter){
            List<Product> productsList = new ArrayList<>(products.keySet()) ;
            productsList.sort(sorter);
            StringBuilder txt = new StringBuilder();

                    productsList
                            .stream()
                            .sorted(sorter)
                            .filter(filter)
                            .forEach(p ->  txt.append(formatter.formatProduct(p) + '\n'));

            System.out.println(txt.toString());
    }


    public void parseReview(String text){
        try {
            Object[] values = reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String)values[0]),
                    Rateable.convert(Integer.parseInt((String) values[1])),
                    (String)values[2]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing review " + text, e);
        }

    }

    public void parseProduct(String text){
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[1]){
                case "D":
                    createProduct(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String)values[5]);
                    createProduct(id, name, price, rating, bestBefore);
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException e) {
            logger.log(Level.WARNING, "Error parsing review " + text, e);
        }

    }



    public Map<String, String> getDiscounts(){

        return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summingDouble(
                                                product -> product.getDiscount().doubleValue()
                                        ), discount -> formatter.moneyFormat.format(discount)
                                )
                        )
                );
    }

}