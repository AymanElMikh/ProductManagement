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

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author AymanElMikh
 **/
public class ProductManager {

    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final Path reportFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private  Map<Product, List<Review>> products = new HashMap<>();

    private static final Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "ru-RU", new ResourceFormatter(Locale.of("ru", "RU")),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CN", new ResourceFormatter(Locale.CHINA));
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private static final ProductManager productManager = new ProductManager();

    private ProductManager(){
        loadAllData();
    }

    public static ProductManager getInstance(){
        return new ProductManager();
    }

    public static Set<String> getSupportedLocales(){
        return formatters.keySet();
    }

    private ResourceFormatter changeLocal(String languageTag){
        return formatters.getOrDefault(languageTag, formatters.get("en-GB"));
    }

    private static class ResourceFormatter{

        private final ResourceBundle resource;
        private final DateTimeFormatter dateFormat;
        private final NumberFormat moneyFormat;

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

    private Product parseProduct(String text){

        Product product = null;

        try {

            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[0]){
                case "D":
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String)values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException e) {
            logger.log(Level.WARNING, "Error parsing product " + text, e);
        }

        return product;

    }

    private Review parseReview(String text){

        Review review = null;

        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review( Rateable.convert(Integer.parseInt((String) values[0])),
                                (String)values[1] );
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing review " + text, e);
        }

        return review;
    }

    private Product loadProduct(Path file){
        Product product = null;

        try {
            product = parseProduct(
                    Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8")).findFirst().orElseThrow());
        } catch (Exception e){
            logger.log(Level.WARNING, "Error loading product " + e.getMessage());
        }


        return product;
    }

    private List<Review> loadReviews(Product product) {

        List<Review> reviews = null;

        Path file = dataFolder.resolve(
                MessageFormat.format(
                        config.getString("review.data.file"), product.getId()
                )
        );

        if ( Files.notExists(file)) {

            reviews = new ArrayList<>();

        } else {

            try {
                reviews = Files
                        .lines(file, Charset.forName("UTF-8"))
                        .map( text -> parseReview(text))
                        .filter( review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException e){
                logger.log(Level.WARNING, "Error loading Reviews", e.getMessage());
            }

        }

        return reviews;
    }

    private void loadAllData(){
        try {
            products = Files
                    .list(dataFolder)
                    .filter(
                            file -> file.getFileName().toString().startsWith("product")
                    )
                    .map(
                            file -> loadProduct(file)
                    )
                    .filter( product -> product != null)
                    .collect( Collectors.toMap(product -> product, product -> loadReviews(product)));
        } catch (Exception e){
            logger.log(Level.WARNING, "Error Loading products" + e.getMessage(), e);
        }

    }

    private void dumpData(){
        try {
            if(Files.notExists(tempFolder)){
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(
                    MessageFormat.format(config.getString("temp.file"), Instant.now().getNano()));

            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE)
            )){
                out.writeObject(products);
                products = new HashMap<>();
            }
        } catch (IOException e){
            logger.log(Level.SEVERE, "Error dumping data" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreData(){

        try {
            Path tempFile = Files.list(tempFolder)
                    .filter( path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE)
            )){
                products = (HashMap) in.readObject();
            }

        } catch (Exception e){
            logger.log(Level.WARNING, "Error restoring data" + e.getMessage(), e);
        }
    }

    private Product findProduct(int id) throws ProductManagerException{
        try {
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter( p -> p.getId() == id )
                    .findFirst()
                    .orElseThrow( () -> new ProductManagerException("Product with id " + id + " not found"));
        } finally {
            readLock.unlock();
        }


    }

    // Public Read Methods

    public Map<String, String> getDiscounts(String languageTag){

        ResourceFormatter formatter = changeLocal(languageTag);

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

    public StringBuilder printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag){

        StringBuilder txt = new StringBuilder();

        try {
            readLock.lock();
            ResourceFormatter formatter = changeLocal(languageTag);
            products.keySet()
                    .stream()
                    .sorted(sorter)
                    .filter(filter)
                    .forEach( p ->  txt.append(formatter.formatProduct(p) + '\n'));
        } catch (Exception e){
            logger.log(Level.WARNING, "Error printing product" + e.getMessage(), e);
        }
        finally {
            readLock.unlock();
        }

        return txt;

    }

    public void printProductReport(int id, String languageTag, String client){

        try {
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e){
            logger.log(Level.SEVERE, "Error printing report"+ e.getMessage(), e);
        } finally {
            readLock.unlock();
        }

    }

    private void printProductReport(Product product, String languageTag, String client) throws IOException {

        ResourceFormatter formatter = changeLocal(languageTag);
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);

        Path productFile = reportFolder.resolve(
                MessageFormat.format(config.getString("report.file"), product.getId(), client)
        );

        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(
                                productFile, StandardOpenOption.CREATE),"UTF-8"
                ))) {

            out.append(formatter.formatProduct(product)+System.lineSeparator());

            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews")+System.lineSeparator());


            } else {

                out.append(reviews.stream()
                        .map(r -> formatter.formatReview(r)+System.lineSeparator())
                        .collect(Collectors.joining())
                );

            }
        }

    }

    // Public Write methods

    public Product reviewProduct(int id, Rating rating,String comment){

        try {
            writeLock.lock();
            return  reviewProduct(findProduct(id), rating, comment);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }

    }

    private Product reviewProduct(Product product, Rating rating, String comment){

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

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore){

        Product product = null;
        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(product, new ArrayList<Review>());
        } catch (Exception e){
            logger.log(Level.INFO, "Error adding product " + e.getMessage());
            return null;
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    public  Product createProduct(int id, String name, BigDecimal price, Rating rating){

        Product product = null;

        try{
            writeLock.lock();;
            product = new Drink(id, name, price, rating);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception e){
            logger.log(Level.WARNING, "Error adding product " + e.getMessage());
            return null;
        }  finally {
            writeLock.unlock();
        }

        return product;

    }

}